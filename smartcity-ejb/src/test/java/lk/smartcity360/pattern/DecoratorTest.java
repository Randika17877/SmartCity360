package lk.smartcity360.pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import javax.crypto.SecretKey;
import lk.smartcity360.pattern.decorator.AuthenticationDecorator;
import lk.smartcity360.pattern.decorator.BasicEnergyStream;
import lk.smartcity360.pattern.decorator.CompressionDecorator;
import lk.smartcity360.pattern.decorator.DataStream;
import lk.smartcity360.pattern.decorator.EncryptionDecorator;
import lk.smartcity360.pattern.decorator.StreamKey;
import org.junit.jupiter.api.Test;

class DecoratorTest {

    private static final SecretKey KEY = StreamKey.fromBytes(new byte[32]);
    private static final SecretKey OTHER_KEY = StreamKey.fromBytes(new byte[]{
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1});

    private static DataStream fullChain() {
        return new EncryptionDecorator(
                new CompressionDecorator(new AuthenticationDecorator(new BasicEnergyStream(), KEY)), KEY);
    }

    @Test
    void basicStreamIsPassThrough() {
        assertEquals("meter=1", new BasicEnergyStream().process("meter=1"));
    }

    @Test
    void fullChainCanBeReversedByTheReceiver() {
        String payload = "meter=MTR-001,kwh=12.5";

        String wire = fullChain().process(payload);

        String signed = CompressionDecorator.decompress(EncryptionDecorator.decrypt(wire, KEY));
        assertEquals(payload, AuthenticationDecorator.verify(signed, KEY));
    }

    @Test
    void ciphertextHidesThePayloadAndChangesEveryTime() {
        String payload = "meter=MTR-001,kwh=12.5";

        String first = fullChain().process(payload);
        String second = fullChain().process(payload);

        assertFalse(new String(Base64.getDecoder().decode(first)).contains("MTR-001"));
        assertNotEquals(first, second, "a fresh IV must be used for every message");
    }

    @Test
    void decryptionWithTheWrongKeyFails() {
        String wire = fullChain().process("secret");

        assertThrows(SecurityException.class, () -> EncryptionDecorator.decrypt(wire, OTHER_KEY));
    }

    @Test
    void tamperedPayloadFailsSignatureCheck() {
        String signed = new AuthenticationDecorator(new BasicEnergyStream(), KEY).process("kwh=10");

        assertEquals("kwh=10", AuthenticationDecorator.verify(signed, KEY));
        assertThrows(SecurityException.class, () -> AuthenticationDecorator.verify(signed.replace("kwh=10", "kwh=99"), KEY));
        assertThrows(SecurityException.class, () -> AuthenticationDecorator.verify(signed, OTHER_KEY));
    }

    @Test
    void compressionShrinksRepetitiveData() {
        String payload = "kwh=1;".repeat(500);

        String compressed = new CompressionDecorator(new BasicEnergyStream()).process(payload);

        assertTrue(compressed.length() < payload.length());
        assertEquals(payload, CompressionDecorator.decompress(compressed));
    }
}
