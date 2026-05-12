package com.dimazak.gym.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {

    private static final int EXPECTED_LENGTH = 10;

    private final PasswordGenerator passwordGenerator = new PasswordGenerator();

    @Test
    void generatePassword_shouldReturnCorrectLength() {
        assertEquals(EXPECTED_LENGTH, passwordGenerator.generatePassword().length());
    }

    @Test
    void generatePassword_shouldGenerateDifferentPasswords() {
        assertNotEquals(passwordGenerator.generatePassword(),
                passwordGenerator.generatePassword());
    }

    @Test
    void generatePassword_shouldContainOnlyAlphanumeric() {
        assertTrue(passwordGenerator.generatePassword().matches("[A-Za-z0-9]+"));
    }
}