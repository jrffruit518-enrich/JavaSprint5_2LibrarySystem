package com.rongproject.JavaSprint5_2LibrarySystem.services;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.AuthResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.LoginRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserRegisterRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.User;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.UserRole;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.UserRepository;
import com.rongproject.JavaSprint5_2LibrarySystem.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    /**
     * User Registration logic
     * @param request The registration details from the client
     * @return Success message
     */
    @Transactional
    public String register(UserRegisterRequest request) {
        // 1. Check if username or email already exists
        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("Error: Username is already taken!");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // 2. Create new user and encrypt the password
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        // Use BCrypt to encode the plain text password
        user.setPassword(passwordEncoder.encode(request.password()));

        // 3. Set default role and status
        user.setUserRole(UserRole.ROLE_USER);
        user.setEnabled(true);

        userRepository.save(user);
        return "User registered successfully!";
    }

    /**
     * User Login logic
     * @param request The login credentials
     * @return AuthResponse containing the JWT token
     */
    public AuthResponse login(LoginRequest request) {
        // 1. Find user by username
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("Error: Invalid username or password"));

        // 2. Verify the password (matches plain text with hashed password)
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Error: Invalid username or password");
        }

        // 3. If valid, generate and return the token
        String token = jwtUtils.generateToken(user.getUsername());
        return new AuthResponse(token);
    }
}
