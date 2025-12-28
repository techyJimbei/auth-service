package org.oppexai.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordUtil Test Suite")
class PasswordUtilTest {

    @Test
    @DisplayName("Should successfully hash a password")
    void testHashPassword_Success() {
        String password = "Test@1234";

        String hashedPassword = PasswordUtil.hashPassword(password);

        assertNotNull(hashedPassword);
        assertNotEquals(password, hashedPassword);
        assertTrue(hashedPassword.length() > 0);
    }

    @Test
    @DisplayName("Should generate different hashes for same password (salt test)")
    void testHashPassword_DifferentSalts() {
        String password = "Test@1234";

        String hash1 = PasswordUtil.hashPassword(password);
        String hash2 = PasswordUtil.hashPassword(password);

        assertNotNull(hash1);
        assertNotNull(hash2);
        assertNotEquals(hash1, hash2, "Same password should generate different hashes due to salt");
    }

    @Test
    @DisplayName("Should successfully verify correct password")
    void testVerifyPassword_Success() {
        String password = "Test@1234";
        String hashedPassword = PasswordUtil.hashPassword(password);

        boolean isValid = PasswordUtil.verifyPassword(password, hashedPassword);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should fail verification with incorrect password")
    void testVerifyPassword_Failure() {
        String correctPassword = "Test@1234";
        String wrongPassword = "Wrong@1234";
        String hashedPassword = PasswordUtil.hashPassword(correctPassword);

        boolean isValid = PasswordUtil.verifyPassword(wrongPassword, hashedPassword);

        assertFalse(isValid);
    }

    @ParameterizedTest
    @DisplayName("Should handle various password formats")
    @ValueSource(strings = {
            "Simple@123",
            "Complex@Pass123!",
            "Test@1234567890",
            "!@#$%^&*()Pass1",
            "VeryLongPassword@123456789"
    })
    void testHashPassword_VariousFormats(String password) {
        String hashedPassword = PasswordUtil.hashPassword(password);

        assertNotNull(hashedPassword);
        assertTrue(PasswordUtil.verifyPassword(password, hashedPassword));
    }

    @Test
    @DisplayName("Should handle empty password")
    void testHashPassword_EmptyPassword() {
        String emptyPassword = "";

        String hashedPassword = PasswordUtil.hashPassword(emptyPassword);

        assertNotNull(hashedPassword);
        assertTrue(PasswordUtil.verifyPassword(emptyPassword, hashedPassword));
    }

    @Test
    @DisplayName("Should handle null password gracefully")
    void testHashPassword_NullPassword() {
        assertThrows(Exception.class, () -> {
            PasswordUtil.hashPassword(null);
        });
    }

    @Test
    @DisplayName("Should fail verification with null password")
    void testVerifyPassword_NullPassword() {
        String hashedPassword = PasswordUtil.hashPassword("Test@1234");

        assertThrows(Exception.class, () -> {
            PasswordUtil.verifyPassword(null, hashedPassword);
        });
    }

    @Test
    @DisplayName("Should fail verification with null hash")
    void testVerifyPassword_NullHash() {
        assertThrows(Exception.class, () -> {
            PasswordUtil.verifyPassword("Test@1234", null);
        });
    }

    @Test
    @DisplayName("Should fail verification with invalid hash format")
    void testVerifyPassword_InvalidHashFormat() {
        String password = "Test@1234";
        String invalidHash = "not-a-valid-bcrypt-hash";

        assertThrows(Exception.class, () -> {
            PasswordUtil.verifyPassword(password, invalidHash);
        });
    }

    @Test
    @DisplayName("Should handle special characters in password")
    void testHashPassword_SpecialCharacters() {
        String password = "T€$t@P@$$w0rd!#%";

        String hashedPassword = PasswordUtil.hashPassword(password);

        assertNotNull(hashedPassword);
        assertTrue(PasswordUtil.verifyPassword(password, hashedPassword));
    }

    @Test
    @DisplayName("Should handle unicode characters in password")
    void testHashPassword_UnicodeCharacters() {
        String password = "Test密码@123";

        String hashedPassword = PasswordUtil.hashPassword(password);

        assertNotNull(hashedPassword);
        assertTrue(PasswordUtil.verifyPassword(password, hashedPassword));
    }

    @Test
    @DisplayName("Should verify that hash length is consistent")
    void testHashPassword_ConsistentLength() {
        String password1 = "Short@1";
        String password2 = "VeryVeryLongPasswordWithManyCharacters@123456789";

        String hash1 = PasswordUtil.hashPassword(password1);
        String hash2 = PasswordUtil.hashPassword(password2);

        assertEquals(hash1.length(), hash2.length(),
                "BCrypt hashes should have consistent length regardless of input length");
    }

    @Test
    @DisplayName("Should be case sensitive")
    void testVerifyPassword_CaseSensitive() {
        String password = "Test@1234";
        String hashedPassword = PasswordUtil.hashPassword(password);

        boolean lowerCaseValid = PasswordUtil.verifyPassword("test@1234", hashedPassword);
        boolean upperCaseValid = PasswordUtil.verifyPassword("TEST@1234", hashedPassword);
        boolean correctCaseValid = PasswordUtil.verifyPassword(password, hashedPassword);

        assertFalse(lowerCaseValid);
        assertFalse(upperCaseValid);
        assertTrue(correctCaseValid);
    }
}