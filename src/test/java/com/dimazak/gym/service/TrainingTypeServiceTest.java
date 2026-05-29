package com.dimazak.gym.service;

import com.dimazak.gym.dao.TrainingTypeDao;
import com.dimazak.gym.dto.TrainingTypeResponse;
import com.dimazak.gym.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceTest {

    private static final Long CARDIO_ID = 1L;
    private static final Long STRENGTH_ID = 2L;
    private static final String CARDIO = "Cardio";
    private static final String STRENGTH = "Strength";

    @Mock private TrainingTypeDao trainingTypeDao;

    @InjectMocks
    private TrainingTypeService service;

    @Test
    void getAllTrainingTypes_shouldReturnMappedList() {
        List<TrainingType> types = List.of(
                new TrainingType(CARDIO_ID, CARDIO),
                new TrainingType(STRENGTH_ID, STRENGTH));
        when(trainingTypeDao.findAll()).thenReturn(types);

        List<TrainingTypeResponse> result = service.getAllTrainingTypes();

        assertEquals(2, result.size());
        assertEquals(CARDIO_ID, result.get(0).id());
        assertEquals(CARDIO, result.get(0).trainingType());
        assertEquals(STRENGTH_ID, result.get(1).id());
        assertEquals(STRENGTH, result.get(1).trainingType());
    }

    @Test
    void getAllTrainingTypes_shouldReturnEmptyList() {
        when(trainingTypeDao.findAll()).thenReturn(List.of());

        List<TrainingTypeResponse> result = service.getAllTrainingTypes();

        assertTrue(result.isEmpty());
    }
}