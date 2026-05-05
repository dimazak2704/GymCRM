package com.dimazak.util;

import com.dimazak.gym.dao.UserDao;
import com.dimazak.gym.model.User;
import com.dimazak.gym.util.UsernameGenerator;
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
    private static final String USERNAME_WITH_SUFFIX_1 = "John.Smith1";
    private static final String USERNAME_WITH_SUFFIX_2 = "John.Smith2";
    private static final String PASSWORD = "pass";

    @Mock private UserDao userDao;

    @InjectMocks
    private UsernameGenerator usernameGenerator;

    @Test
    void generateUsername_shouldReturnBaseWhenNoConflict() {
        when(userDao.findAll()).thenReturn(Collections.emptyList());

        String username = usernameGenerator.generateUsername(FIRST_NAME, LAST_NAME);

        assertEquals(BASE_USERNAME, username);
    }

    @Test
    void generateUsername_shouldAppendNumberOnConflict() {
        User existing = new User(1L, FIRST_NAME, LAST_NAME, BASE_USERNAME, PASSWORD, true);
        when(userDao.findAll()).thenReturn(List.of(existing));

        String username = usernameGenerator.generateUsername(FIRST_NAME, LAST_NAME);

        assertEquals(USERNAME_WITH_SUFFIX_1, username);
    }

    @Test
    void generateUsername_shouldIncrementOnMultipleConflicts() {
        User u1 = new User(1L, FIRST_NAME, LAST_NAME, BASE_USERNAME, PASSWORD, true);
        User u2 = new User(2L, FIRST_NAME, LAST_NAME, USERNAME_WITH_SUFFIX_1, PASSWORD, true);
        when(userDao.findAll()).thenReturn(List.of(u1, u2));

        String username = usernameGenerator.generateUsername(FIRST_NAME, LAST_NAME);

        assertEquals(USERNAME_WITH_SUFFIX_2, username);
    }
}