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
import static org.mockito.ArgumentMatchers.any;
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

    private User createTestUser() {
        return new User(1L, FIRST_NAME, LAST_NAME, USERNAME, ENCODED_PASSWORD, true);
    }

    // ==================== authenticate() ====================

    @Test
    void authenticate_shouldPassForValidCredentials() {
        User user = createTestUser();
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
        User user = createTestUser();
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(WRONG_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        assertThrows(AuthenticationException.class,
                () -> authenticationService.authenticate(USERNAME, WRONG_PASSWORD));
    }

    @Test
    void authenticate_shouldNotChangeIsLoggedFlag() {
        User user = createTestUser();
        user.setLogged(false);
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        authenticationService.authenticate(USERNAME, RAW_PASSWORD);

        assertFalse(user.isLogged());
        verify(userDao, never()).save(any());
    }

    // ==================== login() ====================

    @Test
    void login_shouldSetIsLoggedTrueForValidCredentials() {
        User user = createTestUser();
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        authenticationService.login(USERNAME, RAW_PASSWORD);

        assertTrue(user.isLogged());
        verify(userDao).save(user);
    }

    @Test
    void login_shouldThrowForInvalidUsername() {
        when(userDao.findByUsername(WRONG_USERNAME)).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class,
                () -> authenticationService.login(WRONG_USERNAME, RAW_PASSWORD));
        verify(userDao, never()).save(any());
    }

    @Test
    void login_shouldThrowForInvalidPassword() {
        User user = createTestUser();
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(WRONG_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        assertThrows(AuthenticationException.class,
                () -> authenticationService.login(USERNAME, WRONG_PASSWORD));
        assertFalse(user.isLogged());
        verify(userDao, never()).save(any());
    }

    @Test
    void login_shouldKeepIsLoggedTrueWhenAlreadyLogged() {
        User user = createTestUser();
        user.setLogged(true);
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        authenticationService.login(USERNAME, RAW_PASSWORD);

        assertTrue(user.isLogged());
        verify(userDao).save(user);
    }

    // ==================== logout() ====================

    @Test
    void logout_shouldSetIsLoggedFalse() {
        User user = createTestUser();
        user.setLogged(true);
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        authenticationService.logout(USERNAME);

        assertFalse(user.isLogged());
        verify(userDao).save(user);
    }

    @Test
    void logout_shouldThrowWhenUserNotFound() {
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class,
                () -> authenticationService.logout(USERNAME));
        verify(userDao, never()).save(any());
    }

    @Test
    void logout_shouldKeepIsLoggedFalseWhenAlreadyNotLogged() {
        User user = createTestUser();
        user.setLogged(false);
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        authenticationService.logout(USERNAME);

        assertFalse(user.isLogged());
        verify(userDao).save(user);
    }

    // ==================== checkLogged() ====================

    @Test
    void checkLogged_shouldPassWhenUserIsLogged() {
        User user = createTestUser();
        user.setLogged(true);
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> authenticationService.checkLogged(USERNAME));
    }

    @Test
    void checkLogged_shouldThrowWhenUserIsNotLogged() {
        User user = createTestUser();
        user.setLogged(false);
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authenticationService.checkLogged(USERNAME));
        assertTrue(ex.getMessage().toLowerCase().contains("not logged in"));
    }

    @Test
    void checkLogged_shouldThrowWhenUserNotFound() {
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class,
                () -> authenticationService.checkLogged(USERNAME));
    }
}