package com.rongproject.JavaSprint5_2LibrarySystem.controller;

import com.rongproject.JavaSprint5_2LibrarySystem.services.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.AuthResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.LoginRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserRegisterRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.controllers.AuthController;
import com.rongproject.JavaSprint5_2LibrarySystem.security.JwtUtils;
import com.rongproject.JavaSprint5_2LibrarySystem.services.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // English Comment: Correct import for Spring Boot 3.4+
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // English Comment: Skip Security filters to avoid 401/403
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // English Comment: @MockBean is deprecated, use @MockitoBean in Spring Boot 3.4+
    @MockitoBean
    private AuthService authService;

    // --- 关键修复：Mock 掉 Security 过滤器链依赖的工具类 ---

    @MockitoBean
    private JwtUtils jwtUtils; // English Comment: Mocking the dependency required by JwtAuthenticationFilter

    // 如果你的 SecurityConfig 还需要 UserDetailsService，也一并 Mock
    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("POST /api/auth/register - Success")
    void register_Success() throws Exception {
        // English Comment: Ensure the record matches your constructor: (username, password, email, avatarUrl)
        UserRegisterRequest request = new UserRegisterRequest(
                "library_user_01",
                "StrongPassword123!",
                "user@example.com",
                "https://example.com/avatar.png"
        );

        when(authService.register(any(UserRegisterRequest.class))).thenReturn("User registered successfully!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully!"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Validation Failure (Password too short)")
    void register_ValidationFail() throws Exception {
        // English Comment: Password is 5 chars, but @Size(min = 6) is required
        UserRegisterRequest invalidRequest = new UserRegisterRequest(
                "user",
                "12345",
                "invalid-email",
                null
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - Success")
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "password");
        AuthResponse mockResponse = new AuthResponse("mock-jwt-token");

        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }
}
