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

    private static final Long ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long SPECIALIZATION = 1L;
    private static final Long NEW_SPECIALIZATION = 2L;
    private static final Long TRAINING_TYPE_ID = 1L;
    private static final String FIRST_NAME = "A";
    private static final String LAST_NAME = "B";
    private static final String ADDRESS = "Addr";
    private static final String NEW_ADDRESS = "New";
    private static final String TRAINING_NAME = "Test";
    private static final LocalDate DATE = LocalDate.of(2024, 1, 1);
    private static final int DURATION = 60;

    @Mock private TraineeService traineeService;
    @Mock private TrainerService trainerService;
    @Mock private TrainingService trainingService;

    @InjectMocks
    private GymFacade facade;

    @Test
    void createTrainee_shouldDelegateToService() {
        Trainee expected = new Trainee(ID, DATE, ADDRESS, USER_ID);
        when(traineeService.createTrainee(FIRST_NAME, LAST_NAME, DATE, ADDRESS, true))
                .thenReturn(expected);

        Trainee result = facade.createTrainee(FIRST_NAME, LAST_NAME, DATE, ADDRESS, true);

        assertEquals(expected, result);
    }

    @Test
    void deleteTrainee_shouldDelegateToService() {
        facade.deleteTrainee(ID);
        verify(traineeService).deleteTrainee(ID);
    }

    @Test
    void createTrainer_shouldDelegateToService() {
        Trainer expected = new Trainer(ID, SPECIALIZATION, USER_ID);
        when(trainerService.createTrainer(FIRST_NAME, LAST_NAME, SPECIALIZATION, true))
                .thenReturn(expected);

        Trainer result = facade.createTrainer(FIRST_NAME, LAST_NAME, SPECIALIZATION, true);

        assertEquals(expected, result);
    }

    @Test
    void createTraining_shouldDelegateToService() {
        Training expected = new Training(ID, ID, ID, TRAINING_NAME, TRAINING_TYPE_ID, DATE, DURATION);
        when(trainingService.createTraining(ID, ID, TRAINING_NAME, TRAINING_TYPE_ID, DATE, DURATION))
                .thenReturn(expected);

        Training result = facade.createTraining(ID, ID, TRAINING_NAME, TRAINING_TYPE_ID, DATE, DURATION);

        assertEquals(expected, result);
    }

    @Test
    void selectTrainee_shouldDelegateToService() {
        when(traineeService.selectTrainee(ID)).thenReturn(Optional.empty());

        assertTrue(facade.selectTrainee(ID).isEmpty());
    }

    @Test
    void selectTrainer_shouldDelegateToService() {
        Trainer trainer = new Trainer(ID, SPECIALIZATION, USER_ID);
        when(trainerService.selectTrainer(ID)).thenReturn(Optional.of(trainer));

        assertTrue(facade.selectTrainer(ID).isPresent());
    }

    @Test
    void selectTraining_shouldDelegateToService() {
        when(trainingService.selectTraining(ID)).thenReturn(Optional.empty());

        assertTrue(facade.selectTraining(ID).isEmpty());
    }

    @Test
    void updateTrainee_shouldDelegateToService() {
        Trainee expected = new Trainee(ID, DATE, NEW_ADDRESS, USER_ID);
        when(traineeService.updateTrainee(ID, DATE, NEW_ADDRESS)).thenReturn(expected);

        Trainee result = facade.updateTrainee(ID, DATE, NEW_ADDRESS);

        assertEquals(NEW_ADDRESS, result.getAddress());
    }

    @Test
    void updateTrainer_shouldDelegateToService() {
        Trainer expected = new Trainer(ID, NEW_SPECIALIZATION, USER_ID);
        when(trainerService.updateTrainer(ID, NEW_SPECIALIZATION)).thenReturn(expected);

        Trainer result = facade.updateTrainer(ID, NEW_SPECIALIZATION);

        assertEquals(NEW_SPECIALIZATION, result.getSpecialization());
    }
}