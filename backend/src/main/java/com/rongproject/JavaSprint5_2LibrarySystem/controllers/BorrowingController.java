package com.rongproject.JavaSprint5_2LibrarySystem.controllers;

import com.rongproject.JavaSprint5_2LibrarySystem.security.CustomUserDetails;
import com.rongproject.JavaSprint5_2LibrarySystem.services.CustomUserDetailsService;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.LogResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserStatusResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.services.BorrowingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrowings")
@RequiredArgsConstructor
@Tag(name = "Borrowing Management", description = "Endpoints for borrowing and returning books")
public class BorrowingController {

    private final BorrowingService borrowingService;

    @PostMapping("/borrow/{bookId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Borrow a book")
    public ResponseEntity<LogResponse> borrowBook(
            @PathVariable Long bookId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // English Comment: Extract the numeric userId from the security context
        Long userId = userDetails.getId();
        LogResponse response = borrowingService.borrowBook(userId, bookId);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/return/{bookId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Return a book")
    public ResponseEntity<LogResponse> returnBook(
            @PathVariable Long bookId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // English Comment: Service requires both userId and bookId to locate the log and update stock
        Long userId = userDetails.getId();
        LogResponse response = borrowingService.returnBook(userId, bookId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get user borrowing eligibility",
            description = "Check if the user can borrow new books based on account status, overdue logs, and current loan count."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}/status")
    public ResponseEntity<UserStatusResponse> getStatus(
            @Parameter(description = "The ID of the user to check", example = "5")
            @PathVariable Long userId) {
        return ResponseEntity.ok(borrowingService.getUserBorrowingStatus(userId));
    }


}
