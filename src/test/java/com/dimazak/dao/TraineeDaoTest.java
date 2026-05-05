package com.dimazak.dao;

import com.dimazak.gym.dao.TraineeDao;
import com.dimazak.gym.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class TraineeDaoTest {

    private static final Long TRAINEE_ID_1 = 1L;
    private static final Long TRAINEE_ID_2 = 2L;
    private static final Long TRAINEE_ID_5 = 5L;
    private static final Long NON_EXISTENT_ID = 99L;
    private static final Long USER_ID_1 = 1L;
    private static final Long USER_ID_2 = 2L;
    private static final LocalDate DEFAULT_BIRTH_DATE = LocalDate.of(1990, 1, 1);
    private static final String ADDRESS_A = "Address A";
    private static final String ADDRESS_B = "Address B";

    private Map<Long, Trainee> traineeMap;
    private AtomicLong traineeIdSequence;
    private TraineeDao traineeDao;

    @BeforeEach
    void setUp() {
        traineeMap = new ConcurrentHashMap<>();
        traineeIdSequence = new AtomicLong(0);
        traineeDao = new TraineeDao(traineeMap, traineeIdSequence);
    }

    @Test
    void save_shouldAssignIdAndStore() {
        Trainee trainee = new Trainee(null, DEFAULT_BIRTH_DATE, ADDRESS_A, USER_ID_1);

        Trainee saved = traineeDao.save(trainee);

        assertEquals(TRAINEE_ID_1, saved.getId());
        assertTrue(traineeMap.containsKey(TRAINEE_ID_1));
    }

    @Test
    void save_shouldNotReassignExistingId() {
        Trainee trainee = new Trainee(TRAINEE_ID_5, DEFAULT_BIRTH_DATE, ADDRESS_A, USER_ID_1);

        Trainee saved = traineeDao.save(trainee);

        assertEquals(TRAINEE_ID_5, saved.getId());
        assertTrue(traineeMap.containsKey(TRAINEE_ID_5));
    }

    @Test
    void findById_shouldReturnTrainee() {
        Trainee trainee = new Trainee(TRAINEE_ID_1, DEFAULT_BIRTH_DATE, ADDRESS_A, USER_ID_1);
        traineeMap.put(TRAINEE_ID_1, trainee);

        Optional<Trainee> result = traineeDao.findById(TRAINEE_ID_1);

        assertTrue(result.isPresent());
        assertEquals(TRAINEE_ID_1, result.get().getId());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        assertTrue(traineeDao.findById(NON_EXISTENT_ID).isEmpty());
    }

    @Test
    void findAll_shouldReturnAll() {
        traineeMap.put(TRAINEE_ID_1, new Trainee(TRAINEE_ID_1, DEFAULT_BIRTH_DATE, ADDRESS_A, USER_ID_1));
        traineeMap.put(TRAINEE_ID_2, new Trainee(TRAINEE_ID_2, DEFAULT_BIRTH_DATE, ADDRESS_B, USER_ID_2));

        Collection<Trainee> all = traineeDao.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void deleteById_shouldRemoveTrainee() {
        traineeMap.put(TRAINEE_ID_1, new Trainee(TRAINEE_ID_1, DEFAULT_BIRTH_DATE, ADDRESS_A, USER_ID_1));

        traineeDao.deleteById(TRAINEE_ID_1);

        assertFalse(traineeMap.containsKey(TRAINEE_ID_1));
    }

    @Test
    void deleteById_shouldHandleNonExistent() {
        traineeDao.deleteById(NON_EXISTENT_ID);
        assertTrue(traineeMap.isEmpty());
    }
}