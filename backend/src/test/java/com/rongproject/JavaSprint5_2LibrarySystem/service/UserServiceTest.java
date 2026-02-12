package com.rongproject.JavaSprint5_2LibrarySystem.service;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.AdminRegisterRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserProfileDTO;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.User;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.LogStatus;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.UserRole;
import com.rongproject.JavaSprint5_2LibrarySystem.exceptions.AlreadyExistsException;
import com.rongproject.JavaSprint5_2LibrarySystem.exceptions.ResourceNotFoundException;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.BorrowLogRepository;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.UserRepository;
import com.rongproject.JavaSprint5_2LibrarySystem.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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

        // Changed from RuntimeException to AlreadyExistsException
        assertThrows(AlreadyExistsException.class, () -> userService.createUser(mockUser));
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

        when(userRepository.existsByEmail("other@example.com")).thenReturn(true);

        // Changed from IllegalStateException to AlreadyExistsException
        assertThrows(AlreadyExistsException.class, () ->
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
    void deleteUser_Success() {
        User targetUser = User.builder().id(2L).username("other_user").build();
        when(userRepository.findByIdOrThrow(2L)).thenReturn(targetUser);

        // 关键：模拟当前登录用户为 "admin_user"
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin_user");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        userService.deleteUser(2L);

        verify(userRepository).delete(targetUser);
        SecurityContextHolder.clearContext(); // 养成好习惯
    }

    @Test
    @DisplayName("deleteUser - Fail when user has unreturned books")
    void deleteUser_Fail_ActiveLoans() {
        // 1. Arrange
        when(userRepository.findByIdOrThrow(1L)).thenReturn(mockUser);

        // 使用 lenient() 允许这个 stubbing 不被调用
        lenient().when(borrowLogRepository.existsByUserIdAndStatus(1L, LogStatus.BORROWED)).thenReturn(true);

        // 2. Security Context Mock 也使用 lenient()
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getName()).thenReturn("some_other_admin");

        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        // 3. Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.deleteUser(1L)
        );

        assertTrue(exception.getMessage().contains("unreturned books"));

        SecurityContextHolder.clearContext();
    }


    @Test
    @DisplayName("deleteUser - Root admin cannot be deleted")
    void deleteUser_RootAdminProtection() {
        mockUser.setUsername("admin");
        when(userRepository.findByIdOrThrow(1L)).thenReturn(mockUser);

        assertThrows(RuntimeException.class, () -> userService.deleteUser(1L));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should return UserProfileDTO when user exists")
    void getProfileByUsername_Success() {
        // 1. Arrange: Create a mock User entity
        User mockUser = new User();
        mockUser.setId(5L);
        mockUser.setUsername("john_doe");
        mockUser.setEmail("john@example.com");
        mockUser.setUserRole(UserRole.ROLE_USER);
        mockUser.setAvatarUrl("https://example.com/avatar.png");

        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(mockUser));

        // 2. Act: Call the service method
        UserProfileDTO result = userService.getProfileByUsername("john_doe");

        // 3. Assert: Verify the Record DTO contains correct data
        assertNotNull(result);
        assertEquals(5L, result.id());
        assertEquals("john_doe", result.username());
        assertEquals(UserRole.ROLE_USER, result.userRole());
        verify(userRepository, times(1)).findByUsername("john_doe");
    }

    @Test
    @DisplayName("Should throw exception when user is not found")
    void getProfileByUsername_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getProfileByUsername("unknown");
        });
    }
    @Test
    void createAdmin_Success() {
        // 1. Arrange
        AdminRegisterRequest request = new AdminRegisterRequest("new_admin", "password123");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. Act
        UserResponse response = userService.createAdmin(request);

        // 3. Assert
        assertEquals("new_admin", response.username());
        // Verify our professional internal email logic
        assertEquals("new_admin@internal.system", response.email());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createAdmin_ThrowsException_WhenUsernameExists() {
        // Arrange
        AdminRegisterRequest request = new AdminRegisterRequest("existing_admin", "password");
        when(userRepository.existsByUsername("existing_admin")).thenReturn(true);

        // Act & Assert
        assertThrows(AlreadyExistsException.class, () -> userService.createAdmin(request));
    }

    @Test
    @DisplayName("createUser - Failure (Placeholder Email Blocked)")
    void createUser_Fail_PlaceholderEmail() {
        // 确保 mockUser 已经被实例化，不是 null
        mockUser.setEmail("pending@library.com");

        assertThrows(AlreadyExistsException.class, () -> userService.createUser(mockUser));
    }

    @Test
    @DisplayName("deleteUser - Fail when deleting root admin")
    void deleteUser_Fail_RootAdmin() {
        // 1. Arrange: Target is the protected "admin" account
        User rootAdmin = User.builder().id(1L).username("admin").build();
        when(userRepository.findByIdOrThrow(1L)).thenReturn(rootAdmin);

        // 2. Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.deleteUser(1L)
        );

        // English Comment: Ensure the error message matches the Service implementation exactly
        assertTrue(exception.getMessage().contains("root administrator account cannot be deleted"));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteAdmin_Success() {
        // 1. Arrange: Mock the operator as "admin_boss" and target as "user_to_delete"
        User targetUser = User.builder().id(1L).username("user_to_delete").build();
        when(userRepository.findByIdOrThrow(1L)).thenReturn(targetUser);

        // Mock SecurityContext to return "admin_boss"
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin_boss");
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 2. Act
        userService.deleteUser(1L);

        // 3. Assert
        verify(userRepository).delete(targetUser);
    }

    @Test
    void deleteUser_ThrowsException_WhenDeletingRootAdmin() {
        // Arrange: Target is the protected "admin" account
        User rootAdmin = User.builder().id(1L).username("admin").build();
        when(userRepository.findByIdOrThrow(1L)).thenReturn(rootAdmin);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.deleteUser(1L));
        assertEquals("The root administrator account cannot be deleted!", exception.getMessage());
    }
}
