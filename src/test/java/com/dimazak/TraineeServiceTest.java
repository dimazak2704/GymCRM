package com.dimazak;

import com.dimazak.gym.dao.TraineeDao;
import com.dimazak.gym.dao.UserDao;
import com.dimazak.gym.model.Trainee;
import com.dimazak.gym.model.User;
import com.dimazak.gym.service.TraineeService;
import com.dimazak.gym.util.PasswordGenerator;
import com.dimazak.gym.util.UsernameGenerator;
import org.junit.jupiter.api.BeforeEach;
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
class TraineeServiceTest {

    @Mock private TraineeDao traineeDao;
    @Mock private UserDao userDao;
    @Mock private UsernameGenerator usernameGenerator;
    @Mock private PasswordGenerator passwordGenerator;

    @InjectMocks
    private TraineeService traineeService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void createTrainee_shouldCreateUserAndTrainee() {
        when(usernameGenerator.generateUsername("John", "Doe")).thenReturn("John.Doe");
        when(passwordGenerator.generatePassword()).thenReturn("abc1234567");
        when(userDao.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(traineeDao.save(any(Trainee.class))).thenAnswer(invocation -> {
            Trainee t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        Trainee result = traineeService.createTrainee("John", "Doe",
                LocalDate.of(1990, 1, 1), "123 Main St", true);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getUserId());
        verify(userDao).save(any(User.class));
        verify(traineeDao).save(any(Trainee.class));
    }

    @Test
    void updateTrainee_shouldUpdateExistingTrainee() {
        Trainee existing = new Trainee(1L, LocalDate.of(1990, 1, 1), "Old Addr", 1L);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(existing));
        when(traineeDao.save(any(Trainee.class))).thenReturn(existing);

        Trainee result = traineeService.updateTrainee(1L,
                LocalDate.of(1991, 2, 2), "New Addr");

        assertEquals("New Addr", result.getAddress());
        assertEquals(LocalDate.of(1991, 2, 2), result.getDateOfBirth());
        verify(traineeDao).save(any(Trainee.class));
    }

    @Test
    void updateTrainee_shouldThrowWhenNotFound() {
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> traineeService.updateTrainee(99L, LocalDate.now(), "addr"));
    }

    @Test
    void deleteTrainee_shouldDeleteTraineeAndUser() {
        Trainee existing = new Trainee(1L, LocalDate.of(1990, 1, 1), "Addr", 5L);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(existing));

        traineeService.deleteTrainee(1L);

        verify(userDao).deleteById(5L);
        verify(traineeDao).deleteById(1L);
    }

    @Test
    void deleteTrainee_shouldThrowWhenNotFound() {
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> traineeService.deleteTrainee(99L));
    }

    @Test
    void selectTrainee_shouldReturnTraineeWhenExists() {
        Trainee trainee = new Trainee(1L, LocalDate.of(1990, 1, 1), "Addr", 1L);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeService.selectTrainee(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void selectTrainee_shouldReturnEmptyWhenNotFound() {
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        Optional<Trainee> result = traineeService.selectTrainee(99L);

        assertTrue(result.isEmpty());
    }
}