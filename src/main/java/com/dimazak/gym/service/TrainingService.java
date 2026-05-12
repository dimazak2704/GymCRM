package com.dimazak.gym.service;

import com.dimazak.gym.dao.TraineeDao;
import com.dimazak.gym.dao.TrainerDao;
import com.dimazak.gym.dao.TrainingDao;
import com.dimazak.gym.dao.TrainingTypeDao;
import com.dimazak.gym.exception.EntityNotFoundException;
import com.dimazak.gym.exception.ValidationException;
import com.dimazak.gym.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private final TrainingDao trainingDao;
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final TrainingTypeDao trainingTypeDao;

    public TrainingService(TrainingDao trainingDao,
                           TraineeDao traineeDao,
                           TrainerDao trainerDao,
                           TrainingTypeDao trainingTypeDao) {
        this.trainingDao = trainingDao;
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.trainingTypeDao = trainingTypeDao;
    }

    @Transactional
    public Training addTraining(String traineeUsername, String trainerUsername,
                                String trainingName, Long trainingTypeId,
                                LocalDate trainingDate, int trainingDuration) {
        log.info("Adding training '{}' for trainee: '{}', trainer: '{}'",
                trainingName, traineeUsername, trainerUsername);

        validateTrainingFields(trainingName, trainingTypeId, trainingDate, trainingDuration);

        Trainee trainee = traineeDao.findByUsername(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trainee not found: " + traineeUsername));

        Trainer trainer = trainerDao.findByUsername(trainerUsername)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trainer not found: " + trainerUsername));

        TrainingType trainingType = trainingTypeDao.findById(trainingTypeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Training type not found with id: " + trainingTypeId));

        Training training = new Training(null, trainee, trainer,
                trainingName, trainingType, trainingDate, trainingDuration);
        training = trainingDao.save(training);

        log.info("Training created with id: {}", training.getId());
        return training;
    }

    private void validateTrainingFields(String trainingName, Long trainingTypeId,
                                        LocalDate trainingDate, int trainingDuration) {
        if (trainingName == null || trainingName.isBlank()) {
            throw new ValidationException("Training name is required");
        }
        if (trainingTypeId == null) {
            throw new ValidationException("Training type is required");
        }
        if (trainingDate == null) {
            throw new ValidationException("Training date is required");
        }
        if (trainingDuration <= 0) {
            throw new ValidationException("Training duration must be positive");
        }
    }
}