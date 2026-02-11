package com.rongproject.JavaSprint5_2LibrarySystem.DTO;

import com.rongproject.JavaSprint5_2LibrarySystem.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserProfileDTO(

        @Schema(description = "The unique MySQL database ID of the user", example = "5")
        Long id,

        @Schema(description = "The unique username of the account", example = "john_doe")
        String username,

        @Schema(description = "The registered email address", example = "john@example.com")
        String email,

        @Schema(description = "The role assigned to the user", example = "MEMBER")
        UserRole userRole,

        @Schema(description = "The URL of the user's profile picture", example = "https://example.com/avatars/user5.png")
        String avatarUrl
) {
}
