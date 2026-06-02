package com.dimazak.gym.controller;

import com.dimazak.gym.dto.ChangePasswordRequest;
import com.dimazak.gym.dto.LoginRequest;
import com.dimazak.gym.service.AuthenticationService;
import com.dimazak.gym.service.TraineeService;
import com.dimazak.gym.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Authentication", description = "Login and password management")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationService authenticationService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;

    public AuthController(AuthenticationService authenticationService,
                          TraineeService traineeService,
                          TrainerService trainerService) {
        this.authenticationService = authenticationService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Verify user credentials")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.username());
        authenticationService.authenticate(request.username(), request.password());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    @Operation(summary = "Change password", description = "Change user password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("Password change request for user: {}", request.username());
        authenticationService.authenticate(request.username(), request.oldPassword());

        if (traineeService.existsByUsername(request.username())) {
            traineeService.changePassword(request.username(), request.newPassword());
        } else {
            trainerService.changePassword(request.username(), request.newPassword());
        }

        return ResponseEntity.ok().build();
    }
}