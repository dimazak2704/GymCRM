package com.dimazak;

import com.dimazak.gym.util.PasswordGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {

    private final PasswordGenerator passwordGenerator = new PasswordGenerator();

    @Test
    void generatePassword_shouldReturn10Characters() {
        String password = passwordGenerator.generatePassword();

        assertEquals(10, password.length());
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

        assertTrue(password.matches("[A-Za-z0-9]+"));
    }
}