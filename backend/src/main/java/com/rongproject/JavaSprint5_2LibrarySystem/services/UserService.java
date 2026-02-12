package com.rongproject.JavaSprint5_2LibrarySystem.services;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.AdminRegisterRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserProfileDTO;
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

    public UserResponse updateUser(Long targetUserId, User updatedData, String operatorRole) {
        // 1. 获取现有用户信息
        System.out.println("DEBUG: Current User is " + SecurityUtils.getCurrentUsername());
        User userToUpdate = userRepository.findByIdOrThrow(targetUserId);
        String currentUsername = SecurityUtils.getCurrentUsername(); // 假设你的工具类

        // 2. Root Admin 核心保护
        if ("admin".equals(userToUpdate.getUsername())) {
            // 只有 admin 自己能改自己
            if (!"admin".equals(currentUsername)) {
                throw new ForbiddenException("Root admin can only be modified by itself");
            }
            // 即使是 admin 自己，也不允许把自己的名字改掉
            if (updatedData.getUsername() != null && !"admin".equals(updatedData.getUsername())) {
                throw new ForbiddenException("Root admin username is permanent");
            }
        }

        // 3. 禁止非法占位
        if (updatedData.getUsername() != null && "admin".equals(updatedData.getUsername())
                && !"admin".equals(userToUpdate.getUsername())) {
            throw new ForbiddenException("Username 'admin' is reserved");
        }

        // 4. 唯一性检查 (仅在字段发生变化且不为空时检查)
        if (updatedData.getEmail() != null && !updatedData.getEmail().equals(userToUpdate.getEmail())) {
            if (userRepository.existsByEmail(updatedData.getEmail())) {
                throw new AlreadyExistsException("Email is already in use: " + updatedData.getEmail());
            }
        }
        if (updatedData.getUsername() != null && !updatedData.getUsername().equals(userToUpdate.getUsername())) {
            if (userRepository.existsByUsername(updatedData.getUsername())) {
                throw new AlreadyExistsException("Username is already taken: " + updatedData.getUsername());
            }
        }

        // 5. 分权执行修改逻辑
        if ("ADMIN".equals(operatorRole)) {
            // --- 管理员特权分支 ---

            // 修复布尔陷阱：只有状态不一致时才切换，避免默认值 false 误伤
            if (updatedData.isEnabled() != userToUpdate.isEnabled()) {
                userToUpdate.setEnabled(updatedData.isEnabled());
                userToUpdate.setManualLock(!updatedData.isEnabled());
            }

            // 修复 Null 覆盖风险：只有传入非空值才修改
            if (updatedData.getUsername() != null) {
                userToUpdate.setUsername(updatedData.getUsername());
            }
            if (updatedData.getEmail() != null) {
                userToUpdate.setEmail(updatedData.getEmail());
            }
            if (updatedData.getUserRole() != null) {
                userToUpdate.setUserRole(updatedData.getUserRole());
            }
        }
        else {
            // --- 普通用户自我修改分支 ---
            if (updatedData.getEmail() != null) {
                userToUpdate.setEmail(updatedData.getEmail());
            }
            if (updatedData.getAvatarUrl() != null) {
                userToUpdate.setAvatarUrl(updatedData.getAvatarUrl());
            }
            if (updatedData.getPassword() != null && !updatedData.getPassword().isEmpty()) {
                userToUpdate.setPassword(passwordEncoder.encode(updatedData.getPassword()));
            }
            // 普通用户路径下，enabled/username/userRole 保持原样，不予处理
        }

        // 6. 持久化并返回
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
