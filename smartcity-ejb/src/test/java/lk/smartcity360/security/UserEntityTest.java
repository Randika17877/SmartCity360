package lk.smartcity360.security;

import lk.smartcity360.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserEntityTest {

    @Test
    void defaultRoleIsCitizen() {
        User user = new User();
        assertEquals("CITIZEN", user.getRole());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void constructorDefaultsNullOrBlankRoleToCitizen() {
        User user1 = new User("test1", "test1@example.com", "Test User", "hash", "salt", null);
        assertEquals("CITIZEN", user1.getRole());

        User user2 = new User("test2", "test2@example.com", "Test User", "hash", "salt", "   ");
        assertEquals("CITIZEN", user2.getRole());

        User user3 = new User("test3", "test3@example.com", "Test User", "hash", "salt", "ADMIN");
        assertEquals("ADMIN", user3.getRole());
    }
}
