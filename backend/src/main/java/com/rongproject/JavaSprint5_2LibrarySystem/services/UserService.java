package com.rongproject.JavaSprint5_2LibrarySystem.services;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserProfileDTO;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.User;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.LogStatus;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.UserRole;
import com.rongproject.JavaSprint5_2LibrarySystem.exceptions.ResourceNotFoundException;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.BorrowLogRepository;
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
    private final BorrowLogRepository borrowLogRepository;

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

    public UserResponse updateUser(Long targetUserId, User updatedData, String operatorRole) {
        User userToUpdate = userRepository.findByIdOrThrow(targetUserId);

        // 1. Root Admin Protection
        if ("admin".equals(userToUpdate.getUsername())) {
            if (updatedData.isEnabled() == false) {
                throw new RuntimeException("The root admin account cannot be disabled!");
            }
        }

        // 2. Email & Username Uniqueness Check
        // English Comment: Prevent changing to an email that is already taken by another user
        if (updatedData.getEmail() != null && !updatedData.getEmail().equals(userToUpdate.getEmail())) {
            if (userRepository.existsByEmail(updatedData.getEmail())) {
                throw new IllegalStateException("Email is already in use: " + updatedData.getEmail());
            }
        }

        // English Comment: Prevent changing to a username that is already taken (if your logic allows username change)
        if (updatedData.getUsername() != null && !updatedData.getUsername().equals(userToUpdate.getUsername())) {
            if (userRepository.existsByUsername(updatedData.getUsername())) {
                throw new IllegalStateException("Username is already taken: " + updatedData.getUsername());
            }
        }

        // 3. Logic for Admin Operator
        if ("ADMIN".equals(operatorRole)) {
            userToUpdate.setEnabled(updatedData.isEnabled());
            userToUpdate.setManualLock(!updatedData.isEnabled());

            // English Comment: Admin can also update username/email if necessary
            userToUpdate.setUsername(updatedData.getUsername());
            userToUpdate.setEmail(updatedData.getEmail());
        }
        // 4. Logic for Regular User Operator
        else {
            userToUpdate.setEmail(updatedData.getEmail());
            userToUpdate.setAvatarUrl(updatedData.getAvatarUrl());

            if (updatedData.getPassword() != null && !updatedData.getPassword().isEmpty()) {
                userToUpdate.setPassword(passwordEncoder.encode(updatedData.getPassword()));
            }
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

        // 1. Protection logic: Root admin account cannot be deleted
        // English Comment: Prevent deletion of the system's primary administrator
        if ("admin".equals(userToDelete.getUsername())) {
            throw new RuntimeException("The root admin account cannot be deleted!");
        }

        // 2. Borrowing status check: Query MongoDB to see if user has unreturned books
        // English Comment: Check if the user has any active borrowing records in MongoDB
        boolean hasActiveLoans = borrowLogRepository.existsByUserIdAndStatus(id, LogStatus.BORROWED);

        if (hasActiveLoans) {
            throw new RuntimeException("Cannot delete user: This user still has unreturned books.");
        }

        // 3. If all checks pass, proceed with deletion in MySQL
        userRepository.deleteById(id);
    }

    public UserProfileDTO getProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    /* Convert Entity to Record DTO using the canonical constructor.
       Mapping all required fields from the User entity.
    */
        return new UserProfileDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getUserRole(),   // Ensure this field exists in your User entity
                user.getAvatarUrl()   // Ensure this field exists in your User entity
        );
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getUserRole(),
                user.isEnabled(),
                user.isManualLock(),
                user.getAvatarUrl());
    }
}
