package lk.smartcity360.pattern.decorator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** GZIP-compresses the wrapped output and Base64-encodes it so it stays a text payload. */
public class CompressionDecorator extends StreamDecorator {

    public CompressionDecorator(DataStream stream) {
        super(stream);
    }

    @Override
    public String process(String data) {
        byte[] input = wrapped.process(data).getBytes(StandardCharsets.UTF_8);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
                gzip.write(input);
            }
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Compression failed", e);
        }
    }

    /** Inverse of {@link #process}'s compression step. */
    public static String decompress(String encoded) {
        byte[] compressed = Base64.getDecoder().decode(encoded);
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
            return new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Decompression failed", e);
        }
    }
}
