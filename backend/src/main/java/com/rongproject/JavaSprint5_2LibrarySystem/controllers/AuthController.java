package com.rongproject.JavaSprint5_2LibrarySystem.controllers;


import com.rongproject.JavaSprint5_2LibrarySystem.DTO.AuthResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.LoginRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.RegisterRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        // Handle user registration
        String result = authService.register(request);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Login to get JWT token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Handle login and return JWT
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
