package com.dimazak.gym.service;

import com.dimazak.gym.dao.TrainerDao;
import com.dimazak.gym.dao.TrainingDao;
import com.dimazak.gym.dao.TrainingTypeDao;
import com.dimazak.gym.exception.EntityNotFoundException;
import com.dimazak.gym.exception.ValidationException;
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
class TrainerServiceTest {

    private static final String FIRST_NAME = "Jane";
    private static final String LAST_NAME = "Doe";
    private static final String USERNAME = "Jane.Doe";
    private static final String PASSWORD = "pass123456";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedHash";
    private static final String NEW_PASSWORD = "newPass5678";
    private static final String ENCODED_NEW_PASSWORD = "$2a$10$newEncodedHash";
    private static final String WRONG_PASSWORD = "wrong";
    private static final String SHORT_PASSWORD = "short";
    private static final Long SPECIALIZATION_ID = 1L;
    private static final Long NEW_SPECIALIZATION_ID = 2L;
    private static final Long INVALID_SPECIALIZATION_ID = 99L;
    private static final String SPECIALIZATION = "Cardio";
    private static final String NEW_SPECIALIZATION = "Strength";
    private static final Long TRAINER_ID = 1L;

    @Mock private TrainerDao trainerDao;
    @Mock private TrainingDao trainingDao;
    @Mock private TrainingTypeDao trainingTypeDao;
    @Mock private UsernameGenerator usernameGenerator;
    @Mock private PasswordGenerator passwordGenerator;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TrainerService trainerService;

    private Trainer createTestTrainer() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, ENCODED_PASSWORD, true);
        TrainingType type = new TrainingType(SPECIALIZATION_ID, SPECIALIZATION);
        return new Trainer(TRAINER_ID, type, user);
    }

    @Test
    void createTrainer_shouldCreateSuccessfully() {
        TrainingType type = new TrainingType(SPECIALIZATION_ID, SPECIALIZATION);
        when(trainingTypeDao.findById(SPECIALIZATION_ID)).thenReturn(Optional.of(type));
        when(usernameGenerator.generateUsername(FIRST_NAME, LAST_NAME)).thenReturn(USERNAME);
        when(passwordGenerator.generatePassword()).thenReturn(PASSWORD);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(trainerDao.save(any(Trainer.class))).thenAnswer(inv -> {
            Trainer t = inv.getArgument(0);
            t.setId(TRAINER_ID);
            return t;
        });

        Trainer result = trainerService.createTrainer(FIRST_NAME, LAST_NAME, SPECIALIZATION_ID);

        assertNotNull(result);
        assertEquals(TRAINER_ID, result.getId());
        assertEquals(USERNAME, result.getUser().getUsername());
        assertEquals(PASSWORD, result.getUser().getPassword()); // raw повертається для response
        assertEquals(type, result.getSpecialization());
        verify(passwordEncoder).encode(PASSWORD);
    }

    @Test
    void createTrainer_shouldThrowWhenFirstNameIsNull() {
        assertThrows(ValidationException.class,
                () -> trainerService.createTrainer(null, LAST_NAME, SPECIALIZATION_ID));
    }

    @Test
    void createTrainer_shouldThrowWhenLastNameIsBlank() {
        assertThrows(ValidationException.class,
                () -> trainerService.createTrainer(FIRST_NAME, "", SPECIALIZATION_ID));
    }

    @Test
    void createTrainer_shouldThrowWhenSpecializationNull() {
        assertThrows(ValidationException.class,
                () -> trainerService.createTrainer(FIRST_NAME, LAST_NAME, null));
    }

    @Test
    void createTrainer_shouldThrowWhenSpecializationNotFound() {
        when(trainingTypeDao.findById(INVALID_SPECIALIZATION_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> trainerService.createTrainer(FIRST_NAME, LAST_NAME, INVALID_SPECIALIZATION_ID));
    }

    @Test
    void matchCredentials_shouldReturnTrueForValidCredentials() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(createTestTrainer()));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        assertTrue(trainerService.matchCredentials(USERNAME, PASSWORD));
    }

    @Test
    void matchCredentials_shouldReturnFalseForInvalidPassword() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(createTestTrainer()));
        when(passwordEncoder.matches(WRONG_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        assertFalse(trainerService.matchCredentials(USERNAME, WRONG_PASSWORD));
    }

    @Test
    void matchCredentials_shouldReturnFalseWhenNotFound() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertFalse(trainerService.matchCredentials(USERNAME, PASSWORD));
    }

    @Test
    void getByUsername_shouldReturnTrainer() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(createTestTrainer()));

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
        Trainer trainer = createTestTrainer();
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_NEW_PASSWORD);
        when(trainerDao.save(any(Trainer.class))).thenReturn(trainer);

        trainerService.changePassword(USERNAME, NEW_PASSWORD);

        assertEquals(ENCODED_NEW_PASSWORD, trainer.getUser().getPassword());
        verify(passwordEncoder).encode(NEW_PASSWORD);
    }

    @Test
    void changePassword_shouldThrowWhenPasswordIsNull() {
        assertThrows(ValidationException.class,
                () -> trainerService.changePassword(USERNAME, null));
    }

    @Test
    void changePassword_shouldThrowWhenPasswordIsBlank() {
        assertThrows(ValidationException.class,
                () -> trainerService.changePassword(USERNAME, "  "));
    }

    @Test
    void changePassword_shouldThrowWhenPasswordTooShort() {
        assertThrows(ValidationException.class,
                () -> trainerService.changePassword(USERNAME, SHORT_PASSWORD));
    }

    @Test
    void updateTrainer_shouldUpdateAllFields() {
        Trainer trainer = createTestTrainer();
        TrainingType newType = new TrainingType(NEW_SPECIALIZATION_ID, NEW_SPECIALIZATION);
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
        Trainer trainer = createTestTrainer();
        trainer.getUser().setActive(false);
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(trainerDao.save(any(Trainer.class))).thenReturn(trainer);

        trainerService.setActiveStatus(USERNAME, true);

        assertTrue(trainer.getUser().isActive());
    }

    @Test
    void setActiveStatus_shouldThrowWhenAlreadySameStatus() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(createTestTrainer()));

        assertThrows(ValidationException.class,
                () -> trainerService.setActiveStatus(USERNAME, true));
    }

    @Test
    void getTrainerTrainings_shouldDelegateToDao() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(createTestTrainer()));
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        when(trainingDao.findByTrainerWithFilters(USERNAME, from, to, null))
                .thenReturn(List.of());

        List<Training> result = trainerService.getTrainerTrainings(USERNAME, from, to, null);

        assertTrue(result.isEmpty());
        verify(trainingDao).findByTrainerWithFilters(USERNAME, from, to, null);
    }
}