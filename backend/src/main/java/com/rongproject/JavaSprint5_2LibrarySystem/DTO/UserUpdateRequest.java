package com.rongproject.JavaSprint5_2LibrarySystem.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for updating user profile.
 * No validation constraints added as per request.
 */
@Schema(description = "Request body for updating user profile information")
public record UserUpdateRequest(
        @Schema(description = "The new email address for the user", example = "new_email@example.com")
        String email,

        @Schema(description = "The new profile picture URL", example = "https://example.com/new-avatar.png")
        String avatarUrl
) {}
