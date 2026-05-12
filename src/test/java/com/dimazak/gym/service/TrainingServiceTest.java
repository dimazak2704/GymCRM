package com.dimazak.gym.service;

import com.dimazak.gym.dao.TraineeDao;
import com.dimazak.gym.dao.TrainerDao;
import com.dimazak.gym.dao.TrainingDao;
import com.dimazak.gym.dao.TrainingTypeDao;
import com.dimazak.gym.exception.EntityNotFoundException;
import com.dimazak.gym.exception.ValidationException;
import com.dimazak.gym.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    private static final String TRAINEE_USERNAME = "John.Doe";
    private static final String TRAINER_USERNAME = "Jane.Smith";
    private static final String TRAINING_NAME = "Morning Cardio";
    private static final Long TRAINING_TYPE_ID = 1L;
    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 4, 1);
    private static final int DURATION = 60;

    @Mock private TrainingDao trainingDao;
    @Mock private TraineeDao traineeDao;
    @Mock private TrainerDao trainerDao;
    @Mock private TrainingTypeDao trainingTypeDao;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void addTraining_shouldCreateSuccessfully() {
        User traineeUser = new User(1L, "John", "Doe", TRAINEE_USERNAME, "pass", true);
        Trainee trainee = new Trainee(1L, LocalDate.of(1990, 1, 1), "Addr", traineeUser);

        User trainerUser = new User(2L, "Jane", "Smith", TRAINER_USERNAME, "pass", true);
        TrainingType type = new TrainingType(TRAINING_TYPE_ID, "Cardio");
        Trainer trainer = new Trainer(1L, type, trainerUser);

        when(traineeDao.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));
        when(trainingTypeDao.findById(TRAINING_TYPE_ID)).thenReturn(Optional.of(type));
        when(trainingDao.save(any(Training.class))).thenAnswer(inv -> {
            Training t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        Training result = trainingService.addTraining(TRAINEE_USERNAME, TRAINER_USERNAME,
                TRAINING_NAME, TRAINING_TYPE_ID, TRAINING_DATE, DURATION);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(TRAINING_NAME, result.getTrainingName());
        assertEquals(trainee, result.getTrainee());
        assertEquals(trainer, result.getTrainer());
    }

    @Test
    void addTraining_shouldThrowWhenTraineeNotFound() {
        when(traineeDao.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> trainingService.addTraining(TRAINEE_USERNAME, TRAINER_USERNAME,
                        TRAINING_NAME, TRAINING_TYPE_ID, TRAINING_DATE, DURATION));
    }

    @Test
    void addTraining_shouldThrowWhenTrainerNotFound() {
        User traineeUser = new User(1L, "John", "Doe", TRAINEE_USERNAME, "pass", true);
        Trainee trainee = new Trainee(1L, LocalDate.of(1990, 1, 1), "Addr", traineeUser);
        when(traineeDao.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> trainingService.addTraining(TRAINEE_USERNAME, TRAINER_USERNAME,
                        TRAINING_NAME, TRAINING_TYPE_ID, TRAINING_DATE, DURATION));
    }

    @Test
    void addTraining_shouldThrowWhenNameIsBlank() {
        assertThrows(ValidationException.class,
                () -> trainingService.addTraining(TRAINEE_USERNAME, TRAINER_USERNAME,
                        "", TRAINING_TYPE_ID, TRAINING_DATE, DURATION));
    }

    @Test
    void addTraining_shouldThrowWhenDurationIsNegative() {
        assertThrows(ValidationException.class,
                () -> trainingService.addTraining(TRAINEE_USERNAME, TRAINER_USERNAME,
                        TRAINING_NAME, TRAINING_TYPE_ID, TRAINING_DATE, -1));
    }

    @Test
    void addTraining_shouldThrowWhenDateIsNull() {
        assertThrows(ValidationException.class,
                () -> trainingService.addTraining(TRAINEE_USERNAME, TRAINER_USERNAME,
                        TRAINING_NAME, TRAINING_TYPE_ID, null, DURATION));
    }
}