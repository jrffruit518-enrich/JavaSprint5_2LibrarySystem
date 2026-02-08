package com.rongproject.JavaSprint5_2LibrarySystem.DTO;

/**
 * DTO for updating user profile.
 * No validation constraints added as per request.
 */
public record UserUpdateRequest(
        String email,
        String avatarUrl
) {}
