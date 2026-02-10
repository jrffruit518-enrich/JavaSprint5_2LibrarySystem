package com.rongproject.JavaSprint5_2LibrarySystem.controller;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.LogResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.controllers.BorrowLogController;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.LogStatus;
import com.rongproject.JavaSprint5_2LibrarySystem.security.CustomUserDetails;
import com.rongproject.JavaSprint5_2LibrarySystem.security.JwtUtils;
import com.rongproject.JavaSprint5_2LibrarySystem.services.BorrowLogService;
import com.rongproject.JavaSprint5_2LibrarySystem.services.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BorrowLogController.class)
@AutoConfigureMockMvc
public class BorrowLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BorrowLogService borrowLogService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private CustomUserDetails mockUserDetails;
    private LogResponse mockLogResponse;

    @BeforeEach
    void setUp() {
        // English Comment: Prepare a mock user entity with necessary fields to avoid constructor errors
        com.rongproject.JavaSprint5_2LibrarySystem.entities.User userEntity =
                new com.rongproject.JavaSprint5_2LibrarySystem.entities.User();
        userEntity.setId(1L);
        userEntity.setUsername("testuser");
        userEntity.setPassword("protected");
        userEntity.setUserRole(com.rongproject.JavaSprint5_2LibrarySystem.enums.UserRole.ROLE_ADMIN);

        mockUserDetails = new CustomUserDetails(userEntity);

        mockLogResponse = new LogResponse(
                "mongo_log_001",
                "testuser",
                "Clean Code",
                LocalDateTime.now(),
                null,
                LogStatus.BORROWED,
                "你已成功借阅了《Clean Code》"
        );
    }

    @Test
    @DisplayName("POST /api/borrow-logs/borrow/{bookId} - Success")
    void borrowBook_Success() throws Exception {
        when(borrowLogService.borrowBook(anyLong(), anyLong())).thenReturn(mockLogResponse);

        mockMvc.perform(post("/api/borrow-logs/borrow/101")
                        .with(csrf())
                        .with(user(mockUserDetails)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.logId").value("mongo_log_001"))
                .andExpect(jsonPath("$.message").value(containsString("成功借阅")));
    }

    @Test
    @DisplayName("GET /api/borrow-logs - Admin Access")
    @WithMockUser(roles = "ADMIN")
    void getAllLogs_Success() throws Exception {
        when(borrowLogService.getAllLogs()).thenReturn(List.of(mockLogResponse));

        mockMvc.perform(get("/api/borrow-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/borrow-logs/user/{username} - Success")
    void getLogsByUserName_Success() throws Exception {
        when(borrowLogService.getLogsByUserName("testuser")).thenReturn(List.of(mockLogResponse));

        mockMvc.perform(get("/api/borrow-logs/user/testuser")
                        .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    @DisplayName("PATCH /api/borrow-logs/{recordId}/date-demo - Success")
    void updateBorrowDateForDemo_Success() throws Exception {
        LocalDateTime newDate = LocalDateTime.of(2023, 1, 1, 10, 0);
        LogResponse updatedResponse = new LogResponse(
                "mongo_log_001", "testuser", "Clean Code",
                newDate, null, LogStatus.BORROWED, "Date Updated"
        );

        when(borrowLogService.updateBorrowDateForDemo(eq("mongo_log_001"), any(LocalDateTime.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(patch("/api/borrow-logs/mongo_log_001/date-demo")
                        .param("newBorrowDate", "2023-01-01T10:00:00")
                        .with(csrf())
                        .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.borrowDate").exists());
    }

    @Test
    @DisplayName("POST /api/borrow-logs/borrow/{bookId} - Already Borrowed (Conflict)")
    void borrowBook_Fail_AlreadyBorrowed() throws Exception {
        when(borrowLogService.borrowBook(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("User has already borrowed this book"));

        mockMvc.perform(post("/api/borrow-logs/borrow/101")
                        .with(csrf())
                        .with(user(mockUserDetails)))
                .andExpect(status().isInternalServerError());
        // English Comment: If you haven't mapped RuntimeException in GlobalExceptionHandler, it defaults to 500
    }
}
