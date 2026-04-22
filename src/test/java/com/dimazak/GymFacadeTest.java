package com.dimazak;

import com.dimazak.gym.facade.GymFacade;
import com.dimazak.gym.model.Trainee;
import com.dimazak.gym.model.Trainer;
import com.dimazak.gym.model.Training;
import com.dimazak.gym.service.TraineeService;
import com.dimazak.gym.service.TrainerService;
import com.dimazak.gym.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    @Mock private TraineeService traineeService;
    @Mock private TrainerService trainerService;
    @Mock private TrainingService trainingService;

    @InjectMocks
    private GymFacade facade;

    @Test
    void createTrainee_shouldDelegateToService() {
        Trainee expected = new Trainee(1L, LocalDate.now(), "Addr", 1L);
        when(traineeService.createTrainee("A", "B", LocalDate.now(), "Addr", true))
                .thenReturn(expected);

        Trainee result = facade.createTrainee("A", "B", LocalDate.now(), "Addr", true);

        assertEquals(expected, result);
    }

    @Test
    void deleteTrainee_shouldDelegateToService() {
        facade.deleteTrainee(1L);
        verify(traineeService).deleteTrainee(1L);
    }

    @Test
    void createTrainer_shouldDelegateToService() {
        Trainer expected = new Trainer(1L, 1L, 1L);
        when(trainerService.createTrainer("A", "B", 1L, true)).thenReturn(expected);

        Trainer result = facade.createTrainer("A", "B", 1L, true);

        assertEquals(expected, result);
    }

    @Test
    void createTraining_shouldDelegateToService() {
        Training expected = new Training(1L, 1L, 1L, "Test", 1L, LocalDate.now(), 60);
        when(trainingService.createTraining(1L, 1L, "Test", 1L, LocalDate.now(), 60))
                .thenReturn(expected);

        Training result = facade.createTraining(1L, 1L, "Test", 1L, LocalDate.now(), 60);

        assertEquals(expected, result);
    }

    @Test
    void selectTrainee_shouldDelegateToService() {
        when(traineeService.selectTrainee(1L)).thenReturn(Optional.empty());

        assertTrue(facade.selectTrainee(1L).isEmpty());
    }

    @Test
    void selectTrainer_shouldDelegateToService() {
        Trainer trainer = new Trainer(1L, 1L, 1L);
        when(trainerService.selectTrainer(1L)).thenReturn(Optional.of(trainer));

        assertTrue(facade.selectTrainer(1L).isPresent());
    }

    @Test
    void selectTraining_shouldDelegateToService() {
        when(trainingService.selectTraining(1L)).thenReturn(Optional.empty());

        assertTrue(facade.selectTraining(1L).isEmpty());
    }

    @Test
    void updateTrainee_shouldDelegateToService() {
        Trainee expected = new Trainee(1L, LocalDate.now(), "New", 1L);
        when(traineeService.updateTrainee(1L, LocalDate.now(), "New")).thenReturn(expected);

        Trainee result = facade.updateTrainee(1L, LocalDate.now(), "New");

        assertEquals("New", result.getAddress());
    }

    @Test
    void updateTrainer_shouldDelegateToService() {
        Trainer expected = new Trainer(1L, 2L, 1L);
        when(trainerService.updateTrainer(1L, 2L)).thenReturn(expected);

        Trainer result = facade.updateTrainer(1L, 2L);

        assertEquals(2L, result.getSpecialization());
    }
}