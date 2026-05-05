package com.dimazak.gym.dao;

import com.dimazak.gym.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class TrainingDao {

    private static final Logger log = LoggerFactory.getLogger(TrainingDao.class);

    private final Map<Long, Training> trainingStorage;
    private final AtomicLong trainingIdSequence;

    public TrainingDao(@Qualifier("trainingStorage") Map<Long, Training> trainingStorage,
                       @Qualifier("trainingIdSequence") AtomicLong trainingIdSequence) {
        this.trainingStorage = trainingStorage;
        this.trainingIdSequence = trainingIdSequence;
    }

    public Training save(Training training) {
        if (training.getId() == null) {
            training.setId(trainingIdSequence.incrementAndGet());
        }
        trainingStorage.put(training.getId(), training);
        log.debug("Saved training with id: {}", training.getId());
        return training;
    }

    public Optional<Training> findById(Long id) {
        log.debug("Finding training by id: {}", id);
        return Optional.ofNullable(trainingStorage.get(id));
    }

    public Collection<Training> findAll() {
        log.debug("Finding all trainings");
        return trainingStorage.values();
    }
}