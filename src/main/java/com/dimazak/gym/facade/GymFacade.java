package com.dimazak.gym.facade;

import com.dimazak.gym.model.Trainee;
import com.dimazak.gym.model.Trainer;
import com.dimazak.gym.model.Training;
import com.dimazak.gym.service.TraineeService;
import com.dimazak.gym.service.TrainerService;
import com.dimazak.gym.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class GymFacade {

    private static final Logger log = LoggerFactory.getLogger(GymFacade.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }


    public Trainee createTrainee(String firstName, String lastName,
                                 LocalDate dateOfBirth, String address, boolean isActive) {
        log.info("Facade: creating trainee");
        return traineeService.createTrainee(firstName, lastName, dateOfBirth, address, isActive);
    }

    public Trainee updateTrainee(Long traineeId, LocalDate dateOfBirth, String address) {
        log.info("Facade: updating trainee id={}", traineeId);
        return traineeService.updateTrainee(traineeId, dateOfBirth, address);
    }

    public void deleteTrainee(Long traineeId) {
        log.info("Facade: deleting trainee id={}", traineeId);
        traineeService.deleteTrainee(traineeId);
    }

    public Optional<Trainee> selectTrainee(Long traineeId) {
        log.info("Facade: selecting trainee id={}", traineeId);
        return traineeService.selectTrainee(traineeId);
    }

    // --- Trainer operations ---

    public Trainer createTrainer(String firstName, String lastName,
                                 Long specialization, boolean isActive) {
        log.info("Facade: creating trainer");
        return trainerService.createTrainer(firstName, lastName, specialization, isActive);
    }

    public Trainer updateTrainer(Long trainerId, Long specialization) {
        log.info("Facade: updating trainer id={}", trainerId);
        return trainerService.updateTrainer(trainerId, specialization);
    }

    public Optional<Trainer> selectTrainer(Long trainerId) {
        log.info("Facade: selecting trainer id={}", trainerId);
        return trainerService.selectTrainer(trainerId);
    }

    // --- Training operations ---

    public Training createTraining(Long traineeId, Long trainerId, String trainingName,
                                   Long trainingTypeId, LocalDate trainingDate,
                                   int trainingDurationMinutes) {
        log.info("Facade: creating training");
        return trainingService.createTraining(traineeId, trainerId, trainingName,
                trainingTypeId, trainingDate, trainingDurationMinutes);
    }

    public Optional<Training> selectTraining(Long trainingId) {
        log.info("Facade: selecting training id={}", trainingId);
        return trainingService.selectTraining(trainingId);
    }
}