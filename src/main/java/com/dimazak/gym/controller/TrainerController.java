package com.dimazak.gym.controller;

import com.dimazak.gym.dto.*;
import com.dimazak.gym.mapper.EntityMapper;
import com.dimazak.gym.model.Trainer;
import com.dimazak.gym.model.Training;
import com.dimazak.gym.service.AuthenticationService;
import com.dimazak.gym.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @Operation(summary = "Register trainer", description = "Create a new trainer profile. No authentication required.")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody TrainerRegistrationRequest request) {
        log.info("Registering new trainer: {} {}", request.firstName(), request.lastName());

        Trainer trainer = trainerService.createTrainer(
                request.firstName(), request.lastName(), request.specializationId());

        RegistrationResponse response = new RegistrationResponse(
                trainer.getUser().getUsername(),
                trainer.getUser().getPassword());

        log.info("Trainer registered successfully with username: {}", response.username());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainer profile", description = "Get trainer profile by username")
    public ResponseEntity<TrainerProfileResponse> getProfile(
            @Parameter(description = "Trainer username") @PathVariable String username,
            @Parameter(description = "User password") @RequestHeader("X-Password") String password) {
        log.info("Getting profile for trainer: {}", username);
        authenticationService.authenticate(username, password);

        Trainer trainer = trainerService.getProfileByUsername(username);
        log.info("Profile retrieved for trainer: {}", username);
        return ResponseEntity.ok(mapper.toTrainerProfileResponse(trainer));
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update trainer profile",
            description = "Update trainer profile information. Specialization is read-only.")
    public ResponseEntity<UpdateTrainerResponse> updateProfile(
            @Parameter(description = "Trainer username") @PathVariable String username,
            @Valid @RequestBody UpdateTrainerRequest request,
            @Parameter(description = "User password") @RequestHeader("X-Password") String password) {
        log.info("Updating trainer profile: {}", username);
        authenticationService.authenticate(username, password);

        Trainer trainer = trainerService.updateTrainerProfile(
                username, request.firstName(), request.lastName(), request.isActive());

        log.info("Trainer profile updated: {}", username);
        return ResponseEntity.ok(mapper.toUpdateTrainerResponse(trainer));
    }

    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get trainer trainings list",
            description = "Get trainer's trainings with optional filters")
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainings(
            @Parameter(description = "Trainer username") @PathVariable String username,
            @Parameter(description = "User password") @RequestHeader("X-Password") String password,
            @Parameter(description = "Filter from date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @Parameter(description = "Filter to date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @Parameter(description = "Filter by trainee name") @RequestParam(required = false) String traineeName) {
        log.info("Getting trainings for trainer: {} with filters [from={}, to={}, trainee={}]",
                username, periodFrom, periodTo, traineeName);
        authenticationService.authenticate(username, password);

        List<Training> trainings = trainerService.getTrainerTrainings(
                username, periodFrom, periodTo, traineeName);

        log.info("Found {} trainings for trainer: {}", trainings.size(), username);
        return ResponseEntity.ok(trainings.stream().map(mapper::toTrainerTrainingResponse).toList());
    }

    @PatchMapping("/{username}/activate")
    @Operation(summary = "Activate/De-activate trainer",
            description = "Set trainer active status")
    public ResponseEntity<Void> updateActiveStatus(
            @Parameter(description = "Trainer username") @PathVariable String username,
            @Valid @RequestBody ActivateDeactivateRequest request,
            @Parameter(description = "User password") @RequestHeader("X-Password") String password) {
        log.info("Updating active status for trainer: {} to: {}", username, request.isActive());
        authenticationService.authenticate(username, password);

        trainerService.setActiveStatus(username, request.isActive());
        log.info("Active status updated for trainer: {} → {}", username, request.isActive());
        return ResponseEntity.ok().build();
    }
}