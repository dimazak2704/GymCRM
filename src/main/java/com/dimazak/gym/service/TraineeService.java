package com.dimazak.gym.service;

import com.dimazak.gym.dao.TraineeDao;
import com.dimazak.gym.dao.TrainerDao;
import com.dimazak.gym.dao.TrainingDao;
import com.dimazak.gym.exception.EntityNotFoundException;
import com.dimazak.gym.exception.ValidationException;
import com.dimazak.gym.model.Trainee;
import com.dimazak.gym.model.Trainer;
import com.dimazak.gym.model.Training;
import com.dimazak.gym.model.User;
import com.dimazak.gym.util.PasswordGenerator;
import com.dimazak.gym.util.UsernameGenerator;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final TrainingDao trainingDao;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;

    public TraineeService(TraineeDao traineeDao,
                          TrainerDao trainerDao,
                          TrainingDao trainingDao,
                          UsernameGenerator usernameGenerator,
                          PasswordGenerator passwordGenerator,
                          PasswordEncoder passwordEncoder) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.trainingDao = trainingDao;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Trainee createTrainee(String firstName, String lastName,
                                 LocalDate dateOfBirth, String address) {
        log.info("Creating trainee profile for: {} {}", firstName, lastName);
        validateRequiredFields(firstName, lastName);

        String username = usernameGenerator.generateUsername(firstName, lastName);
        String rawPassword = passwordGenerator.generatePassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = new User(null, firstName, lastName, username, encodedPassword, true);
        Trainee trainee = new Trainee(null, dateOfBirth, address, user);
        trainee = traineeDao.save(trainee);

        trainee.getUser().setPassword(rawPassword);

        log.info("Trainee profile created. Username: {}, TraineeId: {}",
                username, trainee.getId());
        return trainee;
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return traineeDao.findByUsername(username).isPresent();
    }

    @Transactional(readOnly = true)
    public boolean matchCredentials(String username, String password) {
        log.debug("Matching credentials for trainee username: {}", username);
        return traineeDao.findByUsername(username)
                .map(trainee -> passwordEncoder.matches(password, trainee.getUser().getPassword()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Trainee getByUsername(String username) {
        log.info("Selecting trainee by username: {}", username);
        return traineeDao.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Trainee not found with username: {}", username);
                    return new EntityNotFoundException(
                            "Trainee not found with username: " + username);
                });
    }

    @Transactional(readOnly = true)
    public Trainee getProfileByUsername(String username) {
        log.info("Getting full profile for trainee: {}", username);
        Trainee trainee = getByUsername(username);
        Hibernate.initialize(trainee.getTrainers());
        trainee.getTrainers().forEach(t -> {
            Hibernate.initialize(t.getUser());
            Hibernate.initialize(t.getSpecialization());
        });
        return trainee;
    }

    @Transactional
    public void changePassword(String username, String newPassword) {
        log.info("Changing password for trainee: {}", username);
        validatePassword(newPassword);

        Trainee trainee = getByUsername(username);
        trainee.getUser().setPassword(passwordEncoder.encode(newPassword));
        traineeDao.save(trainee);
        log.info("Password changed successfully for trainee: {}", username);
    }

    @Transactional
    public Trainee updateTrainee(String username, String firstName, String lastName,
                                 LocalDate dateOfBirth, String address, boolean isActive) {
        log.info("Updating trainee profile: {}", username);
        validateRequiredFields(firstName, lastName);

        Trainee trainee = getByUsername(username);
        User user = trainee.getUser();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setActive(isActive);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);

        trainee = traineeDao.save(trainee);
        Hibernate.initialize(trainee.getTrainers());
        trainee.getTrainers().forEach(t -> {
            Hibernate.initialize(t.getUser());
            Hibernate.initialize(t.getSpecialization());
        });

        log.info("Trainee profile updated: {}", username);
        return trainee;
    }

    @Transactional
    public void setActiveStatus(String username, boolean isActive) {
        log.info("Setting active status for trainee '{}' to: {}", username, isActive);
        Trainee trainee = getByUsername(username);

        if (trainee.getUser().isActive() == isActive) {
            throw new ValidationException(
                    "Trainee is already " + (isActive ? "active" : "deactivated"));
        }

        trainee.getUser().setActive(isActive);
        traineeDao.save(trainee);
        log.info("Trainee '{}' active status set to: {}", username, isActive);
    }

    @Transactional
    public void deleteByUsername(String username) {
        log.info("Deleting trainee profile by username: {}", username);
        Trainee trainee = getByUsername(username);
        traineeDao.delete(trainee);
        log.info("Trainee '{}' deleted successfully", username);
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(String username, LocalDate fromDate,
                                              LocalDate toDate, String trainerName,
                                              String trainingTypeName) {
        log.info("Getting trainings for trainee: {}", username);
        getByUsername(username);
        return trainingDao.findByTraineeWithFilters(username, fromDate, toDate, trainerName, trainingTypeName);
    }

    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        log.info("Getting unassigned trainers for trainee: {}", traineeUsername);
        getByUsername(traineeUsername);
        return trainerDao.findUnassignedByTraineeUsername(traineeUsername);
    }

    @Transactional
    public Trainee updateTrainersList(String username, List<String> trainerUsernames) {
        log.info("Updating trainers list for trainee: {}", username);
        Trainee trainee = getByUsername(username);

        List<Trainer> trainers = trainerUsernames.stream()
                .map(trainerUsername -> trainerDao.findByUsername(trainerUsername)
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Trainer not found: " + trainerUsername)))
                .collect(Collectors.toList());

        trainee.setTrainers(trainers);
        trainee = traineeDao.save(trainee);

        Hibernate.initialize(trainee.getTrainers());
        trainee.getTrainers().forEach(t -> {
            Hibernate.initialize(t.getUser());
            Hibernate.initialize(t.getSpecialization());
        });

        log.info("Trainers list updated for trainee: {}", username);
        return trainee;
    }

    private void validateRequiredFields(String firstName, String lastName) {
        if (firstName == null || firstName.isBlank()) {
            throw new ValidationException("First name is required");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new ValidationException("Last name is required");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ValidationException("New password cannot be empty");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
        }
    }
}