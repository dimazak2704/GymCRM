package com.dimazak.gym.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ActivateDeactivateRequest(
        @NotBlank(message = "Username is required") String username,
        @NotNull(message = "Is Active is required") Boolean isActive
) {}