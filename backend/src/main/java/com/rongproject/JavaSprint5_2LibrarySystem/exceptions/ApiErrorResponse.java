package com.rongproject.JavaSprint5_2LibrarySystem.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Standard API error response for all endpoints.
 */
@Schema(description = "Standard structure for error messages across the API")
public record ApiErrorResponse(
        @Schema(description = "The HTTP status code", example = "404")
        int status,

        @Schema(description = "The short description of the error", example = "Not Found")
        String error,

        @Schema(description = "Detailed error message explaining what went wrong", example = "The requested book was not found in our records.")
        String message,

        @Schema(description = "The precise time the error occurred", example = "2026-02-09 15:30:45")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp
) {}