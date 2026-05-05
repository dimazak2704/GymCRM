package com.dimazak;

import com.dimazak.gym.model.*;
import com.dimazak.gym.storage.StorageInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class StorageInitializerTest {

    private static final String VALID_INIT_FILE = "initial-data.json";
    private static final String INVALID_INIT_FILE = "non-existent.json";
    private static final int EXPECTED_USERS_COUNT = 2;
    private static final int EXPECTED_TRAINEES_COUNT = 1;
    private static final int EXPECTED_TRAINERS_COUNT = 1;
    private static final int EXPECTED_TRAININGS_COUNT = 1;
    private static final int EXPECTED_TRAINING_TYPES_COUNT = 3;

    private Map<Long, User> userStorage;
    private Map<Long, Trainee> traineeStorage;
    private Map<Long, Trainer> trainerStorage;
    private Map<Long, Training> trainingStorage;
    private Map<Long, TrainingType> trainingTypeStorage;

    private AtomicLong userIdSeq;
    private AtomicLong traineeIdSeq;
    private AtomicLong trainerIdSeq;
    private AtomicLong trainingIdSeq;
    private AtomicLong trainingTypeIdSeq;

    private StorageInitializer initializer;

    @BeforeEach
    void setUp() {
        userStorage = new ConcurrentHashMap<>();
        traineeStorage = new ConcurrentHashMap<>();
        trainerStorage = new ConcurrentHashMap<>();
        trainingStorage = new ConcurrentHashMap<>();
        trainingTypeStorage = new ConcurrentHashMap<>();

        userIdSeq = new AtomicLong(0);
        traineeIdSeq = new AtomicLong(0);
        trainerIdSeq = new AtomicLong(0);
        trainingIdSeq = new AtomicLong(0);
        trainingTypeIdSeq = new AtomicLong(0);

        initializer = new StorageInitializer(
                userStorage, traineeStorage, trainerStorage,
                trainingStorage, trainingTypeStorage,
                userIdSeq, traineeIdSeq, trainerIdSeq,
                trainingIdSeq, trainingTypeIdSeq);
    }

    @Test
    void init_shouldLoadDataFromValidFile() {
        ReflectionTestUtils.setField(initializer, "initFilePath", VALID_INIT_FILE);

        initializer.init();

        assertEquals(EXPECTED_USERS_COUNT, userStorage.size());
        assertEquals(EXPECTED_TRAINEES_COUNT, traineeStorage.size());
        assertEquals(EXPECTED_TRAINERS_COUNT, trainerStorage.size());
        assertEquals(EXPECTED_TRAININGS_COUNT, trainingStorage.size());
        assertEquals(EXPECTED_TRAINING_TYPES_COUNT, trainingTypeStorage.size());
    }

    @Test
    void init_shouldUpdateSequences() {
        ReflectionTestUtils.setField(initializer, "initFilePath", VALID_INIT_FILE);

        initializer.init();

        assertEquals(2L, userIdSeq.get());
        assertEquals(1L, traineeIdSeq.get());
        assertEquals(1L, trainerIdSeq.get());
        assertEquals(1L, trainingIdSeq.get());
        assertEquals(3L, trainingTypeIdSeq.get());
    }

    @Test
    void init_shouldHandleMissingFile() {
        ReflectionTestUtils.setField(initializer, "initFilePath", INVALID_INIT_FILE);

        initializer.init();

        assertTrue(userStorage.isEmpty());
        assertTrue(traineeStorage.isEmpty());
    }
}