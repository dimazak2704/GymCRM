package com.dimazak.service;

import com.dimazak.gym.dao.TrainingDao;
import com.dimazak.gym.model.Training;
import com.dimazak.gym.service.TrainingService;
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

    private static final Long TRAINING_ID = 1L;
    private static final Long NON_EXISTENT_ID = 99L;
    private static final Long TRAINEE_ID = 1L;
    private static final Long TRAINER_ID = 1L;
    private static final Long TRAINING_TYPE_ID = 1L;
    private static final String TRAINING_NAME = "Cardio Session";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 4, 1);
    private static final int DURATION_60 = 60;
    private static final int DURATION_30 = 30;

    @Mock private TrainingDao trainingDao;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void createTraining_shouldSaveAndReturn() {
        when(trainingDao.save(any(Training.class))).thenAnswer(inv -> {
            Training t = inv.getArgument(0);
            t.setId(TRAINING_ID);
            return t;
        });

        Training result = trainingService.createTraining(TRAINEE_ID, TRAINER_ID,
                TRAINING_NAME, TRAINING_TYPE_ID, TRAINING_DATE, DURATION_60);

        assertNotNull(result);
        assertEquals(TRAINING_ID, result.getId());
        assertEquals(TRAINING_NAME, result.getTrainingName());
        verify(trainingDao).save(any(Training.class));
    }

    @Test
    void selectTraining_shouldReturnWhenExists() {
        Training training = new Training(TRAINING_ID, TRAINEE_ID, TRAINER_ID,
                TRAINING_NAME, TRAINING_TYPE_ID, TRAINING_DATE, DURATION_30);
        when(trainingDao.findById(TRAINING_ID)).thenReturn(Optional.of(training));

        assertTrue(trainingService.selectTraining(TRAINING_ID).isPresent());
    }

    @Test
    void selectTraining_shouldReturnEmptyWhenNotFound() {
        when(trainingDao.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertTrue(trainingService.selectTraining(NON_EXISTENT_ID).isEmpty());
    }
}