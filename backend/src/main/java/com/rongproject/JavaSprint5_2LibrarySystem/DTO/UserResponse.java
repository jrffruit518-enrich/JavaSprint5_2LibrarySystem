package com.rongproject.JavaSprint5_2LibrarySystem.DTO;

import com.rongproject.JavaSprint5_2LibrarySystem.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object representing user profile details.
 */
@Schema(description = "Response object containing non-sensitive user profile information")
public record UserResponse(
        @Schema(description = "The unique MySQL database ID of the user", example = "5")
        Long id,

        @Schema(description = "The unique username of the account", example = "john_doe")
        String username,

        @Schema(description = "The registered email address", example = "john@example.com")
        String email,

        @Schema(description = "The role assigned to the user", example = "MEMBER")
        UserRole userRole,

        @Schema(description = "Whether the account is currently active and allowed to borrow", example = "true")
        Boolean enabled,

        // English Comment: If true, the account was locked by an Admin and won't be auto-unlocked by returning books
        @Schema(description = "Manual lock status. If true, only an Admin can unlock this account.", example = "false")
        Boolean manualLock,

        @Schema(description = "The URL of the user's profile picture", example = "https://example.com/avatars/user5.png")
        String avatarUrl
) {
        public UserResponse {
                if (enabled == null) enabled = true;
                if (manualLock == null) manualLock = false;
                if (avatarUrl == null) avatarUrl = ""; // 给 avatar 一个空字符串而非 null
        }
}