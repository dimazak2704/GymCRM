package com.dimazak;

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

    @Mock private TrainerDao trainerDao;
    @Mock private UserDao userDao;
    @Mock private UsernameGenerator usernameGenerator;
    @Mock private PasswordGenerator passwordGenerator;

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void createTrainer_shouldCreateUserAndTrainer() {
        when(usernameGenerator.generateUsername("Jane", "Doe")).thenReturn("Jane.Doe");
        when(passwordGenerator.generatePassword()).thenReturn("pass123456");
        when(userDao.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(trainerDao.save(any(Trainer.class))).thenAnswer(inv -> {
            Trainer t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        Trainer result = trainerService.createTrainer("Jane", "Doe", 1L, true);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userDao).save(any(User.class));
        verify(trainerDao).save(any(Trainer.class));
    }

    @Test
    void updateTrainer_shouldUpdateSpecialization() {
        Trainer existing = new Trainer(1L, 1L, 1L);
        when(trainerDao.findById(1L)).thenReturn(Optional.of(existing));
        when(trainerDao.save(any(Trainer.class))).thenReturn(existing);

        Trainer result = trainerService.updateTrainer(1L, 2L);

        assertEquals(2L, result.getSpecialization());
    }

    @Test
    void updateTrainer_shouldThrowWhenNotFound() {
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> trainerService.updateTrainer(99L, 1L));
    }

    @Test
    void selectTrainer_shouldReturnTrainer() {
        Trainer trainer = new Trainer(1L, 1L, 1L);
        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = trainerService.selectTrainer(1L);

        assertTrue(result.isPresent());
    }

    @Test
    void selectTrainer_shouldReturnEmptyWhenNotFound() {
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        assertTrue(trainerService.selectTrainer(99L).isEmpty());
    }
}