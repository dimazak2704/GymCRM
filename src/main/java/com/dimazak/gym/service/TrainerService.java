package com.dimazak.gym.service;

import com.dimazak.gym.dao.TrainerDao;
import com.dimazak.gym.dao.TrainingDao;
import com.dimazak.gym.dao.TrainingTypeDao;
import com.dimazak.gym.exception.EntityNotFoundException;
import com.dimazak.gym.exception.ValidationException;
import com.dimazak.gym.metrics.GymMetrics;
import com.dimazak.gym.model.Trainer;
import com.dimazak.gym.model.Training;
import com.dimazak.gym.model.TrainingType;
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

@Service
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final TrainerDao trainerDao;
    private final TrainingDao trainingDao;
    private final TrainingTypeDao trainingTypeDao;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;
    private final GymMetrics gymMetrics;

    public TrainerService(TrainerDao trainerDao,
                          TrainingDao trainingDao,
                          TrainingTypeDao trainingTypeDao,
                          UsernameGenerator usernameGenerator,
                          PasswordGenerator passwordGenerator,
                          PasswordEncoder passwordEncoder, GymMetrics gymMetrics) {
        this.trainerDao = trainerDao;
        this.trainingDao = trainingDao;
        this.trainingTypeDao = trainingTypeDao;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
        this.passwordEncoder = passwordEncoder;
        this.gymMetrics = gymMetrics;
    }

    @Transactional
    public Trainer createTrainer(String firstName, String lastName, Long specializationId) {
        log.info("Creating trainer profile for: {} {}", firstName, lastName);
        validateRequiredFields(firstName, lastName, specializationId);

        TrainingType specialization = trainingTypeDao.findById(specializationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Training type not found with id: " + specializationId));

        String username = usernameGenerator.generateUsername(firstName, lastName);
        String rawPassword = passwordGenerator.generatePassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = new User(null, firstName, lastName, username, encodedPassword, true);
        Trainer trainer = new Trainer(null, specialization, user);
        trainer = trainerDao.save(trainer);

        trainer.getUser().setPassword(rawPassword);

        log.info("Trainer profile created. Username: {}, TrainerId: {}",
                username, trainer.getId());
        gymMetrics.incrementTrainerRegistration();
        return trainer;
    }

    @Transactional(readOnly = true)
    public boolean matchCredentials(String username, String password) {
        log.debug("Matching credentials for trainer username: {}", username);
        return trainerDao.findByUsername(username)
                .map(trainer -> passwordEncoder.matches(password, trainer.getUser().getPassword()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Trainer getByUsername(String username) {
        log.info("Selecting trainer by username: {}", username);
        return trainerDao.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Trainer not found with username: {}", username);
                    return new EntityNotFoundException(
                            "Trainer not found with username: " + username);
                });
    }

    @Transactional(readOnly = true)
    public Trainer getProfileByUsername(String username) {
        log.info("Getting full profile for trainer: {}", username);
        Trainer trainer = getByUsername(username);
        Hibernate.initialize(trainer.getSpecialization());
        Hibernate.initialize(trainer.getTrainees());
        trainer.getTrainees().forEach(t -> Hibernate.initialize(t.getUser()));
        return trainer;
    }

    @Transactional
    public void changePassword(String username, String newPassword) {
        log.info("Changing password for trainer: {}", username);
        validatePassword(newPassword);

        Trainer trainer = getByUsername(username);
        trainer.getUser().setPassword(passwordEncoder.encode(newPassword));
        trainerDao.save(trainer);
        log.info("Password changed successfully for trainer: {}", username);
    }

    @Transactional
    public Trainer updateTrainer(String username, String firstName, String lastName,
                                 Long specializationId, boolean isActive) {
        log.info("Updating trainer profile: {}", username);
        validateRequiredFields(firstName, lastName, specializationId);

        TrainingType specialization = trainingTypeDao.findById(specializationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Training type not found with id: " + specializationId));

        Trainer trainer = getByUsername(username);
        User user = trainer.getUser();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setActive(isActive);
        trainer.setSpecialization(specialization);

        trainer = trainerDao.save(trainer);
        log.info("Trainer profile updated: {}", username);
        return trainer;
    }

    @Transactional
    public Trainer updateTrainerProfile(String username, String firstName,
                                        String lastName, boolean isActive) {
        log.info("Updating trainer profile (REST): {}", username);
        if (firstName == null || firstName.isBlank()) {
            throw new ValidationException("First name is required");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new ValidationException("Last name is required");
        }

        Trainer trainer = getByUsername(username);
        User user = trainer.getUser();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setActive(isActive);

        trainer = trainerDao.save(trainer);
        Hibernate.initialize(trainer.getSpecialization());
        Hibernate.initialize(trainer.getTrainees());
        trainer.getTrainees().forEach(t -> Hibernate.initialize(t.getUser()));

        log.info("Trainer profile updated: {}", username);
        return trainer;
    }

    @Transactional
    public void setActiveStatus(String username, boolean isActive) {
        log.info("Setting active status for trainer '{}' to: {}", username, isActive);
        Trainer trainer = getByUsername(username);

        if (trainer.getUser().isActive() == isActive) {
            throw new ValidationException(
                    "Trainer is already " + (isActive ? "active" : "deactivated"));
        }

        trainer.getUser().setActive(isActive);
        trainerDao.save(trainer);
        log.info("Trainer '{}' active status set to: {}", username, isActive);
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(String username, LocalDate fromDate,
                                              LocalDate toDate, String traineeName) {
        log.info("Getting trainings for trainer: {}", username);
        getByUsername(username);
        return trainingDao.findByTrainerWithFilters(username, fromDate, toDate, traineeName);
    }

    private void validateRequiredFields(String firstName, String lastName,
                                        Long specializationId) {
        if (firstName == null || firstName.isBlank()) {
            throw new ValidationException("First name is required");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new ValidationException("Last name is required");
        }
        if (specializationId == null) {
            throw new ValidationException("Specialization is required");
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