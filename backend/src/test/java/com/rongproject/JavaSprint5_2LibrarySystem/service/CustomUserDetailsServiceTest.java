package com.rongproject.JavaSprint5_2LibrarySystem.service;

import com.rongproject.JavaSprint5_2LibrarySystem.entities.User;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.UserRole;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.UserRepository;
import com.rongproject.JavaSprint5_2LibrarySystem.security.CustomUserDetails;
import com.rongproject.JavaSprint5_2LibrarySystem.services.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("loadUserByUsername - Success Scenario with Custom ID")
    void loadUserByUsername_Success() {
        // English Comment: Arrange - Mock our custom User entity with an ID
        User mockUser = new User();
        mockUser.setId(100L); // Set a specific ID to test mapping
        mockUser.setUsername("testadmin");
        mockUser.setPassword("hashed_pwd");
        mockUser.setUserRole(UserRole.ROLE_ADMIN);

        when(userRepository.findByUsername("testadmin")).thenReturn(Optional.of(mockUser));

        // English Comment: Act - Call the service
        UserDetails result = customUserDetailsService.loadUserByUsername("testadmin");

        // English Comment: Assert - Verify it is our custom class and has the ID
        assertNotNull(result);
        assertTrue(result instanceof CustomUserDetails, "Result should be an instance of CustomUserDetails");

        CustomUserDetails customResult = (CustomUserDetails) result;
        assertEquals(100L, customResult.getId()); // English Comment: Crucial check for the new ID field

        assertEquals("testadmin", customResult.getUsername());
        assertEquals("hashed_pwd", customResult.getPassword());

        // English Comment: Verify the authority (role) exists
        assertTrue(customResult.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("loadUserByUsername - User Not Found")
    void loadUserByUsername_Fail_NotFound() {
        // English Comment: Arrange
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        // English Comment: Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("ghost");
        });
    }
}
