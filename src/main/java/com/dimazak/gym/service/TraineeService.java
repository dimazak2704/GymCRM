package com.dimazak.gym.service;

import com.dimazak.gym.dao.TraineeDao;
import com.dimazak.gym.dao.UserDao;
import com.dimazak.gym.model.Trainee;
import com.dimazak.gym.model.User;
import com.dimazak.gym.util.PasswordGenerator;
import com.dimazak.gym.util.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private TraineeDao traineeDao;
    private UserDao userDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
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

    public Trainee createTrainee(String firstName, String lastName,
                                 LocalDate dateOfBirth, String address, boolean isActive) {
        log.info("Creating trainee profile for: {} {}", firstName, lastName);

        String username = usernameGenerator.generateUsername(firstName, lastName);
        String password = passwordGenerator.generatePassword();

        User user = new User(null, firstName, lastName, username, password, isActive);
        user = userDao.save(user);
        log.info("User created with username: {}", username);

        Trainee trainee = new Trainee(null, dateOfBirth, address, user.getId());
        trainee = traineeDao.save(trainee);
        log.info("Trainee profile created with id: {}", trainee.getId());

        return trainee;
    }

    public Trainee updateTrainee(Long traineeId, LocalDate dateOfBirth, String address) {
        log.info("Updating trainee with id: {}", traineeId);

        Optional<Trainee> existing = traineeDao.findById(traineeId);
        if (existing.isEmpty()) {
            log.error("Trainee not found with id: {}", traineeId);
            throw new IllegalArgumentException("Trainee not found with id: " + traineeId);
        }

        Trainee trainee = existing.get();
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);
        traineeDao.save(trainee);

        log.info("Trainee updated successfully with id: {}", traineeId);
        return trainee;
    }

    public void deleteTrainee(Long traineeId) {
        log.info("Deleting trainee with id: {}", traineeId);

        Optional<Trainee> existing = traineeDao.findById(traineeId);
        if (existing.isEmpty()) {
            log.error("Trainee not found with id: {}", traineeId);
            throw new IllegalArgumentException("Trainee not found with id: " + traineeId);
        }

        Trainee trainee = existing.get();
        userDao.deleteById(trainee.getUserId());
        traineeDao.deleteById(traineeId);

        log.info("Trainee deleted successfully with id: {}", traineeId);
    }

    public Optional<Trainee> selectTrainee(Long traineeId) {
        log.info("Selecting trainee with id: {}", traineeId);
        Optional<Trainee> trainee = traineeDao.findById(traineeId);
        if (trainee.isEmpty()) {
            log.warn("Trainee not found with id: {}", traineeId);
        }
        return trainee;
    }
}