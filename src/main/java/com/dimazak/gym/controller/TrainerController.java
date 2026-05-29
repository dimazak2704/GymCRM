package com.dimazak.gym.controller;

import com.dimazak.gym.dto.*;
import com.dimazak.gym.mapper.EntityMapper;
import com.dimazak.gym.model.Trainer;
import com.dimazak.gym.model.Training;
import com.dimazak.gym.service.AuthenticationService;
import com.dimazak.gym.service.TrainerService;
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
@RequestMapping("/api/trainers")
@Tag(name = "Trainer", description = "Trainer management endpoints")
public class TrainerController {

    private static final Logger log = LoggerFactory.getLogger(TrainerController.class);

    private final TrainerService trainerService;
    private final AuthenticationService authenticationService;
    private final EntityMapper mapper;

    public TrainerController(TrainerService trainerService,
                             AuthenticationService authenticationService,
                             EntityMapper mapper) {
        this.trainerService = trainerService;
        this.authenticationService = authenticationService;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Register trainer", description = "Create a new trainer profile")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody TrainerRegistrationRequest request) {
        log.info("Registering new trainer: {} {}", request.firstName(), request.lastName());

        Trainer trainer = trainerService.createTrainer(
                request.firstName(), request.lastName(), request.specializationId());

        RegistrationResponse response = new RegistrationResponse(
                trainer.getUser().getUsername(),
                trainer.getUser().getPassword());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainer profile", description = "Get trainer profile by username")
    public ResponseEntity<TrainerProfileResponse> getProfile(
            @PathVariable String username,
            @RequestHeader("X-Password") String password) {
        log.info("Getting profile for trainer: {}", username);
        authenticationService.authenticate(username, password);

        Trainer trainer = trainerService.getProfileByUsername(username);
        return ResponseEntity.ok(mapper.toTrainerProfileResponse(trainer));
    }

    @PutMapping
    @Operation(summary = "Update trainer profile", description = "Update trainer profile (specialization is read-only)")
    public ResponseEntity<UpdateTrainerResponse> updateProfile(
            @Valid @RequestBody UpdateTrainerRequest request,
            @RequestHeader("X-Password") String password) {
        log.info("Updating trainer profile: {}", request.username());
        authenticationService.authenticate(request.username(), password);

        Trainer trainer = trainerService.updateTrainerProfile(
                request.username(), request.firstName(),
                request.lastName(), request.isActive());

        return ResponseEntity.ok(mapper.toUpdateTrainerResponse(trainer));
    }

    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get trainer trainings list",
            description = "Get trainer's trainings with optional filters")
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainings(
            @PathVariable String username,
            @RequestHeader("X-Password") String password,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String traineeName) {
        log.info("Getting trainings for trainer: {}", username);
        authenticationService.authenticate(username, password);

        List<Training> trainings = trainerService.getTrainerTrainings(
                username, periodFrom, periodTo, traineeName);

        return ResponseEntity.ok(trainings.stream().map(mapper::toTrainerTrainingResponse).toList());
    }

    @PatchMapping("/{username}/activate")
    @Operation(summary = "Activate/De-activate trainer",
            description = "Toggle trainer active status (not idempotent)")
    public ResponseEntity<Void> updateActiveStatus(
            @PathVariable String username,
            @Valid @RequestBody ActivateDeactivateRequest request,
            @RequestHeader("X-Password") String password) {
        log.info("Updating active status for trainer: {} to: {}", username, request.isActive());
        authenticationService.authenticate(username, password);

        trainerService.setActiveStatus(request.username(), request.isActive());
        return ResponseEntity.ok().build();
    }
}