package com.dimazak.gym.dao;

import com.dimazak.gym.model.Trainee;
import com.dimazak.gym.storage.InMemoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
public class TraineeDao {

    private static final Logger log = LoggerFactory.getLogger(TraineeDao.class);

    private InMemoryStorage storage;

    @Autowired
    public void setStorage(InMemoryStorage storage) {
        this.storage = storage;
    }

    public Trainee save(Trainee trainee) {
        if (trainee.getId() == null) {
            trainee.setId(storage.nextTraineeId());
        }
        storage.getTraineeStorage().put(trainee.getId(), trainee);
        log.debug("Saved trainee with id: {}", trainee.getId());
        return trainee;
    }

    public Optional<Trainee> findById(Long id) {
        log.debug("Finding trainee by id: {}", id);
        return Optional.ofNullable(storage.getTraineeStorage().get(id));
    }

    public Collection<Trainee> findAll() {
        log.debug("Finding all trainees");
        return storage.getTraineeStorage().values();
    }

    public void deleteById(Long id) {
        Trainee removed = storage.getTraineeStorage().remove(id);
        if (removed != null) {
            log.debug("Deleted trainee with id: {}", id);
        } else {
            log.warn("Attempted to delete non-existent trainee with id: {}", id);
        }
    }
}