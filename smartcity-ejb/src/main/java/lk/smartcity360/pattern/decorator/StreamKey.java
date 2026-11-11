package lk.smartcity360.pattern.decorator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Logger;
import javax.crypto.spec.SecretKeySpec;

/**
 * Resolves the 256-bit key shared by the signing and encryption decorators.
 *
 * <p>Lookup order: system property {@code smartcity.stream.key}, environment variable
 * {@code SMARTCITY_STREAM_KEY} (both Base64, exactly 32 bytes), then a fixed development key.
 * The development fallback exists only so the demo runs out of the box; it logs a warning
 * and must not be used for real data.
 */
public final class StreamKey {

    public static final String PROPERTY = "smartcity.stream.key";
    public static final String ENVIRONMENT = "SMARTCITY_STREAM_KEY";

    private static final Logger LOG = Logger.getLogger(StreamKey.class.getName());

    private StreamKey() {
    }

    public static SecretKeySpec resolve() {
        String configured = System.getProperty(PROPERTY, System.getenv(ENVIRONMENT));
        if (configured == null || configured.isBlank()) {
            LOG.warning("No stream key configured (" + ENVIRONMENT + "); using the INSECURE development key.");
            return fromBytes(sha256("smartcity360-development-key"));
        }
        byte[] bytes = Base64.getDecoder().decode(configured.trim());
        if (bytes.length != 32) {
            throw new IllegalStateException(ENVIRONMENT + " must be a Base64-encoded 32-byte key");
        }
        return fromBytes(bytes);
    }

    public static SecretKeySpec fromBytes(byte[] bytes) {
        return new SecretKeySpec(bytes, "AES");
    }

    private static byte[] sha256(String text) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
