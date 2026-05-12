package com.dimazak.gym.facade;

import com.dimazak.gym.model.Trainee;
import com.dimazak.gym.model.Trainer;
import com.dimazak.gym.model.Training;
import com.dimazak.gym.service.AuthenticationService;
import com.dimazak.gym.service.TraineeService;
import com.dimazak.gym.service.TrainerService;
import com.dimazak.gym.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class GymFacade {

    private static final Logger log = LoggerFactory.getLogger(GymFacade.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final AuthenticationService authenticationService;

    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService,
                     AuthenticationService authenticationService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.authenticationService = authenticationService;
    }

    // ==================== Registration (no auth required) ====================

    public Trainee createTrainee(String firstName, String lastName,
                                 LocalDate dateOfBirth, String address) {
        log.info("Facade: creating trainee");
        return traineeService.createTrainee(firstName, lastName, dateOfBirth, address);
    }

    public Trainer createTrainer(String firstName, String lastName,
                                 Long specializationId) {
        log.info("Facade: creating trainer");
        return trainerService.createTrainer(firstName, lastName, specializationId);
    }

    // ==================== Authentication ====================

    public boolean matchTraineeCredentials(String username, String password) {
        log.info("Facade: matching trainee credentials for '{}'", username);
        return traineeService.matchCredentials(username, password);
    }

    public boolean matchTrainerCredentials(String username, String password) {
        log.info("Facade: matching trainer credentials for '{}'", username);
        return trainerService.matchCredentials(username, password);
    }

    // ==================== Trainee Operations (auth required) ====================

    public Trainee getTraineeByUsername(String username, String password) {
        authenticationService.authenticate(username, password);
        log.info("Facade: selecting trainee by username '{}'", username);
        return traineeService.getByUsername(username);
    }

    public void changeTraineePassword(String username, String oldPassword,
                                      String newPassword) {
        authenticationService.authenticate(username, oldPassword);
        log.info("Facade: changing trainee password for '{}'", username);
        traineeService.changePassword(username, newPassword);
    }

    public Trainee updateTrainee(String username, String password,
                                 String firstName, String lastName,
                                 LocalDate dateOfBirth, String address,
                                 boolean isActive) {
        authenticationService.authenticate(username, password);
        log.info("Facade: updating trainee '{}'", username);
        return traineeService.updateTrainee(username, firstName, lastName,
                dateOfBirth, address, isActive);
    }

    public void activateTrainee(String username, String password) {
        authenticationService.authenticate(username, password);
        log.info("Facade: activating trainee '{}'", username);
        traineeService.setActiveStatus(username, true);
    }

    public void deactivateTrainee(String username, String password) {
        authenticationService.authenticate(username, password);
        log.info("Facade: deactivating trainee '{}'", username);
        traineeService.setActiveStatus(username, false);
    }

    public void deleteTrainee(String username, String password) {
        authenticationService.authenticate(username, password);
        log.info("Facade: deleting trainee '{}'", username);
        traineeService.deleteByUsername(username);
    }

    public List<Training> getTraineeTrainings(String username, String password,
                                              LocalDate fromDate, LocalDate toDate,
                                              String trainerName,
                                              String trainingTypeName) {
        authenticationService.authenticate(username, password);
        log.info("Facade: getting trainee trainings for '{}'", username);
        return traineeService.getTraineeTrainings(username, fromDate, toDate,
                trainerName, trainingTypeName);
    }

    public List<Trainer> getUnassignedTrainers(String username, String password) {
        authenticationService.authenticate(username, password);
        log.info("Facade: getting unassigned trainers for '{}'", username);
        return traineeService.getUnassignedTrainers(username);
    }

    public Trainee updateTraineeTrainers(String username, String password,
                                         List<String> trainerUsernames) {
        authenticationService.authenticate(username, password);
        log.info("Facade: updating trainers list for '{}'", username);
        return traineeService.updateTrainersList(username, trainerUsernames);
    }

    // ==================== Trainer Operations (auth required) ====================

    public Trainer getTrainerByUsername(String username, String password) {
        authenticationService.authenticate(username, password);
        log.info("Facade: selecting trainer by username '{}'", username);
        return trainerService.getByUsername(username);
    }

    public void changeTrainerPassword(String username, String oldPassword,
                                      String newPassword) {
        authenticationService.authenticate(username, oldPassword);
        log.info("Facade: changing trainer password for '{}'", username);
        trainerService.changePassword(username, newPassword);
    }

    public Trainer updateTrainer(String username, String password,
                                 String firstName, String lastName,
                                 Long specializationId, boolean isActive) {
        authenticationService.authenticate(username, password);
        log.info("Facade: updating trainer '{}'", username);
        return trainerService.updateTrainer(username, firstName, lastName,
                specializationId, isActive);
    }

    public void activateTrainer(String username, String password) {
        authenticationService.authenticate(username, password);
        log.info("Facade: activating trainer '{}'", username);
        trainerService.setActiveStatus(username, true);
    }

    public void deactivateTrainer(String username, String password) {
        authenticationService.authenticate(username, password);
        log.info("Facade: deactivating trainer '{}'", username);
        trainerService.setActiveStatus(username, false);
    }

    public List<Training> getTrainerTrainings(String username, String password,
                                              LocalDate fromDate, LocalDate toDate,
                                              String traineeName) {
        authenticationService.authenticate(username, password);
        log.info("Facade: getting trainer trainings for '{}'", username);
        return trainerService.getTrainerTrainings(username, fromDate, toDate, traineeName);
    }

    // ==================== Training Operations (auth required) ====================

    public Training addTraining(String username, String password,
                                String traineeUsername, String trainerUsername,
                                String trainingName, Long trainingTypeId,
                                LocalDate trainingDate, int trainingDuration) {
        authenticationService.authenticate(username, password);
        log.info("Facade: adding training");
        return trainingService.addTraining(traineeUsername, trainerUsername,
                trainingName, trainingTypeId, trainingDate, trainingDuration);
    }
}