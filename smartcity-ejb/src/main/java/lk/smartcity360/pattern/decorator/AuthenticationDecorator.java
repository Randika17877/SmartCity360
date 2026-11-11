package lk.smartcity360.pattern.decorator;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

/**
 * Appends an HMAC-SHA256 tag so a receiver holding the same key can verify the payload
 * was not altered: {@code payload|sig=<base64 tag>}.
 */
public class AuthenticationDecorator extends StreamDecorator {

    static final String SEPARATOR = "|sig=";

    private final SecretKey key;

    public AuthenticationDecorator(DataStream stream, SecretKey key) {
        super(stream);
        this.key = key;
    }

    @Override
    public String process(String data) {
        String payload = wrapped.process(data);
        return payload + SEPARATOR + sign(payload, key);
    }

    /** Returns the payload if the tag is valid, otherwise throws. */
    public static String verify(String signed, SecretKey key) {
        int at = signed.lastIndexOf(SEPARATOR);
        if (at < 0) {
            throw new SecurityException("Missing signature");
        }
        String payload = signed.substring(0, at);
        String expected = sign(payload, key);
        String actual = signed.substring(at + SEPARATOR.length());
        if (!java.security.MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8))) {
            throw new SecurityException("Signature mismatch");
        }
        return payload;
    }

    private static String sign(String payload, SecretKey key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(key.getEncoded(), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Signing failed", e);
        }
    }
}
