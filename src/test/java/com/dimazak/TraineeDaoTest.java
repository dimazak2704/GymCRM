package com.dimazak;

import com.dimazak.gym.dao.TraineeDao;
import com.dimazak.gym.model.Trainee;
import com.dimazak.gym.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeDaoTest {

    @Mock private InMemoryStorage storage;
    @InjectMocks private TraineeDao traineeDao;

    private Map<Long, Trainee> traineeMap;

    @BeforeEach
    void setUp() {
        traineeMap = new ConcurrentHashMap<>();
        lenient().when(storage.getTraineeStorage()).thenReturn(traineeMap);
    }

    @Test
    void save_shouldAssignIdAndStore() {
        when(storage.nextTraineeId()).thenReturn(1L);
        Trainee trainee = new Trainee(null, LocalDate.of(1990, 1, 1), "Addr", 1L);

        Trainee saved = traineeDao.save(trainee);

        assertEquals(1L, saved.getId());
        assertTrue(traineeMap.containsKey(1L));
    }

    @Test
    void save_shouldNotReassignExistingId() {
        Trainee trainee = new Trainee(5L, LocalDate.of(1990, 1, 1), "Addr", 1L);

        Trainee saved = traineeDao.save(trainee);

        assertEquals(5L, saved.getId());
        assertTrue(traineeMap.containsKey(5L));
    }

    @Test
    void findById_shouldReturnTrainee() {
        Trainee trainee = new Trainee(1L, LocalDate.of(1990, 1, 1), "Addr", 1L);
        traineeMap.put(1L, trainee);

        Optional<Trainee> result = traineeDao.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        assertTrue(traineeDao.findById(99L).isEmpty());
    }

    @Test
    void findAll_shouldReturnAll() {
        traineeMap.put(1L, new Trainee(1L, LocalDate.now(), "A", 1L));
        traineeMap.put(2L, new Trainee(2L, LocalDate.now(), "B", 2L));

        Collection<Trainee> all = traineeDao.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void deleteById_shouldRemoveTrainee() {
        traineeMap.put(1L, new Trainee(1L, LocalDate.now(), "A", 1L));

        traineeDao.deleteById(1L);

        assertFalse(traineeMap.containsKey(1L));
    }

    @Test
    void deleteById_shouldHandleNonExistent() {
        traineeDao.deleteById(99L); // should not throw
        assertTrue(traineeMap.isEmpty());
    }
}