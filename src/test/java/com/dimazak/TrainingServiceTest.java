package com.dimazak;

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

    @Mock private TrainingDao trainingDao;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void createTraining_shouldSaveAndReturn() {
        when(trainingDao.save(any(Training.class))).thenAnswer(inv -> {
            Training t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        Training result = trainingService.createTraining(1L, 1L, "Cardio Session",
                1L, LocalDate.of(2024, 4, 1), 60);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Cardio Session", result.getTrainingName());
        verify(trainingDao).save(any(Training.class));
    }

    @Test
    void selectTraining_shouldReturnWhenExists() {
        Training training = new Training(1L, 1L, 1L, "Test", 1L, LocalDate.now(), 30);
        when(trainingDao.findById(1L)).thenReturn(Optional.of(training));

        assertTrue(trainingService.selectTraining(1L).isPresent());
    }

    @Test
    void selectTraining_shouldReturnEmptyWhenNotFound() {
        when(trainingDao.findById(99L)).thenReturn(Optional.empty());

        assertTrue(trainingService.selectTraining(99L).isEmpty());
    }
}