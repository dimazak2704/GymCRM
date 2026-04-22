package com.dimazak.gym.service;

import com.dimazak.gym.dao.TrainerDao;
import com.dimazak.gym.dao.UserDao;
import com.dimazak.gym.model.Trainer;
import com.dimazak.gym.model.User;
import com.dimazak.gym.util.PasswordGenerator;
import com.dimazak.gym.util.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private TrainerDao trainerDao;
    private UserDao userDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    public Trainer createTrainer(String firstName, String lastName,
                                 Long specialization, boolean isActive) {
        log.info("Creating trainer profile for: {} {}", firstName, lastName);

        String username = usernameGenerator.generateUsername(firstName, lastName);
        String password = passwordGenerator.generatePassword();

        User user = new User(null, firstName, lastName, username, password, isActive);
        user = userDao.save(user);
        log.info("User created with username: {}", username);

        Trainer trainer = new Trainer(null, specialization, user.getId());
        trainer = trainerDao.save(trainer);
        log.info("Trainer profile created with id: {}", trainer.getId());

        return trainer;
    }

    public Trainer updateTrainer(Long trainerId, Long specialization) {
        log.info("Updating trainer with id: {}", trainerId);

        Optional<Trainer> existing = trainerDao.findById(trainerId);
        if (existing.isEmpty()) {
            log.error("Trainer not found with id: {}", trainerId);
            throw new IllegalArgumentException("Trainer not found with id: " + trainerId);
        }

        Trainer trainer = existing.get();
        trainer.setSpecialization(specialization);
        trainerDao.save(trainer);

        log.info("Trainer updated successfully with id: {}", trainerId);
        return trainer;
    }

    public Optional<Trainer> selectTrainer(Long trainerId) {
        log.info("Selecting trainer with id: {}", trainerId);
        Optional<Trainer> trainer = trainerDao.findById(trainerId);
        if (trainer.isEmpty()) {
            log.warn("Trainer not found with id: {}", trainerId);
        }
        return trainer;
    }
}