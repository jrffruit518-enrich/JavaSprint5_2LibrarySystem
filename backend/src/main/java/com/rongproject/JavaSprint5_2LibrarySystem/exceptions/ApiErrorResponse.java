package com.rongproject.JavaSprint5_2LibrarySystem.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ApiErrorResponse(int status, String message,
                               @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                               LocalDateTime localDateTime) {
}
