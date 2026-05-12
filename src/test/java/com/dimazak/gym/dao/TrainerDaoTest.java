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
class TrainerDaoTest {

    @Autowired
    private TrainerDao trainerDao;

    @Autowired
    private TraineeDao traineeDao;

    @Autowired
    private SessionFactory sessionFactory;

    private TrainingType cardioType;

    @BeforeEach
    void setUp() {
        cardioType = new TrainingType(null, "Cardio");
        sessionFactory.getCurrentSession().persist(cardioType);
    }

    private Trainer createAndSaveTrainer(String firstName, String lastName, String username) {
        User user = new User(null, firstName, lastName, username, "pass123456", true);
        Trainer trainer = new Trainer(null, cardioType, user);
        return trainerDao.save(trainer);
    }

    @Test
    void save_shouldPersistNewTrainer() {
        Trainer saved = createAndSaveTrainer("Bob", "Williams", "Bob.Williams");

        assertNotNull(saved.getId());
        assertNotNull(saved.getUser().getId());
        assertEquals(cardioType, saved.getSpecialization());
    }

    @Test
    void save_shouldMergeExistingTrainer() {
        Trainer saved = createAndSaveTrainer("Bob", "Williams", "Bob.Williams");

        TrainingType newType = new TrainingType(null, "Strength");
        sessionFactory.getCurrentSession().persist(newType);
        saved.setSpecialization(newType);

        Trainer merged = trainerDao.save(saved);

        assertEquals(saved.getId(), merged.getId());
        assertEquals("Strength", merged.getSpecialization().getTrainingTypeName());
    }

    @Test
    void findById_shouldReturnTrainerWhenExists() {
        Trainer saved = createAndSaveTrainer("Jane", "Doe", "Jane.Doe");

        Optional<Trainer> found = trainerDao.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        assertTrue(trainerDao.findById(999L).isEmpty());
    }

    @Test
    void findByUsername_shouldReturnTrainerWhenExists() {
        createAndSaveTrainer("Mike", "Jones", "Mike.Jones");

        Optional<Trainer> found = trainerDao.findByUsername("Mike.Jones");

        assertTrue(found.isPresent());
        assertEquals("Mike.Jones", found.get().getUser().getUsername());
    }

    @Test
    void findByUsername_shouldReturnEmptyWhenNotExists() {
        assertTrue(trainerDao.findByUsername("NonExistent").isEmpty());
    }

    @Test
    void findUnassignedTrainers_shouldExcludeAssignedOnes() {
        // Create 2 trainers
        Trainer trainer1 = createAndSaveTrainer("T1", "Last", "T1.Last");
        Trainer trainer2 = createAndSaveTrainer("T2", "Last", "T2.Last");

        // Create trainee and assign trainer1
        User traineeUser = new User(null, "Trainee", "One", "Trainee.One", "pass", true);
        Trainee trainee = new Trainee(null, LocalDate.of(1990, 1, 1), "Addr", traineeUser);
        trainee.getTrainers().add(trainer1);
        traineeDao.save(trainee);

        // Flush to sync with DB
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        // Only trainer2 should be unassigned
        List<Trainer> unassigned =
                trainerDao.findUnassignedTrainersByTraineeUsername("Trainee.One");

        assertEquals(1, unassigned.size());
        assertEquals("T2.Last", unassigned.get(0).getUser().getUsername());
    }

    @Test
    void findUnassignedTrainers_shouldReturnAllWhenNoneAssigned() {
        createAndSaveTrainer("T1", "A", "T1.A");
        createAndSaveTrainer("T2", "B", "T2.B");

        User traineeUser = new User(null, "Solo", "Trainee", "Solo.Trainee", "pass", true);
        Trainee trainee = new Trainee(null, null, null, traineeUser);
        traineeDao.save(trainee);

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        List<Trainer> unassigned =
                trainerDao.findUnassignedTrainersByTraineeUsername("Solo.Trainee");

        assertEquals(2, unassigned.size());
    }

    @Test
    void findUnassignedTrainers_shouldExcludeInactiveTrainers() {
        createAndSaveTrainer("Active", "Trainer", "Active.Trainer");

        User inactiveUser = new User(null, "Inactive", "Trainer", "Inactive.Trainer", "p", false);
        Trainer inactive = new Trainer(null, cardioType, inactiveUser);
        trainerDao.save(inactive);

        User traineeUser = new User(null, "Test", "Trainee", "Test.Trainee", "pass", true);
        Trainee trainee = new Trainee(null, null, null, traineeUser);
        traineeDao.save(trainee);

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        List<Trainer> unassigned =
                trainerDao.findUnassignedTrainersByTraineeUsername("Test.Trainee");

        assertEquals(1, unassigned.size());
        assertEquals("Active.Trainer", unassigned.get(0).getUser().getUsername());
    }
}