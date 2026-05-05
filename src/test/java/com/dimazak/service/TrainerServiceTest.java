package com.dimazak.service;

import com.dimazak.gym.dao.TrainerDao;
import com.dimazak.gym.dao.UserDao;
import com.dimazak.gym.model.Trainer;
import com.dimazak.gym.model.User;
import com.dimazak.gym.service.TrainerService;
import com.dimazak.gym.util.PasswordGenerator;
import com.dimazak.gym.util.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    private static final Long TRAINER_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long NON_EXISTENT_ID = 99L;
    private static final Long SPECIALIZATION_1 = 1L;
    private static final Long SPECIALIZATION_2 = 2L;
    private static final String FIRST_NAME = "Jane";
    private static final String LAST_NAME = "Doe";
    private static final String GENERATED_USERNAME = "Jane.Doe";
    private static final String GENERATED_PASSWORD = "pass123456";

    @Mock private TrainerDao trainerDao;
    @Mock private UserDao userDao;
    @Mock private UsernameGenerator usernameGenerator;
    @Mock private PasswordGenerator passwordGenerator;

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void createTrainer_shouldCreateUserAndTrainer() {
        when(usernameGenerator.generateUsername(FIRST_NAME, LAST_NAME)).thenReturn(GENERATED_USERNAME);
        when(passwordGenerator.generatePassword()).thenReturn(GENERATED_PASSWORD);
        when(userDao.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(USER_ID);
            return u;
        });
        when(trainerDao.save(any(Trainer.class))).thenAnswer(inv -> {
            Trainer t = inv.getArgument(0);
            t.setId(TRAINER_ID);
            return t;
        });

        Trainer result = trainerService.createTrainer(FIRST_NAME, LAST_NAME, SPECIALIZATION_1, true);

        assertNotNull(result);
        assertEquals(TRAINER_ID, result.getId());
        verify(userDao).save(any(User.class));
        verify(trainerDao).save(any(Trainer.class));
    }

    @Test
    void updateTrainer_shouldUpdateSpecialization() {
        Trainer existing = new Trainer(TRAINER_ID, SPECIALIZATION_1, USER_ID);
        when(trainerDao.findById(TRAINER_ID)).thenReturn(Optional.of(existing));
        when(trainerDao.save(any(Trainer.class))).thenReturn(existing);

        Trainer result = trainerService.updateTrainer(TRAINER_ID, SPECIALIZATION_2);

        assertEquals(SPECIALIZATION_2, result.getSpecialization());
    }

    @Test
    void updateTrainer_shouldThrowWhenNotFound() {
        when(trainerDao.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> trainerService.updateTrainer(NON_EXISTENT_ID, SPECIALIZATION_1));
    }

    @Test
    void selectTrainer_shouldReturnTrainer() {
        Trainer trainer = new Trainer(TRAINER_ID, SPECIALIZATION_1, USER_ID);
        when(trainerDao.findById(TRAINER_ID)).thenReturn(Optional.of(trainer));

        assertTrue(trainerService.selectTrainer(TRAINER_ID).isPresent());
    }

    @Test
    void selectTrainer_shouldReturnEmptyWhenNotFound() {
        when(trainerDao.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertTrue(trainerService.selectTrainer(NON_EXISTENT_ID).isEmpty());
    }
}