package com.rongproject.JavaSprint5_2LibrarySystem.DTO;

import com.rongproject.JavaSprint5_2LibrarySystem.enums.LogStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Optimized Response DTO for borrowing events.
 * English Comment: Updated to match BorrowLog entity (Long IDs) and fixed the missing message issue.
 */
@Schema(description = "借阅记录响应对象")
public record LogResponse(
        @Schema(description = "MongoDB 日志ID")
        String logId,

        @Schema(description = "用户ID (来自MySQL)")
        Long userId,

        @Schema(description = "用户名")
        String username,

        @Schema(description = "图书ID (来自MySQL)")
        Long bookId,

        @Schema(description = "图书标题")
        String bookTitle,

        @Schema(description = "借书时间")
        LocalDateTime borrowDate,

        @Schema(description = "归还时间 (未归还则为空)")
        LocalDateTime returnDate,

        @Schema(description = "借阅状态")
        LogStatus status,

        @Schema(description = "前端显示的友好提示消息")
        String message
) {}