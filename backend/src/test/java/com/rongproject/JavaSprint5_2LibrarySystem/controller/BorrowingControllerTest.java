package com.rongproject.JavaSprint5_2LibrarySystem.controller;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.LogResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserStatusResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.controllers.BorrowingController;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.LogStatus;
import com.rongproject.JavaSprint5_2LibrarySystem.security.CustomUserDetails;
import com.rongproject.JavaSprint5_2LibrarySystem.security.JwtUtils;
import com.rongproject.JavaSprint5_2LibrarySystem.services.BorrowingService;
import com.rongproject.JavaSprint5_2LibrarySystem.services.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.containsString;
@WebMvcTest(BorrowingController.class)
@AutoConfigureMockMvc
public class BorrowingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BorrowingService borrowingService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private CustomUserDetails mockUserDetails;
    private LogResponse mockLogResponse;

    @BeforeEach
    void setUp() {
        // English Comment: Mock the custom user details with ID 1
        com.rongproject.JavaSprint5_2LibrarySystem.entities.User userEntity =
                new com.rongproject.JavaSprint5_2LibrarySystem.entities.User();
        userEntity.setId(1L);
        userEntity.setUsername("testuser");
        // English Comment: Mandatory! Spring Security User constructor fails if password is null
        userEntity.setPassword("placeholder_password");
        userEntity.setUserRole(com.rongproject.JavaSprint5_2LibrarySystem.enums.UserRole.ROLE_USER);

        mockUserDetails = new CustomUserDetails(userEntity);

        mockLogResponse = new LogResponse(
                "log123",
                "testuser",
                "Effective Java",
                LocalDateTime.now(),
                null,
                LogStatus.BORROWED,
                "Success! You borrowed Effective Java"
        );
    }

    // --- 1. BORROW BOOK ---
    @Test
    @DisplayName("POST /api/borrowings/borrow/{id} - Success")
    void borrowBook_Success() throws Exception {
        when(borrowingService.borrowBook(eq(1L), eq(101L))).thenReturn(mockLogResponse);

        mockMvc.perform(post("/api/borrowings/borrow/101")
                        .with(csrf())
                        .with(user(mockUserDetails))) // English Comment: Inject our CustomUserDetails
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.logId").value("log123"))
                .andExpect(jsonPath("$.message").value(containsString("Success")));
    }

    @Test
    @DisplayName("POST /api/borrowings/borrow/{id} - Failure (Stock Out)")
    void borrowBook_Failure_StockOut() throws Exception {
        when(borrowingService.borrowBook(anyLong(), anyLong()))
                .thenThrow(new IllegalStateException("Book is currently out of stock."));

        mockMvc.perform(post("/api/borrowings/borrow/101")
                        .with(csrf())
                        .with(user(mockUserDetails)))
                // English Comment: Change from isInternalServerError() to isConflict() to match GlobalExceptionHandler
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Book is currently out of stock."));
    }

    // --- 2. RETURN BOOK ---
    @Test
    @DisplayName("POST /api/borrowings/return/{id} - Success")
    void returnBook_Success() throws Exception {
        LogResponse returnResponse = new LogResponse(
                "log123", "testuser", "Effective Java",
                LocalDateTime.now().minusDays(1), LocalDateTime.now(),
                LogStatus.RETURNED, "Returned successfully"
        );

        when(borrowingService.returnBook(eq(1L), eq(101L))).thenReturn(returnResponse);

        mockMvc.perform(post("/api/borrowings/return/101")
                        .with(csrf())
                        .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));
    }

    // --- 3. GET STATUS ---
    @Test
    @WithMockUser // English Comment: Standard mock user is fine here as we use @PathVariable userId
    @DisplayName("GET /api/borrowings/{userId}/status - Success")
    void getStatus_Success() throws Exception {
        UserStatusResponse statusResponse = new UserStatusResponse(
                true,  // canBorrow
                true,  // isEnabled
                false, // hasOverdue
                2L     // borrowCount (long)
        );

        when(borrowingService.getUserBorrowingStatus(1L)).thenReturn(statusResponse);

        mockMvc.perform(get("/api/borrowings/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.borrowCount").value(2))
                .andExpect(jsonPath("$.canBorrow").value(true));
    }
}