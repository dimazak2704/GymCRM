package com.dimazak.gym.service;

import com.dimazak.gym.dao.TraineeDao;
import com.dimazak.gym.dao.TrainerDao;
import com.dimazak.gym.dao.TrainingDao;
import com.dimazak.gym.exception.EntityNotFoundException;
import com.dimazak.gym.exception.ValidationException;
import com.dimazak.gym.metrics.GymMetrics;
import com.dimazak.gym.model.*;
import com.dimazak.gym.util.PasswordGenerator;
import com.dimazak.gym.util.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private static final String WRONG_PASSWORD = "wrongPassword";
    private static final String EMPTY_PASSWORD = "";
    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 1, 1);
    private static final String ADDRESS = "123 Main St";
    private static final String NEW_ADDRESS = "456 New St";
    private static final String TRAINER_USERNAME = "Bob.Smith";
    private static final String TRAINER_FIRST_NAME = "Bob";
    private static final String TRAINER_LAST_NAME = "Smith";
    private static final Long TRAINEE_ID = 1L;
    private static final Long TRAINER_ID = 1L;
    private static final Long SPECIALIZATION_ID = 1L;
    private static final String SPECIALIZATION = "Cardio";

    @Mock private TraineeDao traineeDao;
    @Mock private TrainerDao trainerDao;
    @Mock private TrainingDao trainingDao;
    @Mock private UsernameGenerator usernameGenerator;
    @Mock private PasswordGenerator passwordGenerator;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private GymMetrics gymMetrics;

    @InjectMocks
    private TraineeService traineeService;

    private Trainee createTestTrainee() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        return new Trainee(TRAINEE_ID, BIRTH_DATE, ADDRESS, user);
    }

    @Test
    void createTrainee_shouldCreateSuccessfully() {
        when(usernameGenerator.generateUsername(FIRST_NAME, LAST_NAME)).thenReturn(USERNAME);
        when(passwordGenerator.generatePassword()).thenReturn(PASSWORD);
        when(passwordEncoder.encode(PASSWORD)).thenReturn("$2a$10$encodedHash");
        when(traineeDao.save(any(Trainee.class))).thenAnswer(inv -> {
            Trainee t = inv.getArgument(0);
            t.setId(TRAINEE_ID);
            return t;
        });

        Trainee result = traineeService.createTrainee(FIRST_NAME, LAST_NAME, BIRTH_DATE, ADDRESS);

        assertNotNull(result);
        assertEquals(TRAINEE_ID, result.getId());
        assertEquals(USERNAME, result.getUser().getUsername());
        assertEquals(PASSWORD, result.getUser().getPassword());
        verify(passwordEncoder).encode(PASSWORD);
    }

    @Test
    void createTrainee_shouldThrowWhenFirstNameIsNull() {
        assertThrows(ValidationException.class,
                () -> traineeService.createTrainee(null, LAST_NAME, BIRTH_DATE, ADDRESS));
    }

    @Test
    void createTrainee_shouldThrowWhenFirstNameIsBlank() {
        assertThrows(ValidationException.class,
                () -> traineeService.createTrainee("  ", LAST_NAME, BIRTH_DATE, ADDRESS));
    }

    @Test
    void createTrainee_shouldThrowWhenLastNameIsBlank() {
        assertThrows(ValidationException.class,
                () -> traineeService.createTrainee(FIRST_NAME, "", BIRTH_DATE, ADDRESS));
    }

    @Test
    void existsByUsername_shouldReturnTrue() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(createTestTrainee()));

        assertTrue(traineeService.existsByUsername(USERNAME));
    }

    @Test
    void existsByUsername_shouldReturnFalse() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertFalse(traineeService.existsByUsername(USERNAME));
    }

    @Test
    void matchCredentials_shouldReturnTrueForValidCredentials() {
        Trainee trainee = createTestTrainee();
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches(PASSWORD, trainee.getUser().getPassword())).thenReturn(true);

        assertTrue(traineeService.matchCredentials(USERNAME, PASSWORD));
    }

    @Test
    void matchCredentials_shouldReturnFalseForInvalidPassword() {
        Trainee trainee = createTestTrainee();
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches(WRONG_PASSWORD, trainee.getUser().getPassword())).thenReturn(false);

        assertFalse(traineeService.matchCredentials(USERNAME, WRONG_PASSWORD));
    }

    @Test
    void matchCredentials_shouldReturnFalseForNonExistentUser() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertFalse(traineeService.matchCredentials(USERNAME, PASSWORD));
    }

    @Test
    void getByUsername_shouldReturnTrainee() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(createTestTrainee()));

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
    void changePassword_shouldThrowWhenPasswordTooShort() {
        assertThrows(ValidationException.class,
                () -> traineeService.changePassword(USERNAME, "short"));
    }

    @Test
    void changePassword_shouldUpdatePassword() {
        Trainee trainee = createTestTrainee();
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn("$2a$10$newHash");
        when(traineeDao.save(any(Trainee.class))).thenReturn(trainee);

        traineeService.changePassword(USERNAME, NEW_PASSWORD);

        assertEquals("$2a$10$newHash", trainee.getUser().getPassword());
        verify(passwordEncoder).encode(NEW_PASSWORD);
    }

    @Test
    void changePassword_shouldThrowWhenPasswordIsEmpty() {
        assertThrows(ValidationException.class,
                () -> traineeService.changePassword(USERNAME, EMPTY_PASSWORD));
    }

    @Test
    void changePassword_shouldThrowWhenPasswordIsNull() {
        assertThrows(ValidationException.class,
                () -> traineeService.changePassword(USERNAME, null));
    }

    @Test
    void updateTrainee_shouldUpdateAllFields() {
        Trainee trainee = createTestTrainee();
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
    void updateTrainee_shouldThrowWhenFirstNameBlank() {
        assertThrows(ValidationException.class,
                () -> traineeService.updateTrainee(USERNAME, "", LAST_NAME,
                        BIRTH_DATE, ADDRESS, true));
    }

    @Test
    void setActiveStatus_shouldDeactivate() {
        Trainee trainee = createTestTrainee();
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(traineeDao.save(any(Trainee.class))).thenReturn(trainee);

        traineeService.setActiveStatus(USERNAME, false);

        assertFalse(trainee.getUser().isActive());
    }

    @Test
    void setActiveStatus_shouldThrowWhenAlreadySameStatus() {
        Trainee trainee = createTestTrainee();
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        assertThrows(ValidationException.class,
                () -> traineeService.setActiveStatus(USERNAME, true));
    }

    @Test
    void deleteByUsername_shouldDeleteTrainee() {
        Trainee trainee = createTestTrainee();
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        traineeService.deleteByUsername(USERNAME);

        verify(traineeDao).delete(trainee);
    }

    @Test
    void deleteByUsername_shouldThrowWhenNotFound() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> traineeService.deleteByUsername(USERNAME));
    }

    @Test
    void getTraineeTrainings_shouldDelegateToDao() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(createTestTrainee()));
        when(trainingDao.findByTraineeWithFilters(USERNAME, null, null, null, null))
                .thenReturn(List.of());

        List<Training> result = traineeService.getTraineeTrainings(
                USERNAME, null, null, null, null);

        assertTrue(result.isEmpty());
        verify(trainingDao).findByTraineeWithFilters(USERNAME, null, null, null, null);
    }

    @Test
    void getUnassignedTrainers_shouldDelegateToDao() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(createTestTrainee()));
        when(trainerDao.findUnassignedByTraineeUsername(USERNAME)).thenReturn(List.of());

        List<Trainer> result = traineeService.getUnassignedTrainers(USERNAME);

        assertTrue(result.isEmpty());
    }

    @Test
    void updateTrainersList_shouldUpdateTrainers() {
        Trainee trainee = createTestTrainee();
        User trainerUser = new User(2L, TRAINER_FIRST_NAME, TRAINER_LAST_NAME, TRAINER_USERNAME, "pass", true);
        TrainingType type = new TrainingType(SPECIALIZATION_ID, SPECIALIZATION);
        Trainer trainer = new Trainer(TRAINER_ID, type, trainerUser);

        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));
        when(traineeDao.save(any(Trainee.class))).thenReturn(trainee);

        Trainee result = traineeService.updateTrainersList(USERNAME, List.of(TRAINER_USERNAME));

        assertEquals(1, result.getTrainers().size());
    }

    @Test
    void updateTrainersList_shouldThrowWhenTrainerNotFound() {
        Trainee trainee = createTestTrainee();
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername("NonExistent")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> traineeService.updateTrainersList(USERNAME, List.of("NonExistent")));
    }
}