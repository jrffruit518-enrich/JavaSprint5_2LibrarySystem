package com.rongproject.JavaSprint5_2LibrarySystem.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Composite status DTO used to determine user borrowing eligibility.
 * This acts as the 'Master Switch' for the frontend UI components.
 */
@Schema(description = "Response object detailing user's borrowing eligibility and current status")
public record UserStatusResponse(
        @Schema(description = "The master switch: true if user meets all conditions to borrow", example = "true")
        boolean canBorrow,

        @Schema(description = "Account status: true if the account is not manually or automatically disabled", example = "true")
        boolean isEnabled,

        @Schema(description = "Overdue status: true if user has books past their due date", example = "false")
        boolean hasOverdue,

        @Schema(description = "The total number of books currently borrowed by the user", example = "2")
        long borrowCount
) {}



