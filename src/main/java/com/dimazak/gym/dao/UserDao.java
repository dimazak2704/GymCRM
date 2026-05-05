package com.dimazak.gym.dao;

import com.dimazak.gym.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final Map<Long, User> userStorage;
    private final AtomicLong userIdSequence;

    public UserDao(@Qualifier("userStorage") Map<Long, User> userStorage,
                   @Qualifier("userIdSequence") AtomicLong userIdSequence) {
        this.userStorage = userStorage;
        this.userIdSequence = userIdSequence;
    }

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(userIdSequence.incrementAndGet());
        }
        userStorage.put(user.getId(), user);
        log.debug("Saved user with id: {}", user.getId());
        return user;
    }

    public Optional<User> findById(Long id) {
        log.debug("Finding user by id: {}", id);
        return Optional.ofNullable(userStorage.get(id));
    }

    public Collection<User> findAll() {
        log.debug("Finding all users");
        return userStorage.values();
    }

    public void deleteById(Long id) {
        User removed = userStorage.remove(id);
        if (removed != null) {
            log.debug("Deleted user with id: {}", id);
        } else {
            log.warn("Attempted to delete non-existent user with id: {}", id);
        }
    }
}