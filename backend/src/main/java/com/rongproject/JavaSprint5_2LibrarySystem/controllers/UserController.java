package com.rongproject.JavaSprint5_2LibrarySystem.controllers;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.*;
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
import org.springframework.security.core.context.SecurityContextHolder;
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

    // Java - UserController.java
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getSelf(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // 诊断 1: 请求是否到达了 Controller
        System.out.println(">>> [DIAGNOSTIC] Request reached /api/users/me");

        // 诊断 2: Security 上下文是否正确提取了用户
        if (userDetails == null) {
            System.err.println(">>> [DIAGNOSTIC] Error: userDetails is NULL. Authentication failed or Principal type mismatch.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        System.out.println(">>> [DIAGNOSTIC] Authenticated User ID: " + userDetails.getId());
        System.out.println(">>> [DIAGNOSTIC] Authenticated Username: " + userDetails.getUsername());

        try {
            // 诊断 3: 调用 Service 层前
            UserResponse response = userService.getUserById(userDetails.getId());
            System.out.println(">>> [DIAGNOSTIC] Service call successful. Data: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 诊断 4: 捕获 Service 层具体崩溃原因
            System.err.println(">>> [DIAGNOSTIC] Service layer CRASHED!");
            e.printStackTrace(); // 这会在控制台打印完整的错误堆栈
            throw e;
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile() {
        // Get username from Spring Security context
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        UserProfileDTO profile = userService.getProfileByUsername(currentUsername);
        return ResponseEntity.ok(profile);
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