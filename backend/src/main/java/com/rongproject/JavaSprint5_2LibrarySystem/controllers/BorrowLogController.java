package com.rongproject.JavaSprint5_2LibrarySystem.controllers;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.LogResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.security.CustomUserDetails;
import com.rongproject.JavaSprint5_2LibrarySystem.services.BorrowLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/borrow-logs")
@RequiredArgsConstructor
@Tag(name = "Borrowing Log Management", description = "Endpoints for book transactions and log history")
public class BorrowLogController {

    private final BorrowLogService borrowLogService;

    @PostMapping("/borrow/{bookId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Borrow a book")
    public ResponseEntity<LogResponse> borrowBook(
            @PathVariable Long bookId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // English Comment: Pass the authenticated userId and the bookId to the service
        LogResponse response = borrowLogService.borrowBook(userDetails.getId(), bookId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/return/{bookId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Return a book")
    public ResponseEntity<LogResponse> returnBook(
            @PathVariable Long bookId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // English Comment: Use the current user's ID to return the specific book
        LogResponse response = borrowLogService.returnBook(userDetails.getId(), bookId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all borrowing logs", description = "Admin only access to all records")
    public ResponseEntity<List<LogResponse>> getAllLogs() {
        return ResponseEntity.ok(borrowLogService.getAllLogs());
    }

    @GetMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.name")
    @Operation(summary = "Get logs by username", description = "Admin can see any user's logs; Users can only see their own")
    public ResponseEntity<List<LogResponse>> getLogsByUserName(@PathVariable String username) {
        return ResponseEntity.ok(borrowLogService.getLogsByUserName(username));
    }

    @PatchMapping("/{recordId}/date-demo")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update borrow date for demonstration purposes")
    public ResponseEntity<LogResponse> updateBorrowDateForDemo(
            @PathVariable String recordId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newBorrowDate) {

        // English Comment: Utility endpoint for testing overdue logic
        return ResponseEntity.ok(borrowLogService.updateBorrowDateForDemo(recordId, newBorrowDate));
    }
}
