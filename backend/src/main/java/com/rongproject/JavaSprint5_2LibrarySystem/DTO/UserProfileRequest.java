package com.rongproject.JavaSprint5_2LibrarySystem.DTO;

import com.rongproject.JavaSprint5_2LibrarySystem.entities.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for a user to update their own profile")
public record UserProfileRequest(
        @Email(message = "Invalid email format")
        @Schema(description = "The new email address for the user", example = "new_email@example.com")
        String email,

        @Pattern(regexp = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",
                message = "Invalid avatar URL format")
        @Schema(description = "The new profile picture URL", example = "https://example.com/new-avatar.png")
        String avatarUrl,

        @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
        @Schema(description = "The new password (optional)", example = "newStrongPassword123")
        String password
) {
    // English Comment: Convert DTO to Entity for Service layer processing
    public void updateExistingUser(User existingUser) {
        if (this.email != null) existingUser.setEmail(this.email);
        if (this.avatarUrl != null) existingUser.setAvatarUrl(this.avatarUrl);
        // Note: Password update should be handled separately in Service for encoding
    }
}