package com.rongproject.JavaSprint5_2LibrarySystem.service;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.AuthResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.LoginRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserRegisterRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.User;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.UserRepository;
import com.rongproject.JavaSprint5_2LibrarySystem.security.JwtUtils;
import com.rongproject.JavaSprint5_2LibrarySystem.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    private User mockUser;
    private UserRegisterRequest regRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setPassword("hashed_password");
        mockUser.setEmail("test@test.com");

        regRequest = new UserRegisterRequest("testuser", "plain_password","test@test.com",null );
        loginRequest = new LoginRequest("testuser", "plain_password");
    }

    // --- 1. Registration Tests ---

    @Test
    @DisplayName("Register - Success Scenario")
    void register_Success() {
        // English Comment: Arrange - No duplicates exist
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");

        // English Comment: Act
        String result = authService.register(regRequest);

        // English Comment: Assert
        assertEquals("User registered successfully!", result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Register - Fail when Username exists")
    void register_Fail_DuplicateUsername() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(regRequest));
        verify(userRepository, never()).save(any());
    }

    // --- 2. Login Tests ---

    @Test
    @DisplayName("Login - Success Scenario")
    void login_Success() {
        // English Comment: Arrange - User exists and password matches
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("plain_password", "hashed_password")).thenReturn(true);
        when(jwtUtils.generateToken("testuser")).thenReturn("mocked_jwt_token");

        // English Comment: Act
        AuthResponse response = authService.login(loginRequest);

        // English Comment: Assert
        assertNotNull(response.token());
        assertEquals("mocked_jwt_token", response.token());
    }

    @Test
    @DisplayName("Login - Fail with Wrong Password")
    void login_Fail_WrongPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        // English Comment: Simulate BCrypt mismatch
        when(passwordEncoder.matches("wrong_password", "hashed_password")).thenReturn(false);

        LoginRequest wrongRequest = new LoginRequest("testuser", "wrong_password");

        assertThrows(RuntimeException.class, () -> authService.login(wrongRequest));
    }

    @Test
    @DisplayName("Login - Fail when User not found")
    void login_Fail_UserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        LoginRequest unknownUserRequest = new LoginRequest("unknown", "password");

        assertThrows(RuntimeException.class, () -> authService.login(unknownUserRequest));
    }
}
