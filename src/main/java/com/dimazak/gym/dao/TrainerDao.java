package com.dimazak.gym.dao;

import com.dimazak.gym.model.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class TrainerDao {

    private static final Logger log = LoggerFactory.getLogger(TrainerDao.class);

    private final Map<Long, Trainer> trainerStorage;
    private final AtomicLong trainerIdSequence;

    public TrainerDao(@Qualifier("trainerStorage") Map<Long, Trainer> trainerStorage,
                      @Qualifier("trainerIdSequence") AtomicLong trainerIdSequence) {
        this.trainerStorage = trainerStorage;
        this.trainerIdSequence = trainerIdSequence;
    }

    public Trainer save(Trainer trainer) {
        if (trainer.getId() == null) {
            trainer.setId(trainerIdSequence.incrementAndGet());
        }
        trainerStorage.put(trainer.getId(), trainer);
        log.debug("Saved trainer with id: {}", trainer.getId());
        return trainer;
    }

    public Optional<Trainer> findById(Long id) {
        log.debug("Finding trainer by id: {}", id);
        return Optional.ofNullable(trainerStorage.get(id));
    }

    public Collection<Trainer> findAll() {
        log.debug("Finding all trainers");
        return trainerStorage.values();
    }
}