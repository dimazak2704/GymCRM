package com.dimazak.gym.facade;

import com.dimazak.gym.exception.AuthenticationException;
import com.dimazak.gym.model.*;
import com.dimazak.gym.service.AuthenticationService;
import com.dimazak.gym.service.TraineeService;
import com.dimazak.gym.service.TrainerService;
import com.dimazak.gym.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    private static final String USERNAME = "John.Doe";
    private static final String PASSWORD = "pass123456";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 1, 1);
    private static final String ADDRESS = "123 Main St";

    private static final String TRAINER_USERNAME = "Jane.Smith";
    private static final String TRAINER_PASSWORD = "trainerPass1";
    private static final String TRAINER_FIRST_NAME = "Jane";
    private static final String TRAINER_LAST_NAME = "Smith";
    private static final Long SPECIALIZATION_ID = 1L;

    @Mock private TraineeService traineeService;
    @Mock private TrainerService trainerService;
    @Mock private TrainingService trainingService;
    @Mock private AuthenticationService authenticationService;

    @InjectMocks
    private GymFacade facade;

    // ==================== Registration ====================

    @Test
    void createTrainee_shouldNotRequireAuth() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        Trainee expected = new Trainee(1L, BIRTH_DATE, ADDRESS, user);
        when(traineeService.createTrainee(FIRST_NAME, LAST_NAME, BIRTH_DATE, ADDRESS))
                .thenReturn(expected);

        Trainee result = facade.createTrainee(FIRST_NAME, LAST_NAME, BIRTH_DATE, ADDRESS);

        assertEquals(expected, result);
        verifyNoInteractions(authenticationService);
    }

    @Test
    void createTrainer_shouldNotRequireAuth() {
        User user = new User(1L, TRAINER_FIRST_NAME, TRAINER_LAST_NAME, TRAINER_USERNAME, "pass", true);
        TrainingType type = new TrainingType(SPECIALIZATION_ID, "Cardio");
        Trainer expected = new Trainer(1L, type, user);
        when(trainerService.createTrainer(TRAINER_FIRST_NAME, TRAINER_LAST_NAME, SPECIALIZATION_ID))
                .thenReturn(expected);

        Trainer result = facade.createTrainer(TRAINER_FIRST_NAME, TRAINER_LAST_NAME, SPECIALIZATION_ID);

        assertEquals(expected, result);
        verifyNoInteractions(authenticationService);
    }

    // ==================== Credentials Matching ====================

    @Test
    void matchTraineeCredentials_shouldReturnTrue() {
        when(traineeService.matchCredentials(USERNAME, PASSWORD)).thenReturn(true);

        assertTrue(facade.matchTraineeCredentials(USERNAME, PASSWORD));
        verifyNoInteractions(authenticationService);
    }

    @Test
    void matchTraineeCredentials_shouldReturnFalse() {
        when(traineeService.matchCredentials(USERNAME, "wrong")).thenReturn(false);

        assertFalse(facade.matchTraineeCredentials(USERNAME, "wrong"));
    }

    @Test
    void matchTrainerCredentials_shouldReturnTrue() {
        when(trainerService.matchCredentials(TRAINER_USERNAME, TRAINER_PASSWORD)).thenReturn(true);

        assertTrue(facade.matchTrainerCredentials(TRAINER_USERNAME, TRAINER_PASSWORD));
        verifyNoInteractions(authenticationService);
    }

    @Test
    void matchTrainerCredentials_shouldReturnFalse() {
        when(trainerService.matchCredentials(TRAINER_USERNAME, "wrong")).thenReturn(false);

        assertFalse(facade.matchTrainerCredentials(TRAINER_USERNAME, "wrong"));
    }

    // ==================== Trainee Operations ====================

    @Test
    void getTraineeByUsername_shouldRequireAuth() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        Trainee expected = new Trainee(1L, BIRTH_DATE, ADDRESS, user);
        when(traineeService.getByUsername(USERNAME)).thenReturn(expected);

        Trainee result = facade.getTraineeByUsername(USERNAME, PASSWORD);

        assertEquals(expected, result);
        verify(authenticationService).authenticate(USERNAME, PASSWORD);
    }

    @Test
    void getTraineeByUsername_shouldThrowWhenAuthFails() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService).authenticate(USERNAME, "wrong");

        assertThrows(AuthenticationException.class,
                () -> facade.getTraineeByUsername(USERNAME, "wrong"));
        verify(traineeService, never()).getByUsername(any());
    }

    @Test
    void changeTraineePassword_shouldAuthenticateWithOldPassword() {
        facade.changeTraineePassword(USERNAME, PASSWORD, "newPass");

        verify(authenticationService).authenticate(USERNAME, PASSWORD);
        verify(traineeService).changePassword(USERNAME, "newPass");
    }

    @Test
    void changeTraineePassword_shouldThrowWhenAuthFails() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService).authenticate(USERNAME, "wrong");

        assertThrows(AuthenticationException.class,
                () -> facade.changeTraineePassword(USERNAME, "wrong", "newPass"));
        verify(traineeService, never()).changePassword(any(), any());
    }

    @Test
    void updateTrainee_shouldRequireAuth() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        Trainee expected = new Trainee(1L, BIRTH_DATE, ADDRESS, user);
        when(traineeService.updateTrainee(USERNAME, FIRST_NAME, LAST_NAME,
                BIRTH_DATE, ADDRESS, true)).thenReturn(expected);

        Trainee result = facade.updateTrainee(USERNAME, PASSWORD,
                FIRST_NAME, LAST_NAME, BIRTH_DATE, ADDRESS, true);

        assertEquals(expected, result);
        verify(authenticationService).authenticate(USERNAME, PASSWORD);
    }

    @Test
    void updateTrainee_shouldThrowWhenAuthFails() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService).authenticate(USERNAME, "wrong");

        assertThrows(AuthenticationException.class,
                () -> facade.updateTrainee(USERNAME, "wrong",
                        FIRST_NAME, LAST_NAME, BIRTH_DATE, ADDRESS, true));
        verify(traineeService, never()).updateTrainee(any(), any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void activateTrainee_shouldRequireAuth() {
        facade.activateTrainee(USERNAME, PASSWORD);

        verify(authenticationService).authenticate(USERNAME, PASSWORD);
        verify(traineeService).setActiveStatus(USERNAME, true);
    }

    @Test
    void activateTrainee_shouldThrowWhenAuthFails() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService).authenticate(USERNAME, "wrong");

        assertThrows(AuthenticationException.class,
                () -> facade.activateTrainee(USERNAME, "wrong"));
        verify(traineeService, never()).setActiveStatus(any(), anyBoolean());
    }

    @Test
    void deactivateTrainee_shouldRequireAuth() {
        facade.deactivateTrainee(USERNAME, PASSWORD);

        verify(authenticationService).authenticate(USERNAME, PASSWORD);
        verify(traineeService).setActiveStatus(USERNAME, false);
    }

    @Test
    void deactivateTrainee_shouldThrowWhenAuthFails() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService).authenticate(USERNAME, "wrong");

        assertThrows(AuthenticationException.class,
                () -> facade.deactivateTrainee(USERNAME, "wrong"));
        verify(traineeService, never()).setActiveStatus(any(), anyBoolean());
    }

    @Test
    void deleteTrainee_shouldRequireAuth() {
        facade.deleteTrainee(USERNAME, PASSWORD);

        verify(authenticationService).authenticate(USERNAME, PASSWORD);
        verify(traineeService).deleteByUsername(USERNAME);
    }

    @Test
    void deleteTrainee_shouldThrowWhenAuthFails() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService).authenticate(USERNAME, "wrong");

        assertThrows(AuthenticationException.class,
                () -> facade.deleteTrainee(USERNAME, "wrong"));
        verify(traineeService, never()).deleteByUsername(any());
    }

    @Test
    void getTraineeTrainings_shouldRequireAuth() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        List<Training> expected = List.of();
        when(traineeService.getTraineeTrainings(USERNAME, from, to, "Bob", "Cardio"))
                .thenReturn(expected);

        List<Training> result = facade.getTraineeTrainings(
                USERNAME, PASSWORD, from, to, "Bob", "Cardio");

        assertEquals(expected, result);
        verify(authenticationService).authenticate(USERNAME, PASSWORD);
    }

    @Test
    void getTraineeTrainings_shouldThrowWhenAuthFails() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService).authenticate(USERNAME, "wrong");

        assertThrows(AuthenticationException.class,
                () -> facade.getTraineeTrainings(USERNAME, "wrong", null, null, null, null));
        verify(traineeService, never()).getTraineeTrainings(any(), any(), any(), any(), any());
    }

    @Test
    void getTraineeTrainings_shouldPassNullCriteria() {
        when(traineeService.getTraineeTrainings(USERNAME, null, null, null, null))
                .thenReturn(List.of());

        List<Training> result = facade.getTraineeTrainings(
                USERNAME, PASSWORD, null, null, null, null);

        assertTrue(result.isEmpty());
        verify(authenticationService).authenticate(USERNAME, PASSWORD);
    }

    @Test
    void getUnassignedTrainers_shouldRequireAuth() {
        when(traineeService.getUnassignedTrainers(USERNAME)).thenReturn(List.of());

        List<Trainer> result = facade.getUnassignedTrainers(USERNAME, PASSWORD);

        assertTrue(result.isEmpty());
        verify(authenticationService).authenticate(USERNAME, PASSWORD);
    }

    @Test
    void getUnassignedTrainers_shouldThrowWhenAuthFails() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService).authenticate(USERNAME, "wrong");

        assertThrows(AuthenticationException.class,
                () -> facade.getUnassignedTrainers(USERNAME, "wrong"));
        verify(traineeService, never()).getUnassignedTrainers(any());
    }

    @Test
    void updateTraineeTrainers_shouldRequireAuth() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        Trainee expected = new Trainee(1L, BIRTH_DATE, ADDRESS, user);
        when(traineeService.updateTrainersList(USERNAME, List.of(TRAINER_USERNAME)))
                .thenReturn(expected);

        Trainee result = facade.updateTraineeTrainers(USERNAME, PASSWORD, List.of(TRAINER_USERNAME));

        assertEquals(expected, result);
        verify(authenticationService).authenticate(USERNAME, PASSWORD);
    }

    @Test
    void updateTraineeTrainers_shouldThrowWhenAuthFails() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService).authenticate(USERNAME, "wrong");

        assertThrows(AuthenticationException.class,
                () -> facade.updateTraineeTrainers(USERNAME, "wrong", List.of(TRAINER_USERNAME)));
        verify(traineeService, never()).updateTrainersList(any(), any());
    }

    // ==================== Trainer Operations ====================

    @Test
    void getTrainerByUsername_shouldRequireAuth() {
        User user = new User(2L, TRAINER_FIRST_NAME, TRAINER_LAST_NAME, TRAINER_USERNAME, TRAINER_PASSWORD, true);
        TrainingType type = new TrainingType(SPECIALIZATION_ID, "Cardio");
        Trainer expected = new Trainer(1L, type, user);
        when(trainerService.getByUsername(TRAINER_USERNAME)).thenReturn(expected);

        Trainer result = facade.getTrainerByUsername(TRAINER_USERNAME, TRAINER_PASSWORD);

        assertEquals(expected, result);
        verify(authenticationService).authenticate(TRAINER_USERNAME, TRAINER_PASSWORD);
    }

    @Test
    void getTrainerByUsername_shouldThrowWhenAuthFails() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService).authenticate(TRAINER_USERNAME, "wrong");

        assertThrows(AuthenticationException.class,
                () -> facade.getTrainerByUsername(TRAINER_USERNAME, "wrong"));
        verify(trainerService, never()).getByUsername(any());
    }

    @Test
    void changeTrainerPassword_shouldAuthenticateWithOldPassword() {
        facade.changeTrainerPassword(TRAINER_USERNAME, TRAINER_PASSWORD, "newPass");

        verify(authenticationService).authenticate(TRAINER_USERNAME, TRAINER_PASSWORD);
        verify(trainerService).changePassword(TRAINER_USERNAME, "newPass");
    }

    @Test
    void changeTrainerPassword_shouldThrowWhenAuthFails() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService).authenticate(TRAINER_USERNAME, "wrong");

        assertThrows(AuthenticationException.class,
                () -> facade.changeTrainerPassword(TRAINER_USERNAME, "wrong", "newPass"));
        verify(trainerService, never()).changePassword(any(), any());
    }

    @Test
    void updateTrainer_shouldRequireAuth() {
        User user = new User(2L, "Robert", TRAINER_LAST_NAME, TRAINER_USERNAME, TRAINER_PASSWORD, true);
        TrainingType newType = new TrainingType(2L, "Strength");
        Trainer expected = new Trainer(1L, newType, user);
        when(trainerService.updateTrainer(TRAINER_USERNAME, "Robert", TRAINER_LAST_NAME, 2L, true))
                .thenReturn(expected);

        Trainer result = facade.updateTrainer(TRAINER_USERNAME, TRAINER_PASSWORD,
                "Robert", TRAINER_LAST_NAME, 2L, true);

        assertEquals(expected, result);
        verify(authenticationService).authenticate(TRAINER_USERNAME, TRAINER_PASSWORD);
    }

    @Test
    void updateTrainer_shouldThrowWhenAuthFails() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService).authenticate(TRAINER_USERNAME, "wrong");

        assertThrows(AuthenticationException.class,
                () -> facade.updateTrainer(TRAINER_USERNAME, "wrong",
                        "Robert", TRAINER_LAST_NAME, 2L, true));
        verify(trainerService, never()).updateTrainer(any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void activateTrainer_shouldRequireAuth() {
        facade.activateTrainer(TRAINER_USERNAME, TRAINER_PASSWORD);

        verify(authenticationService).authenticate(TRAINER_USERNAME, TRAINER_PASSWORD);
        verify(trainerService).setActiveStatus(TRAINER_USERNAME, true);
    }

    @Test
    void activateTrainer_shouldThrowWhenAuthFails() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService).authenticate(TRAINER_USERNAME, "wrong");

        assertThrows(AuthenticationException.class,
                () -> facade.activateTrainer(TRAINER_USERNAME, "wrong"));
        verify(trainerService, never()).setActiveStatus(any(), anyBoolean());
    }

    @Test
    void deactivateTrainer_shouldRequireAuth() {
        facade.deactivateTrainer(TRAINER_USERNAME, TRAINER_PASSWORD);

        verify(authenticationService).authenticate(TRAINER_USERNAME, TRAINER_PASSWORD);
        verify(trainerService).setActiveStatus(TRAINER_USERNAME, false);
    }

    @Test
    void deactivateTrainer_shouldThrowWhenAuthFails() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService).authenticate(TRAINER_USERNAME, "wrong");

        assertThrows(AuthenticationException.class,
                () -> facade.deactivateTrainer(TRAINER_USERNAME, "wrong"));
        verify(trainerService, never()).setActiveStatus(any(), anyBoolean());
    }

    @Test
    void getTrainerTrainings_shouldRequireAuth() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        List<Training> expected = List.of();
        when(trainerService.getTrainerTrainings(TRAINER_USERNAME, from, to, "John"))
                .thenReturn(expected);

        List<Training> result = facade.getTrainerTrainings(
                TRAINER_USERNAME, TRAINER_PASSWORD, from, to, "John");

        assertEquals(expected, result);
        verify(authenticationService).authenticate(TRAINER_USERNAME, TRAINER_PASSWORD);
    }

    @Test
    void getTrainerTrainings_shouldThrowWhenAuthFails() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService).authenticate(TRAINER_USERNAME, "wrong");

        assertThrows(AuthenticationException.class,
                () -> facade.getTrainerTrainings(TRAINER_USERNAME, "wrong", null, null, null));
        verify(trainerService, never()).getTrainerTrainings(any(), any(), any(), any());
    }

    @Test
    void getTrainerTrainings_shouldPassNullCriteria() {
        when(trainerService.getTrainerTrainings(TRAINER_USERNAME, null, null, null))
                .thenReturn(List.of());

        List<Training> result = facade.getTrainerTrainings(
                TRAINER_USERNAME, TRAINER_PASSWORD, null, null, null);

        assertTrue(result.isEmpty());
        verify(authenticationService).authenticate(TRAINER_USERNAME, TRAINER_PASSWORD);
    }

    // ==================== Training Operations ====================

    @Test
    void addTraining_shouldRequireAuth() {
        User traineeUser = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);
        Trainee trainee = new Trainee(1L, BIRTH_DATE, ADDRESS, traineeUser);
        User trainerUser = new User(2L, TRAINER_FIRST_NAME, TRAINER_LAST_NAME, TRAINER_USERNAME, "p", true);
        TrainingType type = new TrainingType(1L, "Cardio");
        Trainer trainer = new Trainer(1L, type, trainerUser);
        LocalDate trainingDate = LocalDate.of(2024, 4, 1);

        Training expected = new Training(1L, trainee, trainer,
                "Morning Cardio", type, trainingDate, 60);
        when(trainingService.addTraining(USERNAME, TRAINER_USERNAME,
                "Morning Cardio", 1L, trainingDate, 60)).thenReturn(expected);

        Training result = facade.addTraining(USERNAME, PASSWORD,
                USERNAME, TRAINER_USERNAME, "Morning Cardio", 1L, trainingDate, 60);

        assertEquals(expected, result);
        verify(authenticationService).authenticate(USERNAME, PASSWORD);
    }

    @Test
    void addTraining_shouldThrowWhenAuthFails() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authenticationService).authenticate(USERNAME, "wrong");

        assertThrows(AuthenticationException.class,
                () -> facade.addTraining(USERNAME, "wrong",
                        USERNAME, TRAINER_USERNAME, "Cardio", 1L,
                        LocalDate.of(2024, 4, 1), 60));
        verify(trainingService, never()).addTraining(any(), any(), any(), any(), any(), anyInt());
    }
}