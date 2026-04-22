package com.dimazak.gym.dao;

import com.dimazak.gym.model.Training;
import com.dimazak.gym.storage.InMemoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
public class TrainingDao {

    private static final Logger log = LoggerFactory.getLogger(TrainingDao.class);

    private InMemoryStorage storage;

    @Autowired
    public void setStorage(InMemoryStorage storage) {
        this.storage = storage;
    }

    public Training save(Training training) {
        if (training.getId() == null) {
            training.setId(storage.nextTrainingId());
        }
        storage.getTrainingStorage().put(training.getId(), training);
        log.debug("Saved training with id: {}", training.getId());
        return training;
    }

    public Optional<Training> findById(Long id) {
        log.debug("Finding training by id: {}", id);
        return Optional.ofNullable(storage.getTrainingStorage().get(id));
    }

    public Collection<Training> findAll() {
        log.debug("Finding all trainings");
        return storage.getTrainingStorage().values();
    }
}