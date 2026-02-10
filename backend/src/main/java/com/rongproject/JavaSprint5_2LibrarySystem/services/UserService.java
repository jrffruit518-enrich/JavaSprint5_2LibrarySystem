package com.rongproject.JavaSprint5_2LibrarySystem.services;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.User;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.UserRole;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse createUser(User user) {
        // 1. 基础校验：验证用户名是否存在（无论什么角色都需要验证 Name）
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }

        // 2. 针对普通用户 (USER) 的额外校验
        // English Comment: Additional unique check for email only if the role is USER
        if (user.getUserRole() == UserRole.ROLE_USER) {
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new RuntimeException("Email already exists: " + user.getEmail());
            }
        }
        // English Comment: If role is ADMIN, only username check is needed (already done above)
        // Passwords can be the same as per requirements, so no check needed for that.

        // 3. 加密并保存
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }
    
    public UserResponse getUserById(Long id) {
        User user = userRepository.findByIdOrThrow(id);
        return mapToResponse(user);
    }

    public UserResponse updateUser(Long id, User updatedUser) {
        User userToUpdate = userRepository.findByIdOrThrow(id);

        // Protection: Prevent changing the root admin's role to anything else
        if ("admin".equals(userToUpdate.getUsername()) && !userToUpdate.getUserRole().equals(updatedUser.getUserRole())) {
            throw new RuntimeException("The root admin's role cannot be downgraded!");
        }

        userToUpdate.setUsername(updatedUser.getUsername());
        userToUpdate.setEmail(updatedUser.getEmail());
        userToUpdate.setUserRole(updatedUser.getUserRole());
        userToUpdate.setEnabled(updatedUser.isEnabled());
        userToUpdate.setAvatarUrl(updatedUser.getAvatarUrl());

        // English Comment: If password change is allowed in this flow, encode it
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            userToUpdate.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        User savedUser = userRepository.save(userToUpdate);
        return mapToResponse(savedUser);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void deleteUser(Long id) {
        User userToDelete = userRepository.findByIdOrThrow(id);
        // Protection logic: Root admin account cannot be deleted
        if ("admin".equals(userToDelete.getUsername())) {
            throw new RuntimeException("The root admin account cannot be deleted!");
        }

        userRepository.deleteById(id);
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getUserRole(),
                user.isEnabled(),
                user.getAvatarUrl());
    }
}
