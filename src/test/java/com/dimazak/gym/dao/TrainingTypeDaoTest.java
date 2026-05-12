package com.dimazak.gym.dao;

import com.dimazak.gym.config.TestConfig;
import com.dimazak.gym.model.TrainingType;
import org.hibernate.SessionFactory;
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
class TrainingTypeDaoTest {

    @Autowired
    private TrainingTypeDao trainingTypeDao;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    void findById_shouldReturnTypeWhenExists() {
        TrainingType type = new TrainingType(null, "Cardio");
        sessionFactory.getCurrentSession().persist(type);

        Optional<TrainingType> found = trainingTypeDao.findById(type.getId());

        assertTrue(found.isPresent());
        assertEquals("Cardio", found.get().getTrainingTypeName());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        assertTrue(trainingTypeDao.findById(999L).isEmpty());
    }

    @Test
    void findAll_shouldReturnAllTypes() {
        sessionFactory.getCurrentSession().persist(new TrainingType(null, "Cardio"));
        sessionFactory.getCurrentSession().persist(new TrainingType(null, "Strength"));
        sessionFactory.getCurrentSession().persist(new TrainingType(null, "Yoga"));

        List<TrainingType> all = trainingTypeDao.findAll();

        assertEquals(3, all.size());
    }
}