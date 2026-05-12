package com.dimazak.gym.service;

import com.dimazak.gym.dao.TrainerDao;
import com.dimazak.gym.dao.TrainingDao;
import com.dimazak.gym.dao.TrainingTypeDao;
import com.dimazak.gym.exception.EntityNotFoundException;
import com.dimazak.gym.exception.ValidationException;
import com.dimazak.gym.model.Trainer;
import com.dimazak.gym.model.Training;
import com.dimazak.gym.model.TrainingType;
import com.dimazak.gym.model.User;
import com.dimazak.gym.util.PasswordGenerator;
import com.dimazak.gym.util.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private final TrainerDao trainerDao;
    private final TrainingDao trainingDao;
    private final TrainingTypeDao trainingTypeDao;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;

    public TrainerService(TrainerDao trainerDao,
                          TrainingDao trainingDao,
                          TrainingTypeDao trainingTypeDao,
                          UsernameGenerator usernameGenerator,
                          PasswordGenerator passwordGenerator) {
        this.trainerDao = trainerDao;
        this.trainingDao = trainingDao;
        this.trainingTypeDao = trainingTypeDao;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
    }

    @Transactional
    public Trainer createTrainer(String firstName, String lastName,
                                 Long specializationId) {
        log.info("Creating trainer profile for: {} {}", firstName, lastName);

        validateRequiredFields(firstName, lastName, specializationId);

        TrainingType specialization = trainingTypeDao.findById(specializationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Training type not found with id: " + specializationId));

        String username = usernameGenerator.generateUsername(firstName, lastName);
        String password = passwordGenerator.generatePassword();

        User user = new User(null, firstName, lastName, username, password, true);

        Trainer trainer = new Trainer(null, specialization, user);
        trainer = trainerDao.save(trainer);

        log.info("Trainer profile created. Username: {}, TrainerId: {}",
                username, trainer.getId());
        return trainer;
    }

    @Transactional(readOnly = true)
    public boolean matchCredentials(String username, String password) {
        log.debug("Matching credentials for trainer username: {}", username);
        return trainerDao.findByUsername(username)
                .map(trainer -> trainer.getUser().getPassword().equals(password))
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

    @Transactional
    public void changePassword(String username, String newPassword) {
        log.info("Changing password for trainer: {}", username);

        if (newPassword == null || newPassword.isBlank()) {
            throw new ValidationException("New password cannot be empty");
        }

        Trainer trainer = getByUsername(username);
        trainer.getUser().setPassword(newPassword);
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
        return trainingDao.findByTrainerUsernameAndCriteria(
                username, fromDate, toDate, traineeName);
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
}