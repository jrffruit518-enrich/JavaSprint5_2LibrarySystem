package com.rongproject.JavaSprint5_2LibrarySystem.controller;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.*;
import com.rongproject.JavaSprint5_2LibrarySystem.controllers.UserController;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.User;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.UserRole;
import com.rongproject.JavaSprint5_2LibrarySystem.exceptions.AlreadyExistsException;
import com.rongproject.JavaSprint5_2LibrarySystem.security.CustomUserDetails;
import com.rongproject.JavaSprint5_2LibrarySystem.security.JwtUtils;
import com.rongproject.JavaSprint5_2LibrarySystem.services.BorrowingService;
import com.rongproject.JavaSprint5_2LibrarySystem.services.CustomUserDetailsService;
import com.rongproject.JavaSprint5_2LibrarySystem.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(com.rongproject.JavaSprint5_2LibrarySystem.security.SecurityConfig.class)
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private BorrowingService borrowingService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private CustomUserDetails adminPrincipal;
    private CustomUserDetails userPrincipal;
    private UserResponse mockUserResponse;

    @BeforeEach
    void setUp() {
        // English Comment: Setup Admin Principal
        User adminEntity = new User();
        adminEntity.setId(1L);
        adminEntity.setUsername("admin");
        adminEntity.setPassword("hash");
        adminEntity.setUserRole(UserRole.ROLE_ADMIN);
        adminPrincipal = new CustomUserDetails(adminEntity);

        // English Comment: Setup Regular User Principal
        User userEntity = new User();
        userEntity.setId(2L);
        userEntity.setUsername("testuser");
        userEntity.setPassword("hash");
        userEntity.setUserRole(UserRole.ROLE_USER);
        userPrincipal = new CustomUserDetails(userEntity);

        mockUserResponse = new UserResponse(2L, "testuser", "test@example.com", UserRole.ROLE_USER, true, false, "url");
    }

    // --- 1. Admin Management Tests ---

    @Test
    @DisplayName("POST /api/users - Admin can create user using DTO")
    void createUser_AdminSuccess() throws Exception {
        // 1. 准备符合校验规则的 DTO 数据
        UserRegisterRequest request = new UserRegisterRequest(
                "new_user",
                "password123",
                "new@example.com",
                "http://avatar.url"
        );

        // 2. Mock Service 的行为 (Service 依然接收 Entity)
        when(userService.createUser(any(User.class))).thenReturn(mockUserResponse);

        // 3. 执行请求，发送 DTO 序列化后的 JSON
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // 必须传 request DTO
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - Admin can delete user")
    void deleteUser_AdminSuccess() throws Exception {
        doNothing().when(userService).deleteUser(2L);

        mockMvc.perform(delete("/api/users/2")
                        .with(csrf())
                        .with(user(adminPrincipal)))
                .andExpect(status().isNoContent());
    }

    // --- 2. Profile & Status Tests ---

    @Test
    @DisplayName("PATCH /api/users/{id}/status - Admin can toggle status")
    void toggleStatus_AdminSuccess() throws Exception {
        when(userService.updateUser(eq(2L), any(User.class), eq("ADMIN"))).thenReturn(mockUserResponse);

        mockMvc.perform(patch("/api/users/2/status")
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("true"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/users/me - User can update own profile")
    void updateSelf_Success() throws Exception {
        // 修正 avatarUrl 为合法的 URL 格式
        UserProfileRequest request = new UserProfileRequest(
                "new@example.com",
                "https://example.com/new_avatar.png",
                "123456"
        );

        when(userService.updateUser(eq(2L), any(User.class), eq("USER"))).thenReturn(mockUserResponse);

        mockMvc.perform(put("/api/users/me")
                        .with(csrf())
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // --- 3. Security Boundary Tests ---

    @Test
    @DisplayName("GET /api/users - Regular user cannot list all users")
    void getAllUsers_UserForbidden() throws Exception {
        mockMvc.perform(get("/api/users")
                        .with(user(userPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/users - Failure: Username/Email already exists")
    void createUser_AdminConflict() throws Exception {
        // 确保 email 格式正确，密码长度符合要求
        UserRegisterRequest request = new UserRegisterRequest(
                "existing",
                "password123",
                "ex@example.com",
                "https://avatar.url"
        );

        when(userService.createUser(any(User.class)))
                .thenThrow(new AlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict()); // 现在应该能拿到 409 了
    }

    @Test
    @DisplayName("GET /api/users/{id} - User cannot see other user's profile")
    void getUserById_OtherUserForbidden() throws Exception {
        // English Comment: userPrincipal has ID 2, trying to access ID 1
        mockMvc.perform(get("/api/users/1")
                        .with(user(userPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "john_doe") // Mocks the authenticated user
    @DisplayName("Should return 200 OK and user profile data")
    void getUserProfile_ShouldReturnProfile() throws Exception {
        // 1. Arrange: Prepare the Record DTO
        UserProfileDTO mockProfile = new UserProfileDTO(
                5L,
                "john_doe",
                "john@example.com",
                UserRole.ROLE_USER,
                "https://example.com/avatar.png"
        );

        when(userService.getProfileByUsername("john_doe")).thenReturn(mockProfile);

        // 2. Act & Assert: Perform GET request and verify JSON content
        mockMvc.perform(get("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.userRole").value("ROLE_USER"));
    }

    @Test
    @DisplayName("POST /api/users/admin - Success")
    void createAdmin_Success() throws Exception {
        // 1. Arrange
        AdminRegisterRequest request = new AdminRegisterRequest("new_admin", "password123");

        // Correctly matching the 7 fields in your UserResponse record:
        // id, username, email, userRole, enabled, manualLock, avatarUrl
        UserResponse response = new UserResponse(
                2L,
                "new_admin",
                "new_admin@internal.system",
                UserRole.ROLE_ADMIN, // Use the Enum here
                true,
                false,
                null
        );

        when(userService.createAdmin(any(AdminRegisterRequest.class))).thenReturn(response);

        // 2. Act & Assert
        mockMvc.perform(post("/api/users/admin")
                        .with(csrf()) // 必须加 CSRF
                        .with(user(adminPrincipal)) // 必须模拟 Admin 身份
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }



    @Test
    @DisplayName("POST /api/users/admin - Throws AlreadyExistsException")
    void createAdmin_Conflict() throws Exception {
        // 1. Arrange
        AdminRegisterRequest request = new AdminRegisterRequest("existing_admin", "password");

        when(userService.createAdmin(any(AdminRegisterRequest.class)))
                .thenThrow(new AlreadyExistsException("Admin username already taken"));

        // 2. Act & Assert
        mockMvc.perform(post("/api/users/admin")
                        .with(csrf())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict()); // 必须是 409，因为你模拟了冲突
    }

    @Test
    @DisplayName("POST /api/users/admin - Invalid Input (Empty Password)")
    void createAdmin_BadRequest() throws Exception {
        AdminRegisterRequest invalidRequest = new AdminRegisterRequest("admin", "");

        mockMvc.perform(post("/api/users/admin")
                        .with(csrf()) // 补上
                        .with(user(adminPrincipal)) // 补上
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // 此时权限过了，才会报 400
    }

    @Test
    @DisplayName("PUT /api/users/me - Failure: Email already taken")
    void updateSelf_EmailConflict() throws Exception {
        UserProfileRequest request = new UserProfileRequest("taken@example.com", null, null);

        when(userService.updateUser(eq(2L), any(User.class), eq("USER")))
                .thenThrow(new AlreadyExistsException("Email already taken"));

        mockMvc.perform(put("/api/users/me")
                        .with(csrf())
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
