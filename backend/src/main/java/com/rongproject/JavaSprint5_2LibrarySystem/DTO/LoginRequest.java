package com.rongproject.JavaSprint5_2LibrarySystem.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for user authentication.
 */
@Schema(description = "Request body for user login")
public record LoginRequest(
        @Schema(description = "The unique username of the account", example = "john_doe")
        @NotBlank(message = "Username cannot be empty")
        String username,

        @Schema(description = "The plain text password", example = "Secret123!")
        @NotBlank(message = "Password cannot be empty")
        String password
) {}