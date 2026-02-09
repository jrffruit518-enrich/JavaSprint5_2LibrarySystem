package com.rongproject.JavaSprint5_2LibrarySystem.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO used by the Controller to capture incoming borrowing/returning requests.
 */
@Schema(description = "Request object for borrowing or returning a book")
public record BorrowOperationRequest(
        @Schema(description = "The unique MySQL ID of the user", example = "5")
        @NotNull(message = "User ID is required")
        Long userId,

        @Schema(description = "The unique MySQL ID of the book", example = "1024")
        @NotNull(message = "Book ID is required")
        Long bookId
) {}
