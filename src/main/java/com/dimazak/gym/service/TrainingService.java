package com.dimazak.gym.service;

import com.dimazak.gym.dao.TrainingDao;
import com.dimazak.gym.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private final TrainingDao trainingDao;

    public TrainingService(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    public Training createTraining(Long traineeId, Long trainerId, String trainingName,
                                   Long trainingTypeId, LocalDate trainingDate,
                                   int trainingDurationMinutes) {
        log.info("Creating training '{}' for traineeId: {}, trainerId: {}",
                trainingName, traineeId, trainerId);

        Training training = new Training(null, traineeId, trainerId, trainingName,
                trainingTypeId, trainingDate, trainingDurationMinutes);
        training = trainingDao.save(training);

        log.info("Training created with id: {}", training.getId());
        return training;
    }

    public Optional<Training> selectTraining(Long trainingId) {
        log.info("Selecting training with id: {}", trainingId);
        Optional<Training> training = trainingDao.findById(trainingId);
        if (training.isEmpty()) {
            log.warn("Training not found with id: {}", trainingId);
        }
        return training;
    }
}