package com.dimazak.gym.dao;

import com.dimazak.gym.model.User;
import com.dimazak.gym.storage.InMemoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private InMemoryStorage storage;

    @Autowired
    public void setStorage(InMemoryStorage storage) {
        this.storage = storage;
    }

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(storage.nextUserId());
        }
        storage.getUserStorage().put(user.getId(), user);
        log.debug("Saved user with id: {}", user.getId());
        return user;
    }

    public Optional<User> findById(Long id) {
        log.debug("Finding user by id: {}", id);
        return Optional.ofNullable(storage.getUserStorage().get(id));
    }

    public Collection<User> findAll() {
        log.debug("Finding all users");
        return storage.getUserStorage().values();
    }

    public void deleteById(Long id) {
        User removed = storage.getUserStorage().remove(id);
        if (removed != null) {
            log.debug("Deleted user with id: {}", id);
        } else {
            log.warn("Attempted to delete non-existent user with id: {}", id);
        }
    }
}