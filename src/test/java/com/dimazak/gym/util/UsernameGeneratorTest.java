package com.dimazak.gym.util;

import com.dimazak.gym.dao.UserDao;
import com.dimazak.gym.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsernameGeneratorTest {

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Smith";
    private static final String BASE_USERNAME = "John.Smith";

    @Mock private UserDao userDao;

    @InjectMocks
    private UsernameGenerator usernameGenerator;

    @Test
    void generateUsername_shouldReturnBaseWhenNoConflict() {
        when(userDao.findAll()).thenReturn(Collections.emptyList());

        assertEquals(BASE_USERNAME, usernameGenerator.generateUsername(FIRST_NAME, LAST_NAME));
    }

    @Test
    void generateUsername_shouldAppendNumberOnConflict() {
        User existing = new User(1L, FIRST_NAME, LAST_NAME, BASE_USERNAME, "p", true);
        when(userDao.findAll()).thenReturn(List.of(existing));

        assertEquals("John.Smith1", usernameGenerator.generateUsername(FIRST_NAME, LAST_NAME));
    }

    @Test
    void generateUsername_shouldIncrementOnMultipleConflicts() {
        User u1 = new User(1L, FIRST_NAME, LAST_NAME, BASE_USERNAME, "p", true);
        User u2 = new User(2L, FIRST_NAME, LAST_NAME, "John.Smith1", "p", true);
        when(userDao.findAll()).thenReturn(List.of(u1, u2));

        assertEquals("John.Smith2", usernameGenerator.generateUsername(FIRST_NAME, LAST_NAME));
    }
}