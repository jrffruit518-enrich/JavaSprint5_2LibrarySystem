package com.rongproject.JavaSprint5_2LibrarySystem.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 图书馆项目 - 全局异常处理
 * Jules Fix: Added deep request diagnostic logging to handleAll exception.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handle Validation Errors (e.g., @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn(">>> [JULES WARN] Validation failed: {}", details);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed: " + details);
    }

    // 2. Handle JSON Format Errors
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleJsonError(HttpMessageNotReadableException ex) {
        log.warn(">>> [JULES WARN] JSON parse error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid JSON format or type mismatch");
    }

    // 3. Handle Resource Not Found (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        log.warn(">>> [JULES WARN] Resource not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 4. Handle Illegal States (409 Conflict)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException ex) {
        log.warn(">>> [JULES WARN] Business logic conflict: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // 5. Handle Access Denied (403 Forbidden)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn(">>> [JULES WARN] Access denied: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "You do not have permission to access this resource.");
    }

    // 6. Handle Already Exists (409 Conflict)
    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleAlreadyExistsException(AlreadyExistsException ex) {
        log.warn(">>> [JULES WARN] Already exists: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // 7. Catch-all for unexpected internal errors (500)
    // Jules Fix: Added HttpServletRequest to log the exact URI causing the error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAll(Exception ex, HttpServletRequest request) {
        log.error("--- [JULES CRASH REPORT START] ---");
        log.error("URL: {} {}", request.getMethod(), request.getRequestURI());
        log.error("Query String: {}", request.getQueryString() != null ? request.getQueryString() : "NONE");
        log.error("Exception Type: {}", ex.getClass().getName());
        log.error("Exception Message: {}", ex.getMessage());
        log.error("Full Trace: ", ex);
        log.error("--- [JULES CRASH REPORT END] ---");

        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal error occurred.");
    }

    /**
     * Handle 403 Forbidden - Permission Denied
     * English Comment: Returns the standard ApiErrorResponse when a ForbiddenException is thrown.
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbiddenException(ForbiddenException ex) {
        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleStaticResourceNotFound(NoResourceFoundException ex) {
        // 仅仅记录一条轻微警告，不再触发恐怖的 [JULES CRASH REPORT]
        log.warn(">>> [JULES WARN] Static resource not found: {}", ex.getResourcePath());

        return buildErrorResponse(HttpStatus.NOT_FOUND, "Static resource not found");
    }

    // --- Private Helper Method ---
    private ResponseEntity<ApiErrorResponse> buildErrorResponse(HttpStatus status, String message) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, status);
    }


}