package com.dimazak.util;

import com.dimazak.gym.util.PasswordGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {

    private static final int EXPECTED_PASSWORD_LENGTH = 10;
    private static final String ALPHANUMERIC_PATTERN = "[A-Za-z0-9]+";

    private final PasswordGenerator passwordGenerator = new PasswordGenerator();

    @Test
    void generatePassword_shouldReturnCorrectLength() {
        String password = passwordGenerator.generatePassword();

        assertEquals(EXPECTED_PASSWORD_LENGTH, password.length());
    }

    @Test
    void generatePassword_shouldGenerateDifferentPasswords() {
        String p1 = passwordGenerator.generatePassword();
        String p2 = passwordGenerator.generatePassword();

        assertNotEquals(p1, p2);
    }

    @Test
    void generatePassword_shouldContainOnlyAlphanumeric() {
        String password = passwordGenerator.generatePassword();

        assertTrue(password.matches(ALPHANUMERIC_PATTERN));
    }
}