package com.rongproject.JavaSprint5_2LibrarySystem.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

public class SecurityUtils {

    // English Comment: Get the ID of the currently authenticated user
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        // English Comment: Throw exception if no user is logged in
        throw new RuntimeException("No authenticated user found!");
    }
}
