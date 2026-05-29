package com.dimazak.gym.dao;

import com.dimazak.gym.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DaoTest
class UserDaoTest {

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Smith";
    private static final String USERNAME = "John.Smith";
    private static final String PASSWORD = "abc1234567";
    private static final String UPDATED_NAME = "Updated";
    private static final String NON_EXISTENT = "NonExistent";

    @Autowired
    private UserDao userDao;

    @Test
    void save_shouldPersistNewUser() {
        User user = new User(null, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);

        User saved = userDao.save(user);

        assertNotNull(saved.getId());
        assertEquals(USERNAME, saved.getUsername());
    }

    @Test
    void save_shouldMergeExistingUser() {
        User saved = userDao.save(new User(null, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true));

        saved.setFirstName(UPDATED_NAME);
        User merged = userDao.save(saved);

        assertEquals(saved.getId(), merged.getId());
        assertEquals(UPDATED_NAME, merged.getFirstName());
    }

    @Test
    void findByUsername_shouldReturnUserWhenExists() {
        userDao.save(new User(null, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true));

        Optional<User> found = userDao.findByUsername(USERNAME);

        assertTrue(found.isPresent());
        assertEquals(USERNAME, found.get().getUsername());
    }

    @Test
    void findByUsername_shouldReturnEmptyWhenNotExists() {
        assertTrue(userDao.findByUsername(NON_EXISTENT).isEmpty());
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        userDao.save(new User(null, "A", "B", "A.B", PASSWORD, true));
        userDao.save(new User(null, "C", "D", "C.D", PASSWORD, true));

        List<User> all = userDao.findAll();

        assertEquals(2, all.size());
    }
}