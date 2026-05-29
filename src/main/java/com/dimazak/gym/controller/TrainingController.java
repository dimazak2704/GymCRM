package com.dimazak.gym.controller;

import com.dimazak.gym.dto.AddTrainingRequest;
import com.dimazak.gym.service.AuthenticationService;
import com.dimazak.gym.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainings")
@Tag(name = "Training", description = "Training management endpoints")
public class TrainingController {

    private static final Logger log = LoggerFactory.getLogger(TrainingController.class);

    private final TrainingService trainingService;
    private final AuthenticationService authenticationService;

    public TrainingController(TrainingService trainingService,
                              AuthenticationService authenticationService) {
        this.trainingService = trainingService;
        this.authenticationService = authenticationService;
    }

    @PostMapping
    @Operation(summary = "Add training", description = "Create a new training session")
    public ResponseEntity<Void> addTraining(
            @Valid @RequestBody AddTrainingRequest request,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-Password") String password) {
        log.info("Adding training: '{}' for trainee: {}, trainer: {}",
                request.trainingName(), request.traineeUsername(), request.trainerUsername());
        authenticationService.authenticate(username, password);

        trainingService.addTraining(
                request.traineeUsername(), request.trainerUsername(),
                request.trainingName(), request.trainingDate(),
                request.trainingDuration());

        return ResponseEntity.ok().build();
    }
}