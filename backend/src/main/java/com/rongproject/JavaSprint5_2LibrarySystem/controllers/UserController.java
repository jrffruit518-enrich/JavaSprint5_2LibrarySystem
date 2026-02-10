package com.rongproject.JavaSprint5_2LibrarySystem.controllers;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.LogResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserProfileRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.User;
import com.rongproject.JavaSprint5_2LibrarySystem.security.SecurityUtils;
import com.rongproject.JavaSprint5_2LibrarySystem.services.BorrowingService;
import com.rongproject.JavaSprint5_2LibrarySystem.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users") // English Comment: Base path for all user-related endpoints
public class UserController {

    private final UserService userService;
    private final BorrowingService borrowingService;

    // 访问路径将是: GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }


    // English Comment: Update current logged-in user's profile
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateSelf(@RequestBody UserProfileRequest request) {
        // Logic: Get current ID from Security Context, then call service
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(userService.updateUser(currentUserId, request.toEntity(), "USER"));
    }

    // English Comment: Admin toggles user active status
    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponse> toggleStatus(@PathVariable Long id, @RequestBody boolean enabled) {
        // Logic: Pass the 'ADMIN' role to unlock the status change branch in service
        User data = new User();
        data.setEnabled(enabled);
        return ResponseEntity.ok(userService.updateUser(id, data, "ADMIN"));
    }

    // English Comment: Accelerator for Demo. Admin can force change a borrow log's date to simulate overdue.
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Demo: Force update borrow date", description = "Admin tool to simulate overdue by shifting dates back.")
    @PatchMapping("/borrow-logs/{logId}/date")
    public ResponseEntity<LogResponse> forceUpdateBorrowDate(
            @PathVariable String logId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newDate) {

        // English Comment: Delegate the logic to borrowingService
        LogResponse response = borrowingService.updateBorrowDateForDemo(logId, newDate);
        return ResponseEntity.ok(response);
    }
}
