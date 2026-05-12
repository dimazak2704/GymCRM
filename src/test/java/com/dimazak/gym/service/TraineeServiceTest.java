package com.dimazak.gym.service;

import com.dimazak.gym.dao.TraineeDao;
import com.dimazak.gym.dao.TrainerDao;
import com.dimazak.gym.dao.TrainingDao;
import com.dimazak.gym.exception.EntityNotFoundException;
import com.dimazak.gym.exception.ValidationException;
import com.dimazak.gym.model.Trainee;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String USERNAME = "John.Doe";
    private static final String PASSWORD = "abc1234567";
    private static final String NEW_PASSWORD = "newPass1234";
    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 1, 1);
    private static final String ADDRESS = "123 Main St";
    private static final String NEW_ADDRESS = "456 New St";

    @Mock private TraineeDao traineeDao;
    @Mock private TrainerDao trainerDao;
    @Mock private TrainingDao trainingDao;
    @Mock private UsernameGenerator usernameGenerator;
    @Mock private PasswordGenerator passwordGenerator;

    @InjectMocks
    private TraineeService traineeService;

    @Test
    void createTrainee_shouldCreateSuccessfully() {
        when(usernameGenerator.generateUsername(FIRST_NAME, LAST_NAME)).thenReturn(USERNAME);
        when(passwordGenerator.generatePassword()).thenReturn(PASSWORD);
        when(traineeDao.save(any(Trainee.class))).thenAnswer(inv -> {
            Trainee t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        Trainee result = traineeService.createTrainee(FIRST_NAME, LAST_NAME,
                BIRTH_DATE, ADDRESS);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(USERNAME, result.getUser().getUsername());
        assertEquals(PASSWORD, result.getUser().getPassword());
        verify(traineeDao).save(any(Trainee.class));
    }

    @Test
    void createTrainee_shouldThrowWhenFirstNameIsNull() {
        assertThrows(ValidationException.class,
                () -> traineeService.createTrainee(null, LAST_NAME, BIRTH_DATE, ADDRESS));
    }

    @Test
    void createTrainee_shouldThrowWhenLastNameIsBlank() {
        assertThrows(ValidationException.class,
                () -> traineeService.createTrainee(FIRST_NAME, "", BIRTH_DATE, ADDRESS));
    }

    @Test
    void matchCredentials_shouldReturnTrueForValidCredentials() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        Trainee trainee = new Trainee(1L, BIRTH_DATE, ADDRESS, user);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        assertTrue(traineeService.matchCredentials(USERNAME, PASSWORD));
    }

    @Test
    void matchCredentials_shouldReturnFalseForInvalidPassword() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        Trainee trainee = new Trainee(1L, BIRTH_DATE, ADDRESS, user);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        assertFalse(traineeService.matchCredentials(USERNAME, "wrongPassword"));
    }

    @Test
    void matchCredentials_shouldReturnFalseForNonExistentUser() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertFalse(traineeService.matchCredentials(USERNAME, PASSWORD));
    }

    @Test
    void getByUsername_shouldReturnTrainee() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        Trainee trainee = new Trainee(1L, BIRTH_DATE, ADDRESS, user);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        Trainee result = traineeService.getByUsername(USERNAME);

        assertEquals(USERNAME, result.getUser().getUsername());
    }

    @Test
    void getByUsername_shouldThrowWhenNotFound() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> traineeService.getByUsername(USERNAME));
    }

    @Test
    void changePassword_shouldUpdatePassword() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        Trainee trainee = new Trainee(1L, BIRTH_DATE, ADDRESS, user);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(traineeDao.save(any(Trainee.class))).thenReturn(trainee);

        traineeService.changePassword(USERNAME, NEW_PASSWORD);

        assertEquals(NEW_PASSWORD, trainee.getUser().getPassword());
        verify(traineeDao).save(trainee);
    }

    @Test
    void changePassword_shouldThrowWhenPasswordIsEmpty() {
        assertThrows(ValidationException.class,
                () -> traineeService.changePassword(USERNAME, ""));
    }

    @Test
    void updateTrainee_shouldUpdateAllFields() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        Trainee trainee = new Trainee(1L, BIRTH_DATE, ADDRESS, user);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(traineeDao.save(any(Trainee.class))).thenReturn(trainee);

        Trainee result = traineeService.updateTrainee(USERNAME, "Jane", "Smith",
                BIRTH_DATE, NEW_ADDRESS, false);

        assertEquals("Jane", result.getUser().getFirstName());
        assertEquals("Smith", result.getUser().getLastName());
        assertEquals(NEW_ADDRESS, result.getAddress());
        assertFalse(result.getUser().isActive());
    }

    @Test
    void setActiveStatus_shouldDeactivate() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        Trainee trainee = new Trainee(1L, BIRTH_DATE, ADDRESS, user);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(traineeDao.save(any(Trainee.class))).thenReturn(trainee);

        traineeService.setActiveStatus(USERNAME, false);

        assertFalse(trainee.getUser().isActive());
    }

    @Test
    void setActiveStatus_shouldThrowWhenAlreadySameStatus() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        Trainee trainee = new Trainee(1L, BIRTH_DATE, ADDRESS, user);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        assertThrows(ValidationException.class,
                () -> traineeService.setActiveStatus(USERNAME, true));
    }

    @Test
    void deleteByUsername_shouldDeleteTrainee() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        Trainee trainee = new Trainee(1L, BIRTH_DATE, ADDRESS, user);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        traineeService.deleteByUsername(USERNAME);

        verify(traineeDao).delete(trainee);
    }

    @Test
    void updateTrainersList_shouldUpdateTrainers() {
        User traineeUser = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        Trainee trainee = new Trainee(1L, BIRTH_DATE, ADDRESS, traineeUser);

        User trainerUser = new User(2L, "Bob", "Smith", "Bob.Smith", "pass", true);
        TrainingType type = new TrainingType(1L, "Cardio");
        Trainer trainer = new Trainer(1L, type, trainerUser);

        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername("Bob.Smith")).thenReturn(Optional.of(trainer));
        when(traineeDao.save(any(Trainee.class))).thenReturn(trainee);

        Trainee result = traineeService.updateTrainersList(USERNAME, List.of("Bob.Smith"));

        assertEquals(1, result.getTrainers().size());
    }

    @Test
    void updateTrainersList_shouldThrowWhenTrainerNotFound() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        Trainee trainee = new Trainee(1L, BIRTH_DATE, ADDRESS, user);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername("NonExistent")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> traineeService.updateTrainersList(USERNAME, List.of("NonExistent")));
    }
}