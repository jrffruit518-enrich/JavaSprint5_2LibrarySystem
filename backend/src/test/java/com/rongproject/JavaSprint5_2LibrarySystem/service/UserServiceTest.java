package com.rongproject.JavaSprint5_2LibrarySystem.service;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.User;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.LogStatus;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.UserRole;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.BorrowLogRepository;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.UserRepository;
import com.rongproject.JavaSprint5_2LibrarySystem.services.UserService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private BorrowLogRepository borrowLogRepository;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("john_doe");
        mockUser.setEmail("john@example.com");
        mockUser.setPassword("rawPassword");
        mockUser.setUserRole(UserRole.ROLE_USER);
        mockUser.setEnabled(true);
        mockUser.setManualLock(false);
    }

    // --- 1. createUser Tests ---

    @Test
    @DisplayName("createUser - Success")
    void createUser_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UserResponse response = userService.createUser(mockUser);

        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser - Failure (Username Exists)")
    void createUser_Fail_UsernameExists() {
        when(userRepository.existsByUsername("john_doe")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.createUser(mockUser));
        verify(userRepository, never()).save(any());
    }

    // --- 2. updateUser Tests (Crucial Logic) ---

    @Test
    @DisplayName("updateUser - Admin sets status and triggers ManualLock")
    void updateUser_AdminLockingUser() {
        // English Comment: Mock existing user in DB
        when(userRepository.findByIdOrThrow(1L)).thenReturn(mockUser);

        // English Comment: Admin wants to disable the user
        User updateData = new User();
        updateData.setEnabled(false);

        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UserResponse response = userService.updateUser(1L, updateData, "ADMIN");

        // English Comment: Verify that disabling as ADMIN sets manualLock to true
        assertFalse(mockUser.isEnabled());
        assertTrue(mockUser.isManualLock());
        verify(userRepository).save(mockUser);
    }

    @Test
    @DisplayName("updateUser - Fail when Email is taken by another user")
    void updateUser_Fail_EmailTaken() {
        when(userRepository.findByIdOrThrow(1L)).thenReturn(mockUser);

        User updateData = new User();
        updateData.setEmail("other@example.com");

        // English Comment: Mock that the new email is already in the system
        when(userRepository.existsByEmail("other@example.com")).thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                userService.updateUser(1L, updateData, "USER")
        );
    }

    @Test
    @DisplayName("updateUser - Root Admin cannot be disabled")
    void updateUser_RootAdminProtection() {
        mockUser.setUsername("admin");
        when(userRepository.findByIdOrThrow(1L)).thenReturn(mockUser);

        User updateData = new User();
        updateData.setEnabled(false);

        Exception exception = assertThrows(RuntimeException.class, () ->
                userService.updateUser(1L, updateData, "ADMIN")
        );
        assertTrue(exception.getMessage().contains("root admin"));
    }

    // --- 3. deleteUser Tests ---

    @Test
    @DisplayName("deleteUser - Success when no active loans")
    void deleteUser_Success() {
        when(userRepository.findByIdOrThrow(1L)).thenReturn(mockUser);
        // English Comment: Mock MongoDB checking for active books
        when(borrowLogRepository.existsByUserIdAndStatus(1L, LogStatus.BORROWED)).thenReturn(false);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteUser - Fail when user has unreturned books")
    void deleteUser_Fail_ActiveLoans() {
        when(userRepository.findByIdOrThrow(1L)).thenReturn(mockUser);
        // English Comment: User still has books in MongoDB
        when(borrowLogRepository.existsByUserIdAndStatus(1L, LogStatus.BORROWED)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.deleteUser(1L)
        );
        assertTrue(exception.getMessage().contains("unreturned books"));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("deleteUser - Root admin cannot be deleted")
    void deleteUser_RootAdminProtection() {
        mockUser.setUsername("admin");
        when(userRepository.findByIdOrThrow(1L)).thenReturn(mockUser);

        assertThrows(RuntimeException.class, () -> userService.deleteUser(1L));
        verify(userRepository, never()).deleteById(anyLong());
    }
}
