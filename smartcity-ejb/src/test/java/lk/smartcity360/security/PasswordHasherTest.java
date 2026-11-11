package lk.smartcity360.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void hashesAndVerifiesPasswordCorrectly() {
        String password = "SecretPassword123!";
        String salt = PasswordHasher.generateSalt();

        assertNotNull(salt);
        assertEqualsLength(32, salt);

        String hash = PasswordHasher.hash(password, salt);
        assertNotNull(hash);

        assertTrue(PasswordHasher.verify(password, salt, hash));
        assertFalse(PasswordHasher.verify("WrongPassword", salt, hash));
        assertFalse(PasswordHasher.verify(password, PasswordHasher.generateSalt(), hash));
    }

    @Test
    void differentSaltsYieldDifferentHashesForSamePassword() {
        String password = "samePassword";
        String salt1 = PasswordHasher.generateSalt();
        String salt2 = PasswordHasher.generateSalt();

        String hash1 = PasswordHasher.hash(password, salt1);
        String hash2 = PasswordHasher.hash(password, salt2);

        assertNotEquals(hash1, hash2);
    }

    @Test
    void verifiesSampleDatabaseUserHashes() {
        String adminSalt = "102030405060708090a0b0c0d0e0f001";
        String adminHash = PasswordHasher.hash("admin123", adminSalt);
        assertTrue(PasswordHasher.verify("admin123", adminSalt, adminHash));

        String officerSalt = "2030405060708090a0b0c0d0e0f00102";
        String officerHash = PasswordHasher.hash("officer123", officerSalt);
        assertTrue(PasswordHasher.verify("officer123", officerSalt, officerHash));

        String citizenSalt = "30405060708090a0b0c0d0e0f0010203";
        String citizenHash = PasswordHasher.hash("citizen123", citizenSalt);
        assertTrue(PasswordHasher.verify("citizen123", citizenSalt, citizenHash));
    }

    @Test
    void printsSampleHash() {
        String salt = "102030405060708090a0b0c0d0e0f001";
        String hash = PasswordHasher.hash("admin123", salt);
        System.out.println("SAMPLE_ADMIN_HASH=" + hash);
    }

    private void assertEqualsLength(int expected, String actual) {
        assertTrue(actual != null && actual.length() == expected,
                "Expected length " + expected + " but got " + (actual == null ? "null" : actual.length()));
    }
}
