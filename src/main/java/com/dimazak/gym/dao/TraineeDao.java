package com.dimazak.gym.dao;

import com.dimazak.gym.model.Trainee;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TraineeDao {

    private static final Logger log = LoggerFactory.getLogger(TraineeDao.class);

    private final SessionFactory sessionFactory;

    public TraineeDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Trainee save(Trainee trainee) {
        Session session = sessionFactory.getCurrentSession();
        if (trainee.getId() == null) {
            session.persist(trainee);
            log.debug("Persisted new trainee with id: {}", trainee.getId());
        } else {
            trainee = session.merge(trainee);
            log.debug("Merged trainee with id: {}", trainee.getId());
        }
        return trainee;
    }

    public Optional<Trainee> findById(Long id) {
        log.debug("Finding trainee by id: {}", id);
        Session session = sessionFactory.getCurrentSession();
        return Optional.ofNullable(session.get(Trainee.class, id));
    }

    public Optional<Trainee> findByUsername(String username) {
        log.debug("Finding trainee by username: {}", username);
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery(
                        "FROM Trainee t JOIN FETCH t.user WHERE t.user.username = :username",
                        Trainee.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    public void delete(Trainee trainee) {
        log.debug("Deleting trainee with id: {}", trainee.getId());
        Session session = sessionFactory.getCurrentSession();
        session.remove(trainee);
    }
}