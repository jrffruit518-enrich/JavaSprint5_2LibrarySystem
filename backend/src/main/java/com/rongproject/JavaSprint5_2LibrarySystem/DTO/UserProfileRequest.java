package com.rongproject.JavaSprint5_2LibrarySystem.DTO;

import com.rongproject.JavaSprint5_2LibrarySystem.entities.User;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for users to update their own profile information.
 * This ensures they can only touch safe fields (email, avatar, password).
 */
@Schema(description = "Request body for a user to update their own profile")
public record UserProfileRequest(
        @Schema(description = "The new email address for the user", example = "new_email@example.com")
        String email,

        @Schema(description = "The new profile picture URL", example = "https://example.com/new-avatar.png")
        String avatarUrl,

        @Schema(description = "The new password (optional)", example = "newStrongPassword123")
        String password
) {
    // English Comment: Convert DTO to Entity for Service layer processing
    public User toEntity() {
        User user = new User();
        user.setEmail(this.email);
        user.setAvatarUrl(this.avatarUrl);
        user.setPassword(this.password); // Note: Encoding will happen in Service
        return user;
    }
}
