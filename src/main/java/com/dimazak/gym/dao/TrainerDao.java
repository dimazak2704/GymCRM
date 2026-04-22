package com.dimazak.gym.dao;

import com.dimazak.gym.model.Trainer;
import com.dimazak.gym.storage.InMemoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
public class TrainerDao {

    private static final Logger log = LoggerFactory.getLogger(TrainerDao.class);

    private InMemoryStorage storage;

    @Autowired
    public void setStorage(InMemoryStorage storage) {
        this.storage = storage;
    }

    public Trainer save(Trainer trainer) {
        if (trainer.getId() == null) {
            trainer.setId(storage.nextTrainerId());
        }
        storage.getTrainerStorage().put(trainer.getId(), trainer);
        log.debug("Saved trainer with id: {}", trainer.getId());
        return trainer;
    }

    public Optional<Trainer> findById(Long id) {
        log.debug("Finding trainer by id: {}", id);
        return Optional.ofNullable(storage.getTrainerStorage().get(id));
    }

    public Collection<Trainer> findAll() {
        log.debug("Finding all trainers");
        return storage.getTrainerStorage().values();
    }
}