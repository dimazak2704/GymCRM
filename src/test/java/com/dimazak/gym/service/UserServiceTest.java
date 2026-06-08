package com.dimazak.gym.service;

import com.dimazak.gym.dao.UserDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    @Test
    void countActiveUsers_shouldReturnCountFromDao() {
        when(userDao.countActiveUsers()).thenReturn(15L);

        long result = userService.countActiveUsers();

        assertEquals(15L, result);
        verify(userDao).countActiveUsers();
    }

    @Test
    void countActiveUsers_shouldReturnZeroWhenNoActiveUsers() {
        when(userDao.countActiveUsers()).thenReturn(0L);

        long result = userService.countActiveUsers();

        assertEquals(0L, result);
    }

    @Test
    void countActiveUsers_shouldDelegateToDao() {
        when(userDao.countActiveUsers()).thenReturn(100L);

        userService.countActiveUsers();

        verify(userDao, times(1)).countActiveUsers();
    }
}