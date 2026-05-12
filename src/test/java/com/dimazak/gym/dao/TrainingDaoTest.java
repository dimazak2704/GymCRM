package com.dimazak.gym.dao;

import com.dimazak.gym.config.TestConfig;
import com.dimazak.gym.model.*;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@Transactional
@Rollback
class TrainingDaoTest {

    @Autowired private TrainingDao trainingDao;
    @Autowired private TraineeDao traineeDao;
    @Autowired private TrainerDao trainerDao;
    @Autowired private SessionFactory sessionFactory;

    private TrainingType cardioType;
    private TrainingType yogaType;
    private Trainee trainee;
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        cardioType = new TrainingType(null, "Cardio");
        yogaType = new TrainingType(null, "Yoga");
        sessionFactory.getCurrentSession().persist(cardioType);
        sessionFactory.getCurrentSession().persist(yogaType);

        User traineeUser = new User(null, "John", "Doe", "John.Doe", "pass", true);
        trainee = new Trainee(null, LocalDate.of(1990, 1, 1), "Addr", traineeUser);
        trainee = traineeDao.save(trainee);

        User trainerUser = new User(null, "Jane", "Smith", "Jane.Smith", "pass", true);
        trainer = new Trainer(null, cardioType, trainerUser);
        trainer = trainerDao.save(trainer);
    }

    @Test
    void save_shouldPersistNewTraining() {
        Training training = new Training(null, trainee, trainer,
                "Morning Cardio", cardioType, LocalDate.of(2024, 4, 1), 60);

        Training saved = trainingDao.save(training);

        assertNotNull(saved.getId());
        assertEquals("Morning Cardio", saved.getTrainingName());
    }

    @Test
    void findById_shouldReturnTrainingWhenExists() {
        Training training = new Training(null, trainee, trainer,
                "Test", cardioType, LocalDate.of(2024, 4, 1), 45);
        Training saved = trainingDao.save(training);

        Optional<Training> found = trainingDao.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Test", found.get().getTrainingName());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        assertTrue(trainingDao.findById(999L).isEmpty());
    }

    @Test
    void findByTraineeUsername_shouldReturnAllTrainings() {
        trainingDao.save(new Training(null, trainee, trainer,
                "Session 1", cardioType, LocalDate.of(2024, 3, 1), 60));
        trainingDao.save(new Training(null, trainee, trainer,
                "Session 2", cardioType, LocalDate.of(2024, 4, 1), 45));

        List<Training> found = trainingDao.findByTraineeUsernameAndCriteria(
                "John.Doe", null, null, null, null);

        assertEquals(2, found.size());
    }

    @Test
    void findByTraineeUsername_shouldFilterByDateRange() {
        trainingDao.save(new Training(null, trainee, trainer,
                "March", cardioType, LocalDate.of(2024, 3, 15), 60));
        trainingDao.save(new Training(null, trainee, trainer,
                "April", cardioType, LocalDate.of(2024, 4, 15), 60));
        trainingDao.save(new Training(null, trainee, trainer,
                "May", cardioType, LocalDate.of(2024, 5, 15), 60));

        List<Training> found = trainingDao.findByTraineeUsernameAndCriteria(
                "John.Doe",
                LocalDate.of(2024, 4, 1),
                LocalDate.of(2024, 4, 30),
                null, null);

        assertEquals(1, found.size());
        assertEquals("April", found.get(0).getTrainingName());
    }

    @Test
    void findByTraineeUsername_shouldFilterByTrainerName() {
        // Create second trainer
        User trainerUser2 = new User(null, "Bob", "Jones", "Bob.Jones", "p", true);
        Trainer trainer2 = new Trainer(null, cardioType, trainerUser2);
        trainer2 = trainerDao.save(trainer2);

        trainingDao.save(new Training(null, trainee, trainer,
                "With Jane", cardioType, LocalDate.of(2024, 4, 1), 60));
        trainingDao.save(new Training(null, trainee, trainer2,
                "With Bob", cardioType, LocalDate.of(2024, 4, 2), 60));

        List<Training> found = trainingDao.findByTraineeUsernameAndCriteria(
                "John.Doe", null, null, "Bob", null);

        assertEquals(1, found.size());
        assertEquals("With Bob", found.get(0).getTrainingName());
    }

    @Test
    void findByTraineeUsername_shouldFilterByTrainingType() {
        trainingDao.save(new Training(null, trainee, trainer,
                "Cardio Session", cardioType, LocalDate.of(2024, 4, 1), 60));
        trainingDao.save(new Training(null, trainee, trainer,
                "Yoga Session", yogaType, LocalDate.of(2024, 4, 2), 45));

        List<Training> found = trainingDao.findByTraineeUsernameAndCriteria(
                "John.Doe", null, null, null, "Yoga");

        assertEquals(1, found.size());
        assertEquals("Yoga Session", found.get(0).getTrainingName());
    }

    @Test
    void findByTrainerUsername_shouldReturnAllTrainings() {
        trainingDao.save(new Training(null, trainee, trainer,
                "Session 1", cardioType, LocalDate.of(2024, 4, 1), 60));

        List<Training> found = trainingDao.findByTrainerUsernameAndCriteria(
                "Jane.Smith", null, null, null);

        assertEquals(1, found.size());
    }

    @Test
    void findByTrainerUsername_shouldFilterByTraineeName() {
        // Create second trainee
        User traineeUser2 = new User(null, "Alice", "Brown", "Alice.Brown", "p", true);
        Trainee trainee2 = new Trainee(null, null, null, traineeUser2);
        trainee2 = traineeDao.save(trainee2);

        trainingDao.save(new Training(null, trainee, trainer,
                "With John", cardioType, LocalDate.of(2024, 4, 1), 60));
        trainingDao.save(new Training(null, trainee2, trainer,
                "With Alice", cardioType, LocalDate.of(2024, 4, 2), 60));

        List<Training> found = trainingDao.findByTrainerUsernameAndCriteria(
                "Jane.Smith", null, null, "Alice");

        assertEquals(1, found.size());
        assertEquals("With Alice", found.get(0).getTrainingName());
    }

    @Test
    void findByTrainerUsername_shouldFilterByDateRange() {
        trainingDao.save(new Training(null, trainee, trainer,
                "Early", cardioType, LocalDate.of(2024, 1, 1), 60));
        trainingDao.save(new Training(null, trainee, trainer,
                "Late", cardioType, LocalDate.of(2024, 12, 1), 60));

        List<Training> found = trainingDao.findByTrainerUsernameAndCriteria(
                "Jane.Smith",
                LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 12, 31),
                null);

        assertEquals(1, found.size());
        assertEquals("Late", found.get(0).getTrainingName());
    }
}