package com.rongproject.JavaSprint5_2LibrarySystem.DTO;

import com.rongproject.JavaSprint5_2LibrarySystem.enums.LogStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Optimized Response DTO for borrowing events.
 * Replaces technical IDs with readable names and adds a user-friendly message.
 */
@Schema(description = "Response object detailing a borrowing record with user-friendly messages")
public record LogResponse(
        @Schema(description = "The unique MongoDB document ID for the log", example = "65c4f1a2b3c4d5e6f7a8b9c0")
        String logId,

        @Schema(description = "The display name of the user", example = "张三")
        String username,

        @Schema(description = "The title of the borrowed book", example = "Effective Java")
        String bookTitle,

        @Schema(description = "The date and time when the book was borrowed", example = "2026-02-09T10:00:00")
        LocalDateTime borrowDate,

        @Schema(description = "The date and time when the book was returned (null if not yet returned)", example = "2026-03-11T10:00:00")
        LocalDateTime returnDate,

        @Schema(description = "The current status of the borrowing event", example = "BORROWED")
        LogStatus status,

        @Schema(description = "A personalized message for the user",
                example = "张三，你已成功借阅了《Effective Java》，请于 2026-03-11 前归还。")
        String message
) {}