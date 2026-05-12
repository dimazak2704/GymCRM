package com.dimazak.gym.service;

import com.dimazak.gym.dao.UserDao;
import com.dimazak.gym.exception.AuthenticationException;
import com.dimazak.gym.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    private static final String USERNAME = "John.Doe";
    private static final String PASSWORD = "pass123456";

    @Mock private UserDao userDao;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void authenticate_shouldPassForValidCredentials() {
        User user = new User(1L, "John", "Doe", USERNAME, PASSWORD, true);
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> authenticationService.authenticate(USERNAME, PASSWORD));
    }

    @Test
    void authenticate_shouldThrowForInvalidUsername() {
        when(userDao.findByUsername("wrong")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class,
                () -> authenticationService.authenticate("wrong", PASSWORD));
    }

    @Test
    void authenticate_shouldThrowForInvalidPassword() {
        User user = new User(1L, "John", "Doe", USERNAME, PASSWORD, true);
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        assertThrows(AuthenticationException.class,
                () -> authenticationService.authenticate(USERNAME, "wrongPass"));
    }
}