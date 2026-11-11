package lk.smartcity360.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.SecretKey;
import lk.smartcity360.entity.EnergyReading;
import lk.smartcity360.pattern.decorator.AuthenticationDecorator;
import lk.smartcity360.pattern.decorator.BasicEnergyStream;
import lk.smartcity360.pattern.decorator.CompressionDecorator;
import lk.smartcity360.pattern.decorator.DataStream;
import lk.smartcity360.pattern.decorator.EncryptionDecorator;
import lk.smartcity360.pattern.decorator.StreamKey;

/** Stores energy readings and protects telemetry streams with composable wrappers (Decorator pattern). */
@Stateless
public class EnergyService {

    @PersistenceContext(unitName = "SmartCityPU")
    private EntityManager em;

    public EnergyReading record(EnergyReading reading) {
        em.persist(reading);
        return reading;
    }

    public List<EnergyReading> all() {
        return em.createQuery("SELECT e FROM EnergyReading e ORDER BY e.recordedAt DESC, e.id DESC", EnergyReading.class)
                .getResultList();
    }

    public double totalKilowattHours() {
        return em.createQuery("SELECT COALESCE(SUM(e.kilowattHours), 0.0) FROM EnergyReading e", Double.class)
                .getSingleResult();
    }

    /** Signs, compresses and encrypts a payload, in that order. */
    public String protect(String payload) {
        SecretKey key = StreamKey.resolve();
        DataStream stream = new EncryptionDecorator(
                new CompressionDecorator(
                        new AuthenticationDecorator(new BasicEnergyStream(), key)),
                key);
        return stream.process(payload);
    }

    /** Simulates the sender applying layers, network transit, and the receiver unwrapping in reverse order. */
    public DecoratorReport simulatePipeline(String meterCode, String layer1, String layer2, String layer3, boolean tamper) {
        SecretKey key = StreamKey.resolve();

        String meterDisplay = getMeterDisplay(meterCode);
        String corePayload = "meter=" + meterCode + ",hour=0,kwh=1.25 | meter=" + meterCode + ",hour=1,kwh=1.18 | meter="
                + meterCode + ",hour=2,kwh=1.05 | meter=" + meterCode + ",hour=3,kwh=0.98 | meter=" + meterCode
                + ",hour=4,kwh=1.02 | meter=" + meterCode + ",hour=5,kwh=1.45 | meter=" + meterCode + ",hour=6,kwh=2.80 | meter="
                + meterCode + ",hour=7,kwh=3.95 | meter=" + meterCode + ",hour=8,kwh=4.20 | meter=" + meterCode + ",hour=9,kwh=4.15";

        List<DecoratorReport.SenderStep> senderSteps = new ArrayList<>();
        String currentData = corePayload;
        String chainName = "Energy Meter Stream (" + meterCode + ")";

        // Step 0: Core stream
        senderSteps.add(new DecoratorReport.SenderStep("Core stream", chainName, currentData.length() + " chars", preview(currentData)));

        // Layer 1
        String[] layers = new String[]{layer1, layer2, layer3};
        List<String> activeLayers = new ArrayList<>();

        for (int i = 0; i < layers.length; i++) {
            String l = layers[i];
            if (l == null || "NONE".equalsIgnoreCase(l) || l.isBlank()) {
                continue;
            }
            activeLayers.add(l);
            String layerLabel = formatLayerName(l);
            chainName = chainName + " + " + layerLabel;

            DataStream stream = createStream(l, new BasicEnergyStream(), key);
            currentData = stream.process(currentData);
            senderSteps.add(new DecoratorReport.SenderStep("Layer " + (i + 1), chainName, currentData.length() + " chars", preview(currentData)));
        }

        // On the network: simulate potential tampering
        String networkData = currentData;
        String networkStatus;
        String networkMessage;

        if (tamper) {
            networkStatus = "Tampered";
            networkMessage = "An attacker altered the payload bytes while in transit over the network!";
            // Alter bytes in transit
            if (networkData.length() > 20) {
                char originalChar = networkData.charAt(15);
                char replacedChar = (originalChar == 'A') ? 'B' : 'A';
                networkData = networkData.substring(0, 15) + replacedChar + networkData.substring(16);
            } else {
                networkData = networkData + "_TAMPERED";
            }
        } else {
            networkStatus = "Unchanged";
            networkMessage = "The data arrived exactly as it was sent.";
        }

        // Control centre removes the layers in REVERSE order
        List<DecoratorReport.ReceiverStep> receiverSteps = new ArrayList<>();
        String unwrapData = networkData;
        boolean allPassed = true;

        for (int i = activeLayers.size() - 1; i >= 0; i--) {
            String l = activeLayers.get(i);
            String opName = getUnwrapOpName(l);
            if (!allPassed) {
                receiverSteps.add(new DecoratorReport.ReceiverStep(opName, "SKIPPED", false, "Skipped due to prior layer failure"));
                continue;
            }
            try {
                if ("ENCRYPTION".equalsIgnoreCase(l)) {
                    unwrapData = EncryptionDecorator.decrypt(unwrapData, key);
                    receiverSteps.add(new DecoratorReport.ReceiverStep(opName, "OK", true, "Decrypted with AES-256-GCM"));
                } else if ("COMPRESSION".equalsIgnoreCase(l)) {
                    unwrapData = CompressionDecorator.decompress(unwrapData);
                    receiverSteps.add(new DecoratorReport.ReceiverStep(opName, "OK", true, "Decompressed GZIP stream"));
                } else if ("AUTHENTICATION".equalsIgnoreCase(l)) {
                    unwrapData = AuthenticationDecorator.verify(unwrapData, key);
                    receiverSteps.add(new DecoratorReport.ReceiverStep(opName, "OK", true, "HMAC-SHA256 signature verified"));
                }
            } catch (Exception e) {
                allPassed = false;
                receiverSteps.add(new DecoratorReport.ReceiverStep(opName, "FAILED", false, e.getMessage()));
            }
        }

        String summaryMessage;
        if (allPassed && corePayload.equals(unwrapData)) {
            summaryMessage = "The receiver got exactly the original meter data.";
        } else {
            summaryMessage = "Security Alert: Tampering detected! The payload was rejected during layer unwrapping.";
        }

        return new DecoratorReport(meterDisplay, chainName, senderSteps, tamper, networkStatus, networkMessage,
                receiverSteps, allPassed, summaryMessage, unwrapData);
    }

    private String getMeterDisplay(String code) {
        if ("EM-1001".equals(code)) return "EM-1001 - Galle Road, Colombo 03";
        if ("EM-1002".equals(code)) return "EM-1002 - Dharmapala Mawatha, Colombo 03";
        if ("EM-1003".equals(code)) return "EM-1003 - Ward Place, Colombo 07";
        return code + " - Municipal Energy Meter";
    }

    private String formatLayerName(String layer) {
        if ("AUTHENTICATION".equalsIgnoreCase(layer)) return "Authentication";
        if ("COMPRESSION".equalsIgnoreCase(layer)) return "Compression";
        if ("ENCRYPTION".equalsIgnoreCase(layer)) return "Encryption";
        return layer;
    }

    private String getUnwrapOpName(String layer) {
        if ("ENCRYPTION".equalsIgnoreCase(layer)) return "Decrypt";
        if ("COMPRESSION".equalsIgnoreCase(layer)) return "Decompress";
        if ("AUTHENTICATION".equalsIgnoreCase(layer)) return "Verify signature";
        return "Unwrap " + layer;
    }

    private DataStream createStream(String layer, DataStream base, SecretKey key) {
        if ("AUTHENTICATION".equalsIgnoreCase(layer)) {
            return new AuthenticationDecorator(base, key);
        } else if ("COMPRESSION".equalsIgnoreCase(layer)) {
            return new CompressionDecorator(base);
        } else if ("ENCRYPTION".equalsIgnoreCase(layer)) {
            return new EncryptionDecorator(base, key);
        }
        return base;
    }

    private String preview(String s) {
        if (s == null) return "";
        if (s.length() <= 65) return s;
        return s.substring(0, 62) + "...";
    }
}
