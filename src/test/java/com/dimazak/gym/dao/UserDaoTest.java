package com.dimazak.gym.dao;

import com.dimazak.gym.config.TestConfig;
import com.dimazak.gym.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@Transactional
@Rollback
class UserDaoTest {

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Smith";
    private static final String USERNAME = "John.Smith";
    private static final String PASSWORD = "abc1234567";

    @Autowired
    private UserDao userDao;

    @Test
    void save_shouldPersistNewUser() {
        User user = new User(null, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);

        User saved = userDao.save(user);

        assertNotNull(saved.getId());
        assertEquals(FIRST_NAME, saved.getFirstName());
        assertEquals(USERNAME, saved.getUsername());
    }

    @Test
    void save_shouldMergeExistingUser() {
        User user = new User(null, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        User saved = userDao.save(user);

        saved.setFirstName("Updated");
        User merged = userDao.save(saved);

        assertEquals(saved.getId(), merged.getId());
        assertEquals("Updated", merged.getFirstName());
    }

    @Test
    void findById_shouldReturnUserWhenExists() {
        User user = new User(null, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        User saved = userDao.save(user);

        Optional<User> found = userDao.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals(USERNAME, found.get().getUsername());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        Optional<User> found = userDao.findById(999L);

        assertTrue(found.isEmpty());
    }

    @Test
    void findByUsername_shouldReturnUserWhenExists() {
        User user = new User(null, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        userDao.save(user);

        Optional<User> found = userDao.findByUsername(USERNAME);

        assertTrue(found.isPresent());
        assertEquals(USERNAME, found.get().getUsername());
    }

    @Test
    void findByUsername_shouldReturnEmptyWhenNotExists() {
        Optional<User> found = userDao.findByUsername("NonExistent");

        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        userDao.save(new User(null, "John", "Doe", "John.Doe", PASSWORD, true));
        userDao.save(new User(null, "Jane", "Doe", "Jane.Doe", PASSWORD, true));

        List<User> all = userDao.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void delete_shouldRemoveUser() {
        User user = new User(null, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        User saved = userDao.save(user);

        userDao.delete(saved);

        assertTrue(userDao.findById(saved.getId()).isEmpty());
    }

    @Test
    void user_equalsByIdOnly() {
        User u1 = new User(null, "John", "Smith", "John.Smith", "pass1", true);
        User u2 = new User(null, "Jane", "Doe", "Jane.Doe", "pass2", false);
        u1 = userDao.save(u1);
        u2 = userDao.save(u2);

        assertNotEquals(u1, u2);

        User u3 = new User();
        u3.setId(u1.getId());
        assertEquals(u1, u3);
        assertEquals(u1.hashCode(), u3.hashCode());
    }
}