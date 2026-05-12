package com.dimazak.gym.service;

import com.dimazak.gym.dao.TrainerDao;
import com.dimazak.gym.dao.TrainingDao;
import com.dimazak.gym.dao.TrainingTypeDao;
import com.dimazak.gym.exception.EntityNotFoundException;
import com.dimazak.gym.exception.ValidationException;
import com.dimazak.gym.model.Trainer;
import com.dimazak.gym.model.TrainingType;
import com.dimazak.gym.model.User;
import com.dimazak.gym.util.PasswordGenerator;
import com.dimazak.gym.util.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    private static final String FIRST_NAME = "Jane";
    private static final String LAST_NAME = "Doe";
    private static final String USERNAME = "Jane.Doe";
    private static final String PASSWORD = "pass123456";
    private static final String NEW_PASSWORD = "newPass5678";
    private static final Long SPECIALIZATION_ID = 1L;
    private static final Long NEW_SPECIALIZATION_ID = 2L;

    @Mock private TrainerDao trainerDao;
    @Mock private TrainingDao trainingDao;
    @Mock private TrainingTypeDao trainingTypeDao;
    @Mock private UsernameGenerator usernameGenerator;
    @Mock private PasswordGenerator passwordGenerator;

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void createTrainer_shouldCreateSuccessfully() {
        TrainingType type = new TrainingType(SPECIALIZATION_ID, "Cardio");
        when(trainingTypeDao.findById(SPECIALIZATION_ID)).thenReturn(Optional.of(type));
        when(usernameGenerator.generateUsername(FIRST_NAME, LAST_NAME)).thenReturn(USERNAME);
        when(passwordGenerator.generatePassword()).thenReturn(PASSWORD);
        when(trainerDao.save(any(Trainer.class))).thenAnswer(inv -> {
            Trainer t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        Trainer result = trainerService.createTrainer(FIRST_NAME, LAST_NAME, SPECIALIZATION_ID);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(USERNAME, result.getUser().getUsername());
        assertEquals(type, result.getSpecialization());
    }

    @Test
    void createTrainer_shouldThrowWhenFirstNameIsNull() {
        assertThrows(ValidationException.class,
                () -> trainerService.createTrainer(null, LAST_NAME, SPECIALIZATION_ID));
    }

    @Test
    void createTrainer_shouldThrowWhenSpecializationNotFound() {
        when(trainingTypeDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> trainerService.createTrainer(FIRST_NAME, LAST_NAME, 99L));
    }

    @Test
    void matchCredentials_shouldReturnTrueForValidCredentials() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        TrainingType type = new TrainingType(SPECIALIZATION_ID, "Cardio");
        Trainer trainer = new Trainer(1L, type, user);
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));

        assertTrue(trainerService.matchCredentials(USERNAME, PASSWORD));
    }

    @Test
    void matchCredentials_shouldReturnFalseForInvalidPassword() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        TrainingType type = new TrainingType(SPECIALIZATION_ID, "Cardio");
        Trainer trainer = new Trainer(1L, type, user);
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));

        assertFalse(trainerService.matchCredentials(USERNAME, "wrong"));
    }

    @Test
    void getByUsername_shouldReturnTrainer() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        TrainingType type = new TrainingType(SPECIALIZATION_ID, "Cardio");
        Trainer trainer = new Trainer(1L, type, user);
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));

        Trainer result = trainerService.getByUsername(USERNAME);

        assertEquals(USERNAME, result.getUser().getUsername());
    }

    @Test
    void getByUsername_shouldThrowWhenNotFound() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> trainerService.getByUsername(USERNAME));
    }

    @Test
    void changePassword_shouldUpdatePassword() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        TrainingType type = new TrainingType(SPECIALIZATION_ID, "Cardio");
        Trainer trainer = new Trainer(1L, type, user);
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(trainerDao.save(any(Trainer.class))).thenReturn(trainer);

        trainerService.changePassword(USERNAME, NEW_PASSWORD);

        assertEquals(NEW_PASSWORD, trainer.getUser().getPassword());
    }

    @Test
    void changePassword_shouldThrowWhenPasswordIsNull() {
        assertThrows(ValidationException.class,
                () -> trainerService.changePassword(USERNAME, null));
    }

    @Test
    void updateTrainer_shouldUpdateAllFields() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        TrainingType oldType = new TrainingType(SPECIALIZATION_ID, "Cardio");
        TrainingType newType = new TrainingType(NEW_SPECIALIZATION_ID, "Strength");
        Trainer trainer = new Trainer(1L, oldType, user);

        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(trainingTypeDao.findById(NEW_SPECIALIZATION_ID)).thenReturn(Optional.of(newType));
        when(trainerDao.save(any(Trainer.class))).thenReturn(trainer);

        Trainer result = trainerService.updateTrainer(USERNAME, "Bob", "Smith",
                NEW_SPECIALIZATION_ID, false);

        assertEquals("Bob", result.getUser().getFirstName());
        assertEquals(newType, result.getSpecialization());
        assertFalse(result.getUser().isActive());
    }

    @Test
    void setActiveStatus_shouldActivate() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, false);
        TrainingType type = new TrainingType(SPECIALIZATION_ID, "Cardio");
        Trainer trainer = new Trainer(1L, type, user);
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(trainerDao.save(any(Trainer.class))).thenReturn(trainer);

        trainerService.setActiveStatus(USERNAME, true);

        assertTrue(trainer.getUser().isActive());
    }

    @Test
    void setActiveStatus_shouldThrowWhenAlreadySameStatus() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        TrainingType type = new TrainingType(SPECIALIZATION_ID, "Cardio");
        Trainer trainer = new Trainer(1L, type, user);
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));

        assertThrows(ValidationException.class,
                () -> trainerService.setActiveStatus(USERNAME, true));
    }
}