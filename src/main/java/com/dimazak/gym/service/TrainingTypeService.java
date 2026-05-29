package com.dimazak.gym.service;

import com.dimazak.gym.dao.TrainingTypeDao;
import com.dimazak.gym.dto.TrainingTypeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainingTypeService {

    private static final Logger log = LoggerFactory.getLogger(TrainingTypeService.class);

    private final TrainingTypeDao trainingTypeDao;

    public TrainingTypeService(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Transactional(readOnly = true)
    public List<TrainingTypeResponse> getAllTrainingTypes() {
        log.info("Fetching all training types");
        return trainingTypeDao.findAll().stream()
                .map(type -> new TrainingTypeResponse(type.getId(), type.getTrainingTypeName()))
                .toList();
    }
}