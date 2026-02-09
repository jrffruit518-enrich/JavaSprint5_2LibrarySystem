package com.rongproject.JavaSprint5_2LibrarySystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.BookCreationRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.BookResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.controllers.BookController;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.BookGenre;
import com.rongproject.JavaSprint5_2LibrarySystem.services.BookService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class) // 只加载 Web 层，不启动整个 Context
@AutoConfigureMockMvc
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService; // 模拟 Service 层的行为

    @Autowired
    private ObjectMapper objectMapper; // 用于将 DTO 转为 JSON 字符串

    private BookResponse mockResponse;
    private BookCreationRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockResponse = new BookResponse(1L, "Effective Java", "Joshua Bloch", "9780134685991",
                BookGenre.SCIENCE, LocalDate.now(), 4.9, "Classic", 5, "url");

        mockRequest = new BookCreationRequest("Effective Java", "Joshua Bloch", "9780134685991",
                BookGenre.SCIENCE, LocalDate.now(), 4.9, "Classic", 5, "url");
    }

    // --- GET ALL BOOKS ---

    @Test
    @DisplayName("GET /api/books - Success")
    @WithMockUser // 模拟已登录用户
    void getAllBooks_Success() throws Exception {
        when(bookService.getAllBooks()).thenReturn(List.of(mockResponse));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Effective Java"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    // --- GET BOOK BY ID ---

    @Test
    @DisplayName("GET /api/books/{id} - Success")
    @WithMockUser
    void getBookById_Success() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    // --- CREATE BOOK ---

    @Test
    @DisplayName("POST /api/books - Success (ADMIN)")
    @WithMockUser(roles = "ADMIN") // 关键：模拟管理员身份
    void createBook_Success_Admin() throws Exception {
        when(bookService.createBook(any(BookCreationRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/books")
                        .with(csrf()) // 必须带上 CSRF，否则会被 Security 拦截
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Effective Java"));
    }

    @Test
    @DisplayName("POST /api/books - Failure (Forbidden for USER)")
    @WithMockUser(roles = "USER") // 模拟普通用户
    void createBook_Forbidden_User() throws Exception {
        mockMvc.perform(post("/api/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockRequest)))
                .andExpect(status().isForbidden()); // 验证返回 403
    }

    // --- UPDATE BOOK ---

    @Test
    @DisplayName("PUT /api/books/{id} - Success (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void updateBook_Success() throws Exception {
        when(bookService.updateBook(eq(1L), any(BookCreationRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(put("/api/books/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockRequest)))
                .andExpect(status().isOk());
    }

    // --- DELETE BOOK ---

    @Test
    @DisplayName("DELETE /api/books/{id} - Success (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void deleteBook_Success() throws Exception {
        mockMvc.perform(delete("/api/books/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/books/{id} - Failure (Unauthorized)")
    void deleteBook_Failure_Anonymous() throws Exception {
        // 不带 @WithMockUser，模拟匿名用户
        mockMvc.perform(delete("/api/books/1").with(csrf()))
                .andExpect(status().isUnauthorized()); // 应该重定向或返回 401/403
    }
}
