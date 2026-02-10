package com.rongproject.JavaSprint5_2LibrarySystem.controllers;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.LogResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserProfileRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserRegisterRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.User;
import com.rongproject.JavaSprint5_2LibrarySystem.security.CustomUserDetails;
import com.rongproject.JavaSprint5_2LibrarySystem.security.SecurityUtils;
import com.rongproject.JavaSprint5_2LibrarySystem.services.BorrowingService;
import com.rongproject.JavaSprint5_2LibrarySystem.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for user profile, administration, and demo tools")
public class UserController {

    private final UserService userService;
    private final BorrowingService borrowingService;

    // --- 1. 基础管理 (Admin Only) ---

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Create a new user")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRegisterRequest request) {

        // English Comment: Convert DTO to Entity within the service layer logic
        // We reuse UserRegisterRequest to ensure validation is applied
        UserResponse response = userService.createUser(request.toEntity());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: List all users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Delete user", description = "Checks MongoDB for active loans before deletion.")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // --- 2. 状态切换 (Admin Only) ---

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Toggle user active status")
    public ResponseEntity<UserResponse> toggleStatus(@PathVariable Long id, @RequestBody boolean enabled) {
        // English Comment: Pass the 'ADMIN' role to unlock status change branch in service
        User data = new User();
        data.setEnabled(enabled);
        return ResponseEntity.ok(userService.updateUser(id, data, "ADMIN"));
    }

    // --- 3. 用户自查与信息更新 ---

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Get user by ID", description = "Admins can get any; users can only get their own.")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/me")
    @Operation(summary = "User: Update own profile")
    public ResponseEntity<UserResponse> updateSelf(
            @RequestBody @Valid UserProfileRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // English Comment: Get ID directly from the principal object to avoid static utility issues in tests
        Long currentUserId = userDetails.getId();
        return ResponseEntity.ok(userService.updateUser(currentUserId, request.toEntity(), "USER"));
    }

    // --- 4. 演示辅助工具 (Demo Tools) ---

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Demo: Force update borrow date", description = "Admin tool to simulate overdue by shifting dates back.")
    @PatchMapping("/borrow-logs/{logId}/date")
    public ResponseEntity<LogResponse> forceUpdateBorrowDate(
            @PathVariable String logId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newDate) {

        // English Comment: Delegate the logic to borrowingService
        return ResponseEntity.ok(borrowingService.updateBorrowDateForDemo(logId, newDate));
    }
}