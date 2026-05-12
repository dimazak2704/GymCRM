package com.dimazak.gym.dao;

import com.dimazak.gym.model.Trainer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDao {

    private static final Logger log = LoggerFactory.getLogger(TrainerDao.class);

    private final SessionFactory sessionFactory;

    public TrainerDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Trainer save(Trainer trainer) {
        Session session = sessionFactory.getCurrentSession();
        if (trainer.getId() == null) {
            session.persist(trainer);
            log.debug("Persisted new trainer with id: {}", trainer.getId());
        } else {
            trainer = session.merge(trainer);
            log.debug("Merged trainer with id: {}", trainer.getId());
        }
        return trainer;
    }

    public Optional<Trainer> findById(Long id) {
        log.debug("Finding trainer by id: {}", id);
        Session session = sessionFactory.getCurrentSession();
        return Optional.ofNullable(session.get(Trainer.class, id));
    }

    public Optional<Trainer> findByUsername(String username) {
        log.debug("Finding trainer by username: {}", username);
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery(
                        "FROM Trainer t JOIN FETCH t.user WHERE t.user.username = :username",
                        Trainer.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    public List<Trainer> findUnassignedTrainersByTraineeUsername(String traineeUsername) {
        log.debug("Finding unassigned trainers for trainee: {}", traineeUsername);
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery(
                        """
                        FROM Trainer t JOIN FETCH t.user
                        WHERE t.user.isActive = true
                        AND t NOT IN (
                            SELECT tr FROM Trainee te JOIN te.trainers tr
                            WHERE te.user.username = :username
                        )
                        """, Trainer.class)
                .setParameter("username", traineeUsername)
                .list();
    }
}