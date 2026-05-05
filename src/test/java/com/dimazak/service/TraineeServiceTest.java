package com.dimazak.service;

import com.dimazak.gym.dao.TraineeDao;
import com.dimazak.gym.dao.UserDao;
import com.dimazak.gym.model.Trainee;
import com.dimazak.gym.model.User;
import com.dimazak.gym.service.TraineeService;
import com.dimazak.gym.util.PasswordGenerator;
import com.dimazak.gym.util.UsernameGenerator;
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

    private static final Long TRAINEE_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long NON_EXISTENT_ID = 99L;
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String GENERATED_USERNAME = "John.Doe";
    private static final String GENERATED_PASSWORD = "abc1234567";
    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 1, 1);
    private static final LocalDate NEW_BIRTH_DATE = LocalDate.of(1991, 2, 2);
    private static final String ADDRESS = "123 Main St";
    private static final String NEW_ADDRESS = "456 New St";

    @Mock private TraineeDao traineeDao;
    @Mock private UserDao userDao;
    @Mock private UsernameGenerator usernameGenerator;
    @Mock private PasswordGenerator passwordGenerator;

    @InjectMocks
    private TraineeService traineeService;

    @Test
    void createTrainee_shouldCreateUserAndTrainee() {
        when(usernameGenerator.generateUsername(FIRST_NAME, LAST_NAME)).thenReturn(GENERATED_USERNAME);
        when(passwordGenerator.generatePassword()).thenReturn(GENERATED_PASSWORD);
        when(userDao.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(USER_ID);
            return u;
        });
        when(traineeDao.save(any(Trainee.class))).thenAnswer(invocation -> {
            Trainee t = invocation.getArgument(0);
            t.setId(TRAINEE_ID);
            return t;
        });

        Trainee result = traineeService.createTrainee(FIRST_NAME, LAST_NAME,
                BIRTH_DATE, ADDRESS, true);

        assertNotNull(result);
        assertEquals(TRAINEE_ID, result.getId());
        assertEquals(USER_ID, result.getUserId());
        verify(userDao).save(any(User.class));
        verify(traineeDao).save(any(Trainee.class));
    }

    @Test
    void updateTrainee_shouldUpdateExistingTrainee() {
        Trainee existing = new Trainee(TRAINEE_ID, BIRTH_DATE, ADDRESS, USER_ID);
        when(traineeDao.findById(TRAINEE_ID)).thenReturn(Optional.of(existing));
        when(traineeDao.save(any(Trainee.class))).thenReturn(existing);

        Trainee result = traineeService.updateTrainee(TRAINEE_ID, NEW_BIRTH_DATE, NEW_ADDRESS);

        assertEquals(NEW_ADDRESS, result.getAddress());
        assertEquals(NEW_BIRTH_DATE, result.getDateOfBirth());
        verify(traineeDao).save(any(Trainee.class));
    }

    @Test
    void updateTrainee_shouldThrowWhenNotFound() {
        when(traineeDao.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> traineeService.updateTrainee(NON_EXISTENT_ID, BIRTH_DATE, ADDRESS));
    }

    @Test
    void deleteTrainee_shouldDeleteTraineeAndUser() {
        Trainee existing = new Trainee(TRAINEE_ID, BIRTH_DATE, ADDRESS, USER_ID);
        when(traineeDao.findById(TRAINEE_ID)).thenReturn(Optional.of(existing));

        traineeService.deleteTrainee(TRAINEE_ID);

        verify(userDao).deleteById(USER_ID);
        verify(traineeDao).deleteById(TRAINEE_ID);
    }

    @Test
    void deleteTrainee_shouldThrowWhenNotFound() {
        when(traineeDao.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> traineeService.deleteTrainee(NON_EXISTENT_ID));
    }

    @Test
    void selectTrainee_shouldReturnTraineeWhenExists() {
        Trainee trainee = new Trainee(TRAINEE_ID, BIRTH_DATE, ADDRESS, USER_ID);
        when(traineeDao.findById(TRAINEE_ID)).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeService.selectTrainee(TRAINEE_ID);

        assertTrue(result.isPresent());
        assertEquals(TRAINEE_ID, result.get().getId());
    }

    @Test
    void selectTrainee_shouldReturnEmptyWhenNotFound() {
        when(traineeDao.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertTrue(traineeService.selectTrainee(NON_EXISTENT_ID).isEmpty());
    }
}