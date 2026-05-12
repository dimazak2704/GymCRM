package com.dimazak.gym.dao;

import com.dimazak.gym.model.TrainingType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingTypeDao {

    private static final Logger log = LoggerFactory.getLogger(TrainingTypeDao.class);

    private final SessionFactory sessionFactory;

    public TrainingTypeDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Optional<TrainingType> findById(Long id) {
        log.debug("Finding training type by id: {}", id);
        Session session = sessionFactory.getCurrentSession();
        return Optional.ofNullable(session.get(TrainingType.class, id));
    }

    public List<TrainingType> findAll() {
        log.debug("Finding all training types");
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery("FROM TrainingType", TrainingType.class).list();
    }
}