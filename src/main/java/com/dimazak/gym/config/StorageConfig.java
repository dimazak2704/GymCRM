package com.dimazak.gym.config;

import com.dimazak.gym.model.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class StorageConfig {

    @Bean(name = "userStorage")
    public Map<Long, User> userStorage() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "traineeStorage")
    public Map<Long, Trainee> traineeStorage() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "trainerStorage")
    public Map<Long, Trainer> trainerStorage() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "trainingStorage")
    public Map<Long, Training> trainingStorage() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "trainingTypeStorage")
    public Map<Long, TrainingType> trainingTypeStorage() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "userIdSequence")
    public AtomicLong userIdSequence() {
        return new AtomicLong(0);
    }

    @Bean(name = "traineeIdSequence")
    public AtomicLong traineeIdSequence() {
        return new AtomicLong(0);
    }

    @Bean(name = "trainerIdSequence")
    public AtomicLong trainerIdSequence() {
        return new AtomicLong(0);
    }

    @Bean(name = "trainingIdSequence")
    public AtomicLong trainingIdSequence() {
        return new AtomicLong(0);
    }

    @Bean(name = "trainingTypeIdSequence")
    public AtomicLong trainingTypeIdSequence() {
        return new AtomicLong(0);
    }
}