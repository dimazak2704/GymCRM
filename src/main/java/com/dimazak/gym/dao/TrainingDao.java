package com.dimazak.gym.dao;

import com.dimazak.gym.model.Training;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainingDao {

    private static final Logger log = LoggerFactory.getLogger(TrainingDao.class);

    private final SessionFactory sessionFactory;

    public TrainingDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Training save(Training training) {
        Session session = sessionFactory.getCurrentSession();
        if (training.getId() == null) {
            session.persist(training);
            log.debug("Persisted new training with id: {}", training.getId());
        } else {
            training = session.merge(training);
            log.debug("Merged training with id: {}", training.getId());
        }
        return training;
    }

    public Optional<Training> findById(Long id) {
        log.debug("Finding training by id: {}", id);
        Session session = sessionFactory.getCurrentSession();
        return Optional.ofNullable(session.get(Training.class, id));
    }

    public List<Training> findByTraineeUsernameAndCriteria(
            String traineeUsername, LocalDate fromDate, LocalDate toDate,
            String trainerName, String trainingTypeName) {

        log.debug("Finding trainings for trainee '{}' with criteria", traineeUsername);
        Session session = sessionFactory.getCurrentSession();

        StringBuilder hql = new StringBuilder(
                """
                FROM Training t
                JOIN FETCH t.trainee
                JOIN FETCH t.trainer
                JOIN FETCH t.trainingType
                WHERE t.trainee.user.username = :traineeUsername
                """);

        if (fromDate != null) {
            hql.append(" AND t.trainingDate >= :fromDate");
        }
        if (toDate != null) {
            hql.append(" AND t.trainingDate <= :toDate");
        }
        if (trainerName != null && !trainerName.isBlank()) {
            hql.append(" AND t.trainer.user.firstName LIKE :trainerName");
        }
        if (trainingTypeName != null && !trainingTypeName.isBlank()) {
            hql.append(" AND t.trainingType.trainingTypeName = :trainingTypeName");
        }

        Query<Training> query = session.createQuery(hql.toString(), Training.class);
        query.setParameter("traineeUsername", traineeUsername);

        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        if (trainerName != null && !trainerName.isBlank()) {
            query.setParameter("trainerName", "%" + trainerName + "%");
        }
        if (trainingTypeName != null && !trainingTypeName.isBlank()) {
            query.setParameter("trainingTypeName", trainingTypeName);
        }

        return query.list();
    }

    public List<Training> findByTrainerUsernameAndCriteria(
            String trainerUsername, LocalDate fromDate, LocalDate toDate,
            String traineeName) {

        log.debug("Finding trainings for trainer '{}' with criteria", trainerUsername);
        Session session = sessionFactory.getCurrentSession();

        StringBuilder hql = new StringBuilder(
                """
                FROM Training t
                JOIN FETCH t.trainee
                JOIN FETCH t.trainer
                JOIN FETCH t.trainingType
                WHERE t.trainer.user.username = :trainerUsername
                """);

        if (fromDate != null) {
            hql.append(" AND t.trainingDate >= :fromDate");
        }
        if (toDate != null) {
            hql.append(" AND t.trainingDate <= :toDate");
        }
        if (traineeName != null && !traineeName.isBlank()) {
            hql.append(" AND t.trainee.user.firstName LIKE :traineeName");
        }

        Query<Training> query = session.createQuery(hql.toString(), Training.class);
        query.setParameter("trainerUsername", trainerUsername);

        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        if (traineeName != null && !traineeName.isBlank()) {
            query.setParameter("traineeName", "%" + traineeName + "%");
        }

        return query.list();
    }
}