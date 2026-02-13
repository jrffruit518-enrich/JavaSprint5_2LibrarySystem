package com.rongproject.JavaSprint5_2LibrarySystem.services;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.AdminRegisterRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserProfileDTO;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserProfileRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.User;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.LogStatus;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.UserRole;
import com.rongproject.JavaSprint5_2LibrarySystem.exceptions.AlreadyExistsException;
import com.rongproject.JavaSprint5_2LibrarySystem.exceptions.ForbiddenException;
import com.rongproject.JavaSprint5_2LibrarySystem.exceptions.ResourceNotFoundException;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.BorrowLogRepository;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.UserRepository;
import com.rongproject.JavaSprint5_2LibrarySystem.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.security.SecurityUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BorrowLogRepository borrowLogRepository;

    public UserResponse createUser(User user) {
        // 1. Placeholder email check (Jules: Security guardrail for regular users)
        // English Comment: Prevent regular users from using the system-reserved placeholder email.
        if ("pending@library.com".equalsIgnoreCase(user.getEmail())) {
            throw new AlreadyExistsException("Registration failed: Placeholder email 'pending@library.com' is reserved for system use.");
        }

        // 2. Global uniqueness check for Username
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new AlreadyExistsException("Username already exists");
        }

        // 3. Default role assignment if null
        if (user.getUserRole() == null) {
            user.setUserRole(UserRole.ROLE_USER);
        }

        // 4. Global uniqueness check for Email
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new AlreadyExistsException("Email already exists");
        }

        // 5. Password encoding and Save
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return mapToResponse(userRepository.save(user));
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findByIdOrThrow(id);
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long targetUserId, UserProfileRequest request, String operatorRole) {
        // 1. 获取现有用户信息 (existingUser)
        User userToUpdate = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String currentUsername = SecurityUtils.getCurrentUsername();

        // 2. Root Admin 核心保护 (保持逻辑不变)
        if ("admin".equals(userToUpdate.getUsername())) {
            if (!"admin".equals(currentUsername)) {
                throw new ForbiddenException("Root admin can only be modified by itself");
            }
        }

        // 3. 唯一性检查 (仅针对 email，因为 UserProfileRequest 通常不改 username)
        if (request.email() != null && !request.email().equals(userToUpdate.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new AlreadyExistsException("Email is already in use: " + request.email());
            }
        }

        // 4. 执行修改逻辑
        if ("ADMIN".equals(operatorRole)) {
            // --- 管理员特权分支 (未来如果 Admin 专用 DTO 扩展，在此处理) ---
            // 目前 UserProfileRequest 仅包含个人资料字段
            request.updateExistingUser(userToUpdate);
        } else {
            // --- 普通用户自我修改分支 ---
            // 映射基本字段：email, avatarUrl
            request.updateExistingUser(userToUpdate);

            // 独立处理密码加密
            if (request.password() != null && !request.password().isEmpty()) {
                userToUpdate.setPassword(passwordEncoder.encode(request.password()));
            }
        }

        // 5. 持久化并返回
        User savedUser = userRepository.save(userToUpdate);
        return mapToResponse(savedUser);
    }

    public List<UserResponse> getAllUsers() {
        System.out.println(">>> [JULES DB CHECK] Fetching all users from database...");
        List<User> users = userRepository.findAll();
        System.out.println(">>> [JULES DB CHECK] Found users count: " + users.size());

        // 检查第一个用户是否有 Role 缺失
        if (!users.isEmpty()) {
            System.out.println(">>> [JULES DB CHECK] Sample User Role: " + users.get(0).getUserRole());
        }

        return users.stream().map(this::mapToResponse).toList();
    }

    public void deleteUser(Long id) {
        // 1. 获取用户
        User targetUser = userRepository.findByIdOrThrow(id);

        // 2. 检查是否有未归还图书 (MongoDB)
        // English Comment: Prevent deletion if user still has books
        if (borrowLogRepository.existsByUserIdAndStatus(id, LogStatus.BORROWED)) {
            throw new RuntimeException("Cannot delete user with unreturned books!");
        }

        // 3. 保护逻辑 1: Root Admin
        if ("admin".equalsIgnoreCase(targetUser.getUsername())) {
            throw new RuntimeException("The root administrator account cannot be deleted!");
        }

        // 4. 保护逻辑 2: Self-deletion
        // 注意：这里可能会因为 SecurityContext 没 Mock 报 NPE，所以测试里一定要 Mock
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName().equals(targetUser.getUsername())) {
            throw new RuntimeException("You cannot delete your own account while logged in!");
        }

        userRepository.delete(targetUser);
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


    public UserResponse createAdmin(AdminRegisterRequest request) {
        // 1. Validation: Use the newly created AlreadyExistsException
        if (userRepository.existsByUsername(request.username())) {
            throw new AlreadyExistsException("Admin username already taken: " + request.username());
        }

        // 2. Build Admin Entity using the internal email generator
        User admin = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .email(generateInternalEmail(request.username()))
                .userRole(UserRole.ROLE_ADMIN)
                .enabled(true) // Ensure the admin is active by default
                .manualLock(false)
                .build();

        // 3. Save and map to response
        User savedAdmin = userRepository.save(admin);
        return mapToResponse(savedAdmin);
    }

    private String generateInternalEmail(String username) {
        // English Comment: Generates a unique internal identifier to satisfy DB constraints
        return String.format("%s@internal.system", username.toLowerCase());
    }

    @Transactional
    public UserResponse toggleUserStatus(Long id, boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Root Admin 保护：禁止禁用 admin 账号
        if ("admin".equals(user.getUsername())) {
            throw new ForbiddenException("Root admin status cannot be changed");
        }

        // 更新状态：同步修改 enabled 和 manualLock
        user.setEnabled(enabled);
        user.setManualLock(!enabled);

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
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
