package com.dimazak.gym.dao;

import com.dimazak.gym.config.TestConfig;
import com.dimazak.gym.model.Trainee;
import com.dimazak.gym.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@Transactional
@Rollback
class TraineeDaoTest {

    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 5, 15);
    private static final String ADDRESS = "123 Main St";

    @Autowired
    private TraineeDao traineeDao;

    private Trainee createAndSaveTrainee(String firstName, String lastName, String username) {
        User user = new User(null, firstName, lastName, username, "pass123456", true);
        Trainee trainee = new Trainee(null, BIRTH_DATE, ADDRESS, user);
        return traineeDao.save(trainee);
    }

    @Test
    void save_shouldPersistNewTrainee() {
        Trainee saved = createAndSaveTrainee("Alice", "Johnson", "Alice.Johnson");

        assertNotNull(saved.getId());
        assertNotNull(saved.getUser().getId());
        assertEquals(BIRTH_DATE, saved.getDateOfBirth());
        assertEquals(ADDRESS, saved.getAddress());
    }

    @Test
    void save_shouldMergeExistingTrainee() {
        Trainee saved = createAndSaveTrainee("Alice", "Johnson", "Alice.Johnson");

        saved.setAddress("New Address");
        Trainee merged = traineeDao.save(saved);

        assertEquals(saved.getId(), merged.getId());
        assertEquals("New Address", merged.getAddress());
    }

    @Test
    void findById_shouldReturnTraineeWhenExists() {
        Trainee saved = createAndSaveTrainee("Bob", "Smith", "Bob.Smith");

        Optional<Trainee> found = traineeDao.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        assertTrue(traineeDao.findById(999L).isEmpty());
    }

    @Test
    void findByUsername_shouldReturnTraineeWhenExists() {
        createAndSaveTrainee("Jane", "Doe", "Jane.Doe");

        Optional<Trainee> found = traineeDao.findByUsername("Jane.Doe");

        assertTrue(found.isPresent());
        assertEquals("Jane.Doe", found.get().getUser().getUsername());
        assertEquals(BIRTH_DATE, found.get().getDateOfBirth());
    }

    @Test
    void findByUsername_shouldReturnEmptyWhenNotExists() {
        assertTrue(traineeDao.findByUsername("NonExistent").isEmpty());
    }

    @Test
    void delete_shouldRemoveTraineeAndUser() {
        Trainee saved = createAndSaveTrainee("Delete", "Me", "Delete.Me");
        Long traineeId = saved.getId();

        traineeDao.delete(saved);

        assertTrue(traineeDao.findById(traineeId).isEmpty());
    }

    @Test
    void save_shouldAllowNullOptionalFields() {
        User user = new User(null, "Min", "Data", "Min.Data", "pass123456", true);
        Trainee trainee = new Trainee(null, null, null, user);

        Trainee saved = traineeDao.save(trainee);

        assertNotNull(saved.getId());
        assertNull(saved.getDateOfBirth());
        assertNull(saved.getAddress());
    }
}