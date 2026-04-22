package com.dimazak.gym.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.dimazak.gym.model.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryStorage {

    private static final Logger log = LoggerFactory.getLogger(InMemoryStorage.class);

    private final Map<Long, User> userStorage = new ConcurrentHashMap<>();
    private final Map<Long, Trainee> traineeStorage = new ConcurrentHashMap<>();
    private final Map<Long, Trainer> trainerStorage = new ConcurrentHashMap<>();
    private final Map<Long, Training> trainingStorage = new ConcurrentHashMap<>();
    private final Map<Long, TrainingType> trainingTypeStorage = new ConcurrentHashMap<>();

    private final AtomicLong userIdSequence = new AtomicLong(0);
    private final AtomicLong traineeIdSequence = new AtomicLong(0);
    private final AtomicLong trainerIdSequence = new AtomicLong(0);
    private final AtomicLong trainingIdSequence = new AtomicLong(0);
    private final AtomicLong trainingTypeIdSequence = new AtomicLong(0);

    @Value("${storage.init.file:initial-data.json}")
    private String initFilePath;

    @PostConstruct
    public void init() {
        log.info("Initializing in-memory storage from file: {}", initFilePath);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(initFilePath)) {
            if (is == null) {
                log.warn("Initialization file '{}' not found on classpath. Starting with empty storage.", initFilePath);
                return;
            }

            Map<String, Object> data = mapper.readValue(is, new TypeReference<>() {});

            loadTrainingTypes(mapper, data);
            loadUsers(mapper, data);
            loadTrainees(mapper, data);
            loadTrainers(mapper, data);
            loadTrainings(mapper, data);

            log.info("Storage initialized successfully. Users: {}, Trainees: {}, Trainers: {}, Trainings: {}, TrainingTypes: {}",
                    userStorage.size(), traineeStorage.size(), trainerStorage.size(),
                    trainingStorage.size(), trainingTypeStorage.size());

        } catch (IOException e) {
            log.error("Failed to initialize storage from file: {}", initFilePath, e);
        }
    }

    private void loadTrainingTypes(ObjectMapper mapper, Map<String, Object> data) {
        if (data.containsKey("trainingTypes")) {
            List<TrainingType> types = mapper.convertValue(data.get("trainingTypes"),
                    new TypeReference<>() {});
            for (TrainingType type : types) {
                trainingTypeStorage.put(type.getId(), type);
                updateSequence(trainingTypeIdSequence, type.getId());
            }
            log.debug("Loaded {} training types", types.size());
        }
    }

    private void loadUsers(ObjectMapper mapper, Map<String, Object> data) {
        if (data.containsKey("users")) {
            List<User> users = mapper.convertValue(data.get("users"),
                    new TypeReference<>() {});
            for (User user : users) {
                userStorage.put(user.getId(), user);
                updateSequence(userIdSequence, user.getId());
            }
            log.debug("Loaded {} users", users.size());
        }
    }

    private void loadTrainees(ObjectMapper mapper, Map<String, Object> data) {
        if (data.containsKey("trainees")) {
            List<Trainee> trainees = mapper.convertValue(data.get("trainees"),
                    new TypeReference<>() {});
            for (Trainee trainee : trainees) {
                traineeStorage.put(trainee.getId(), trainee);
                updateSequence(traineeIdSequence, trainee.getId());
            }
            log.debug("Loaded {} trainees", trainees.size());
        }
    }

    private void loadTrainers(ObjectMapper mapper, Map<String, Object> data) {
        if (data.containsKey("trainers")) {
            List<Trainer> trainers = mapper.convertValue(data.get("trainers"),
                    new TypeReference<>() {});
            for (Trainer trainer : trainers) {
                trainerStorage.put(trainer.getId(), trainer);
                updateSequence(trainerIdSequence, trainer.getId());
            }
            log.debug("Loaded {} trainers", trainers.size());
        }
    }

    private void loadTrainings(ObjectMapper mapper, Map<String, Object> data) {
        if (data.containsKey("trainings")) {
            List<Training> trainings = mapper.convertValue(data.get("trainings"),
                    new TypeReference<>() {});
            for (Training training : trainings) {
                trainingStorage.put(training.getId(), training);
                updateSequence(trainingIdSequence, training.getId());
            }
            log.debug("Loaded {} trainings", trainings.size());
        }
    }

    private void updateSequence(AtomicLong sequence, Long id) {
        if (id != null) {
            sequence.updateAndGet(current -> Math.max(current, id));
        }
    }

    // --- Namespace accessors ---

    public Map<Long, User> getUserStorage() { return userStorage; }
    public Map<Long, Trainee> getTraineeStorage() { return traineeStorage; }
    public Map<Long, Trainer> getTrainerStorage() { return trainerStorage; }
    public Map<Long, Training> getTrainingStorage() { return trainingStorage; }
    public Map<Long, TrainingType> getTrainingTypeStorage() { return trainingTypeStorage; }

    // --- ID generators ---

    public Long nextUserId() { return userIdSequence.incrementAndGet(); }
    public Long nextTraineeId() { return traineeIdSequence.incrementAndGet(); }
    public Long nextTrainerId() { return trainerIdSequence.incrementAndGet(); }
    public Long nextTrainingId() { return trainingIdSequence.incrementAndGet(); }
    public Long nextTrainingTypeId() { return trainingTypeIdSequence.incrementAndGet(); }
}