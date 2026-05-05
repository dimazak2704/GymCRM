package com.dimazak.gym.dao;

import com.dimazak.gym.model.Trainee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class TraineeDao {

    private static final Logger log = LoggerFactory.getLogger(TraineeDao.class);

    private final Map<Long, Trainee> traineeStorage;
    private final AtomicLong traineeIdSequence;

    public TraineeDao(@Qualifier("traineeStorage") Map<Long, Trainee> traineeStorage,
                      @Qualifier("traineeIdSequence") AtomicLong traineeIdSequence) {
        this.traineeStorage = traineeStorage;
        this.traineeIdSequence = traineeIdSequence;
    }

    public Trainee save(Trainee trainee) {
        if (trainee.getId() == null) {
            trainee.setId(traineeIdSequence.incrementAndGet());
        }
        traineeStorage.put(trainee.getId(), trainee);
        log.debug("Saved trainee with id: {}", trainee.getId());
        return trainee;
    }

    public Optional<Trainee> findById(Long id) {
        log.debug("Finding trainee by id: {}", id);
        return Optional.ofNullable(traineeStorage.get(id));
    }

    public Collection<Trainee> findAll() {
        log.debug("Finding all trainees");
        return traineeStorage.values();
    }

    public void deleteById(Long id) {
        Trainee removed = traineeStorage.remove(id);
        if (removed != null) {
            log.debug("Deleted trainee with id: {}", id);
        } else {
            log.warn("Attempted to delete non-existent trainee with id: {}", id);
        }
    }
}