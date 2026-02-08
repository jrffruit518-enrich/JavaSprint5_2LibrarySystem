package com.rongproject.JavaSprint5_2LibrarySystem.DTO;

import com.rongproject.JavaSprint5_2LibrarySystem.enums.UserRole;

public record UserResponse(
        Long id,
        String username,
        String email,
        UserRole userRole,
        boolean enabled,
        String avatarUrl
) {}
