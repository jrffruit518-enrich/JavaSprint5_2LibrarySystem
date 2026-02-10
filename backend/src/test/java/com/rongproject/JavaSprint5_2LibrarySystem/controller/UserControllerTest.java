package com.rongproject.JavaSprint5_2LibrarySystem.controller;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserProfileRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserRegisterRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.controllers.UserController;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.User;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.UserRole;
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
        // English Comment: Ensure the mock object exists for the DTO
        UserProfileRequest request = new UserProfileRequest("new@example.com", "new_avatar", "123456");

        // English Comment: Match the ID used in userPrincipal (which is 2L)
        when(userService.updateUser(eq(2L), any(User.class), eq("USER"))).thenReturn(mockUserResponse);

        mockMvc.perform(put("/api/users/me")
                        .with(csrf())
                        .with(user(userPrincipal)) // This sets the security context for SecurityUtils
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
    @DisplayName("GET /api/users/{id} - User cannot see other user's profile")
    void getUserById_OtherUserForbidden() throws Exception {
        // English Comment: userPrincipal has ID 2, trying to access ID 1
        mockMvc.perform(get("/api/users/1")
                        .with(user(userPrincipal)))
                .andExpect(status().isForbidden());
    }
}
