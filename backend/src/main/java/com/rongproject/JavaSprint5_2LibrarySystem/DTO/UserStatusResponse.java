package com.rongproject.JavaSprint5_2LibrarySystem.DTO;

public record UserStatusResponse(boolean canBorrow,   // The master switch for Frontend button
                                 boolean isEnabled,   // Admin status
                                 boolean hasOverdue,  // Overdue status
                                 long borrowCount) // Current count)
 {}



