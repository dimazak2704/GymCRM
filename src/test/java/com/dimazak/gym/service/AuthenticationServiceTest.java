package com.dimazak.gym.service;

import com.dimazak.gym.dao.UserDao;
import com.dimazak.gym.exception.AuthenticationException;
import com.dimazak.gym.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    private static final String USERNAME = "John.Doe";
    private static final String RAW_PASSWORD = "pass123456";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedHash";
    private static final String WRONG_USERNAME = "wrong";
    private static final String WRONG_PASSWORD = "wrongPass";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";

    @Mock private UserDao userDao;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void authenticate_shouldPassForValidCredentials() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, ENCODED_PASSWORD, true);
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        assertDoesNotThrow(() -> authenticationService.authenticate(USERNAME, RAW_PASSWORD));
    }

    @Test
    void authenticate_shouldThrowForInvalidUsername() {
        when(userDao.findByUsername(WRONG_USERNAME)).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class,
                () -> authenticationService.authenticate(WRONG_USERNAME, RAW_PASSWORD));
    }

    @Test
    void authenticate_shouldThrowForInvalidPassword() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, ENCODED_PASSWORD, true);
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(WRONG_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        assertThrows(AuthenticationException.class,
                () -> authenticationService.authenticate(USERNAME, WRONG_PASSWORD));
    }
}