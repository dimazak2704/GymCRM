package com.dimazak.dao;

import com.dimazak.gym.dao.UserDao;
import com.dimazak.gym.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class UserDaoTest {

    private static final Long USER_ID_1 = 1L;
    private static final Long USER_ID_2 = 2L;
    private static final Long USER_ID_5 = 5L;
    private static final Long NON_EXISTENT_ID = 99L;
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Smith";
    private static final String USERNAME = "John.Smith";
    private static final String PASSWORD = "abc1234567";
    private static final boolean IS_ACTIVE = true;

    private Map<Long, User> userMap;
    private AtomicLong userIdSequence;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        userMap = new ConcurrentHashMap<>();
        userIdSequence = new AtomicLong(0);
        userDao = new UserDao(userMap, userIdSequence);
    }

    @Test
    void user_equalsByIdOnly() {
        User u1 = new User(1L, "John", "Smith", "John.Smith", "pass", true);
        User u2 = new User(1L, "Jane", "Doe", "Jane.Doe", "other", false);
        User u3 = new User(2L, "John", "Smith", "John.Smith", "pass", true);

        assertEquals(u1, u2);
        assertNotEquals(u1, u3);
        assertEquals(u1.hashCode(), u2.hashCode());
    }

    @Test
    void save_shouldAssignIdAndStore() {
        User user = new User(null, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, IS_ACTIVE);

        User saved = userDao.save(user);

        assertEquals(USER_ID_1, saved.getId());
        assertTrue(userMap.containsKey(USER_ID_1));
    }

    @Test
    void save_shouldNotReassignExistingId() {
        User user = new User(USER_ID_5, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, IS_ACTIVE);

        User saved = userDao.save(user);

        assertEquals(USER_ID_5, saved.getId());
        assertTrue(userMap.containsKey(USER_ID_5));
    }

    @Test
    void findById_shouldReturnUser() {
        User user = new User(USER_ID_1, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, IS_ACTIVE);
        userMap.put(USER_ID_1, user);

        Optional<User> result = userDao.findById(USER_ID_1);

        assertTrue(result.isPresent());
        assertEquals(USER_ID_1, result.get().getId());
        assertEquals(USERNAME, result.get().getUsername());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        assertTrue(userDao.findById(NON_EXISTENT_ID).isEmpty());
    }

    @Test
    void findAll_shouldReturnAll() {
        userMap.put(USER_ID_1, new User(USER_ID_1, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, IS_ACTIVE));
        userMap.put(USER_ID_2, new User(USER_ID_2, "Jane", "Doe", "Jane.Doe", PASSWORD, IS_ACTIVE));

        Collection<User> all = userDao.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void deleteById_shouldRemoveUser() {
        userMap.put(USER_ID_1, new User(USER_ID_1, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, IS_ACTIVE));

        userDao.deleteById(USER_ID_1);

        assertFalse(userMap.containsKey(USER_ID_1));
    }

    @Test
    void deleteById_shouldHandleNonExistent() {
        userDao.deleteById(NON_EXISTENT_ID); // should not throw
        assertTrue(userMap.isEmpty());
    }
}