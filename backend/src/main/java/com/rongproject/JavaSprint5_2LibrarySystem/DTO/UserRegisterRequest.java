package com.rongproject.JavaSprint5_2LibrarySystem.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for new user registration.
 */
@Schema(description = "Request body for registering a new user account")
public record UserRegisterRequest(
        @Schema(description = "Unique username for the account", example = "library_user_01", minLength = 3, maxLength = 50)
        @NotBlank @Size(min = 3, max = 50)
        String username,

        @Schema(description = "Secure password for the account", example = "StrongPassword123!", minLength = 6)
        @NotBlank @Size(min = 6)
        String password,

        @Schema(description = "Valid email address for notifications and recovery", example = "user@example.com")
        @NotBlank @Email
        String email,

        @Schema(description = "Optional profile picture URL", example = "https://example.com/avatar.png")
        String avatarUrl
) {
}
