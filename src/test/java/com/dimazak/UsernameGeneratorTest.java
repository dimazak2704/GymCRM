package com.dimazak;

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

    @Mock private UserDao userDao;

    @InjectMocks
    private UsernameGenerator usernameGenerator;

    @Test
    void generateUsername_shouldReturnBaseWhenNoConflict() {
        when(userDao.findAll()).thenReturn(Collections.emptyList());

        String username = usernameGenerator.generateUsername("John", "Smith");

        assertEquals("John.Smith", username);
    }

    @Test
    void generateUsername_shouldAppendNumberOnConflict() {
        User existing = new User(1L, "John", "Smith", "John.Smith", "pass", true);
        when(userDao.findAll()).thenReturn(List.of(existing));

        String username = usernameGenerator.generateUsername("John", "Smith");

        assertEquals("John.Smith1", username);
    }

    @Test
    void generateUsername_shouldIncrementOnMultipleConflicts() {
        User u1 = new User(1L, "John", "Smith", "John.Smith", "pass", true);
        User u2 = new User(2L, "John", "Smith", "John.Smith1", "pass", true);
        when(userDao.findAll()).thenReturn(List.of(u1, u2));

        String username = usernameGenerator.generateUsername("John", "Smith");

        assertEquals("John.Smith2", username);
    }
}