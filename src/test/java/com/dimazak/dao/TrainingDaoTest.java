package com.dimazak.dao;

import com.dimazak.gym.dao.TrainingDao;
import com.dimazak.gym.model.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class TrainingDaoTest {

    private static final Long TRAINING_ID_1 = 1L;
    private static final Long TRAINING_ID_2 = 2L;
    private static final Long TRAINING_ID_5 = 5L;
    private static final Long NON_EXISTENT_ID = 99L;
    private static final Long TRAINEE_ID = 1L;
    private static final Long TRAINER_ID = 1L;
    private static final Long TRAINING_TYPE_ID = 1L;
    private static final String TRAINING_NAME = "Morning Cardio";
    private static final String TRAINING_NAME_2 = "Evening Yoga";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 4, 1);
    private static final int DURATION_60 = 60;
    private static final int DURATION_45 = 45;

    private Map<Long, Training> trainingMap;
    private AtomicLong trainingIdSequence;
    private TrainingDao trainingDao;

    @BeforeEach
    void setUp() {
        trainingMap = new ConcurrentHashMap<>();
        trainingIdSequence = new AtomicLong(0);
        trainingDao = new TrainingDao(trainingMap, trainingIdSequence);
    }

    @Test
    void save_shouldAssignIdAndStore() {
        Training training = new Training(null, TRAINEE_ID, TRAINER_ID,
                TRAINING_NAME, TRAINING_TYPE_ID, TRAINING_DATE, DURATION_60);

        Training saved = trainingDao.save(training);

        assertEquals(TRAINING_ID_1, saved.getId());
        assertTrue(trainingMap.containsKey(TRAINING_ID_1));
    }

    @Test
    void save_shouldNotReassignExistingId() {
        Training training = new Training(TRAINING_ID_5, TRAINEE_ID, TRAINER_ID,
                TRAINING_NAME, TRAINING_TYPE_ID, TRAINING_DATE, DURATION_60);

        Training saved = trainingDao.save(training);

        assertEquals(TRAINING_ID_5, saved.getId());
        assertTrue(trainingMap.containsKey(TRAINING_ID_5));
    }

    @Test
    void findById_shouldReturnTraining() {
        Training training = new Training(TRAINING_ID_1, TRAINEE_ID, TRAINER_ID,
                TRAINING_NAME, TRAINING_TYPE_ID, TRAINING_DATE, DURATION_60);
        trainingMap.put(TRAINING_ID_1, training);

        Optional<Training> result = trainingDao.findById(TRAINING_ID_1);

        assertTrue(result.isPresent());
        assertEquals(TRAINING_ID_1, result.get().getId());
        assertEquals(TRAINING_NAME, result.get().getTrainingName());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        assertTrue(trainingDao.findById(NON_EXISTENT_ID).isEmpty());
    }

    @Test
    void findAll_shouldReturnAll() {
        trainingMap.put(TRAINING_ID_1, new Training(TRAINING_ID_1, TRAINEE_ID, TRAINER_ID,
                TRAINING_NAME, TRAINING_TYPE_ID, TRAINING_DATE, DURATION_60));
        trainingMap.put(TRAINING_ID_2, new Training(TRAINING_ID_2, TRAINEE_ID, TRAINER_ID,
                TRAINING_NAME_2, TRAINING_TYPE_ID, TRAINING_DATE, DURATION_45));

        Collection<Training> all = trainingDao.findAll();

        assertEquals(2, all.size());
    }
}