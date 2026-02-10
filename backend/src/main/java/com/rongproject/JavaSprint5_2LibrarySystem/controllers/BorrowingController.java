package com.rongproject.JavaSprint5_2LibrarySystem.controllers;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.LogResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.BorrowOperationRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserStatusResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.services.BorrowingService;

// Swagger 注解：仅用于描述文档
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

// Spring 注解：用于实际业务处理（核心！）
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // 包含了 @RestController, @PostMapping 等
import org.springframework.web.bind.annotation.RequestBody; // 必须用这个！

@RestController
@RequestMapping("/borrow")
@RequiredArgsConstructor
public class BorrowingController {

    private final BorrowingService borrowingService;

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

    @Operation(
            summary = "Return a book",
            description = "Returns a borrowed book, updates stock, and checks if user status can be restored."
    )
    @PostMapping("/return")
    public ResponseEntity<LogResponse> returnBook(@RequestBody BorrowOperationRequest request) {
        return ResponseEntity.ok(borrowingService.returnBook(request.userId(), request.bookId()));
    }


}
