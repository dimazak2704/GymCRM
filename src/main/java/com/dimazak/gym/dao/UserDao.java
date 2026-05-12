package com.dimazak.gym.dao;

import com.dimazak.gym.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final SessionFactory sessionFactory;

    public UserDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public User save(User user) {
        Session session = sessionFactory.getCurrentSession();
        if (user.getId() == null) {
            session.persist(user);
            log.debug("Persisted new user with id: {}", user.getId());
        } else {
            user = session.merge(user);
            log.debug("Merged user with id: {}", user.getId());
        }
        return user;
    }

    public Optional<User> findById(Long id) {
        log.debug("Finding user by id: {}", id);
        Session session = sessionFactory.getCurrentSession();
        return Optional.ofNullable(session.get(User.class, id));
    }

    public Optional<User> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery(
                        "FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    public List<User> findAll() {
        log.debug("Finding all users");
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery("FROM User", User.class).list();
    }

    public void delete(User user) {
        log.debug("Deleting user with id: {}", user.getId());
        Session session = sessionFactory.getCurrentSession();
        session.remove(user);
    }
}