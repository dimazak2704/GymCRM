package com.dimazak.dao;

import com.dimazak.gym.dao.TrainerDao;
import com.dimazak.gym.model.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class TrainerDaoTest {

    private static final Long TRAINER_ID_1 = 1L;
    private static final Long TRAINER_ID_2 = 2L;
    private static final Long TRAINER_ID_5 = 5L;
    private static final Long NON_EXISTENT_ID = 99L;
    private static final Long USER_ID_1 = 1L;
    private static final Long USER_ID_2 = 2L;
    private static final Long SPECIALIZATION_1 = 1L;
    private static final Long SPECIALIZATION_2 = 2L;

    private Map<Long, Trainer> trainerMap;
    private AtomicLong trainerIdSequence;
    private TrainerDao trainerDao;

    @BeforeEach
    void setUp() {
        trainerMap = new ConcurrentHashMap<>();
        trainerIdSequence = new AtomicLong(0);
        trainerDao = new TrainerDao(trainerMap, trainerIdSequence);
    }

    @Test
    void save_shouldAssignIdAndStore() {
        Trainer trainer = new Trainer(null, SPECIALIZATION_1, USER_ID_1);

        Trainer saved = trainerDao.save(trainer);

        assertEquals(TRAINER_ID_1, saved.getId());
        assertTrue(trainerMap.containsKey(TRAINER_ID_1));
    }

    @Test
    void save_shouldNotReassignExistingId() {
        Trainer trainer = new Trainer(TRAINER_ID_5, SPECIALIZATION_1, USER_ID_1);

        Trainer saved = trainerDao.save(trainer);

        assertEquals(TRAINER_ID_5, saved.getId());
        assertTrue(trainerMap.containsKey(TRAINER_ID_5));
    }

    @Test
    void findById_shouldReturnTrainer() {
        Trainer trainer = new Trainer(TRAINER_ID_1, SPECIALIZATION_1, USER_ID_1);
        trainerMap.put(TRAINER_ID_1, trainer);

        Optional<Trainer> result = trainerDao.findById(TRAINER_ID_1);

        assertTrue(result.isPresent());
        assertEquals(TRAINER_ID_1, result.get().getId());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        assertTrue(trainerDao.findById(NON_EXISTENT_ID).isEmpty());
    }

    @Test
    void findAll_shouldReturnAll() {
        trainerMap.put(TRAINER_ID_1, new Trainer(TRAINER_ID_1, SPECIALIZATION_1, USER_ID_1));
        trainerMap.put(TRAINER_ID_2, new Trainer(TRAINER_ID_2, SPECIALIZATION_2, USER_ID_2));

        Collection<Trainer> all = trainerDao.findAll();

        assertEquals(2, all.size());
    }
}