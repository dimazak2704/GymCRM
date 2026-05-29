package com.dimazak.gym.controller;

import com.dimazak.gym.dto.*;
import com.dimazak.gym.mapper.EntityMapper;
import com.dimazak.gym.model.Trainee;
import com.dimazak.gym.model.Trainer;
import com.dimazak.gym.model.Training;
import com.dimazak.gym.service.AuthenticationService;
import com.dimazak.gym.service.TraineeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trainees")
@Tag(name = "Trainee", description = "Trainee management endpoints")
public class TraineeController {

    private static final Logger log = LoggerFactory.getLogger(TraineeController.class);

    private final TraineeService traineeService;
    private final AuthenticationService authenticationService;
    private final EntityMapper mapper;

    public TraineeController(TraineeService traineeService,
                             AuthenticationService authenticationService,
                             EntityMapper mapper) {
        this.traineeService = traineeService;
        this.authenticationService = authenticationService;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Register trainee", description = "Create a new trainee profile")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody TraineeRegistrationRequest request) {
        log.info("Registering new trainee: {} {}", request.firstName(), request.lastName());

        Trainee trainee = traineeService.createTrainee(
                request.firstName(), request.lastName(),
                request.dateOfBirth(), request.address());

        RegistrationResponse response = new RegistrationResponse(
                trainee.getUser().getUsername(),
                trainee.getUser().getPassword());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainee profile", description = "Get trainee profile by username")
    public ResponseEntity<TraineeProfileResponse> getProfile(
            @PathVariable String username,
            @RequestHeader("X-Password") String password) {
        log.info("Getting profile for trainee: {}", username);
        authenticationService.authenticate(username, password);

        Trainee trainee = traineeService.getProfileByUsername(username);
        return ResponseEntity.ok(mapper.toTraineeProfileResponse(trainee));
    }

    @PutMapping
    @Operation(summary = "Update trainee profile", description = "Update trainee profile information")
    public ResponseEntity<UpdateTraineeResponse> updateProfile(
            @Valid @RequestBody UpdateTraineeRequest request,
            @RequestHeader("X-Password") String password) {
        log.info("Updating trainee profile: {}", request.username());
        authenticationService.authenticate(request.username(), password);

        Trainee trainee = traineeService.updateTrainee(
                request.username(), request.firstName(), request.lastName(),
                request.dateOfBirth(), request.address(), request.isActive());

        return ResponseEntity.ok(mapper.toUpdateTraineeResponse(trainee));
    }

    @DeleteMapping("/{username}")
    @Operation(summary = "Delete trainee profile", description = "Hard delete trainee and cascade trainings")
    public ResponseEntity<Void> deleteProfile(
            @PathVariable String username,
            @RequestHeader("X-Password") String password) {
        log.info("Deleting trainee profile: {}", username);
        authenticationService.authenticate(username, password);

        traineeService.deleteByUsername(username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/unassigned-trainers")
    @Operation(summary = "Get unassigned trainers",
            description = "Get active trainers not assigned to the trainee")
    public ResponseEntity<List<TrainerSummary>> getUnassignedTrainers(
            @PathVariable String username,
            @RequestHeader("X-Password") String password) {
        log.info("Getting unassigned trainers for: {}", username);
        authenticationService.authenticate(username, password);

        List<Trainer> trainers = traineeService.getUnassignedTrainers(username);
        return ResponseEntity.ok(trainers.stream().map(mapper::toTrainerSummary).toList());
    }

    @PutMapping("/{username}/trainers")
    @Operation(summary = "Update trainee's trainer list",
            description = "Replace trainee's trainer list with the provided one")
    public ResponseEntity<List<TrainerSummary>> updateTrainersList(
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeTrainersRequest request,
            @RequestHeader("X-Password") String password) {
        log.info("Updating trainers list for trainee: {}", username);
        authenticationService.authenticate(username, password);

        Trainee trainee = traineeService.updateTrainersList(
                request.traineeUsername(), request.trainerUsernames());

        List<TrainerSummary> response = trainee.getTrainers().stream()
                .map(mapper::toTrainerSummary)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get trainee trainings list",
            description = "Get trainee's trainings with optional filters")
    public ResponseEntity<List<TraineeTrainingResponse>> getTrainings(
            @PathVariable String username,
            @RequestHeader("X-Password") String password,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType) {
        log.info("Getting trainings for trainee: {}", username);
        authenticationService.authenticate(username, password);

        List<Training> trainings = traineeService.getTraineeTrainings(
                username, periodFrom, periodTo, trainerName, trainingType);

        return ResponseEntity.ok(trainings.stream().map(mapper::toTraineeTrainingResponse).toList());
    }

    @PatchMapping("/{username}/activate")
    @Operation(summary = "Activate/De-activate trainee",
            description = "Toggle trainee active status (not idempotent)")
    public ResponseEntity<Void> updateActiveStatus(
            @PathVariable String username,
            @Valid @RequestBody ActivateDeactivateRequest request,
            @RequestHeader("X-Password") String password) {
        log.info("Updating active status for trainee: {} to: {}", username, request.isActive());
        authenticationService.authenticate(username, password);

        traineeService.setActiveStatus(request.username(), request.isActive());
        return ResponseEntity.ok().build();
    }
}