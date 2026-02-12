package com.rongproject.JavaSprint5_2LibrarySystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.BookCreationRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.BookResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.controllers.BookController;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.BookGenre;
import com.rongproject.JavaSprint5_2LibrarySystem.exceptions.AlreadyExistsException;
import com.rongproject.JavaSprint5_2LibrarySystem.security.JwtUtils;
import com.rongproject.JavaSprint5_2LibrarySystem.services.BookService;
import com.rongproject.JavaSprint5_2LibrarySystem.services.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

// English Comment: Required imports for CSRF and MockMvc
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc
@EnableMethodSecurity // English Comment: Crucial! This enables @PreAuthorize during the test
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private BookResponse defaultResponse;
    private BookCreationRequest validRequest;

    @BeforeEach
    void setUp() {
        defaultResponse = new BookResponse(
                1L, "Effective Java", "Joshua Bloch", "9780134685991",
                BookGenre.SCIENCE, LocalDate.of(2018, 1, 6), 4.9,
                "A comprehensive guide to Java best practices.", 5,
                "https://example.com/cover.jpg"
        );

        validRequest = new BookCreationRequest(
                "Effective Java", "Joshua Bloch", "9780134685991",
                BookGenre.SCIENCE, LocalDate.of(2018, 1, 6), 4.9,
                "A comprehensive guide to Java best practices.", 5,
                "https://example.com/cover.jpg"
        );
    }

    // --- 1. GET ALL BOOKS ---
    @Test
    @WithMockUser // English Comment: Added to prevent 401 if endpoint is not permitAll
    @DisplayName("GET /api/books - Success")
    void getAllBooks_Success() throws Exception {
        when(bookService.getAllBooks()).thenReturn(List.of(defaultResponse));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Effective Java"));
    }

    // --- 2. GET BOOK BY ID ---
    @Test
    @WithMockUser // English Comment: Added to prevent 401
    @DisplayName("GET /api/books/{id} - Success")
    void getBookById_Success() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(defaultResponse);

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value("9780134685991"));
    }

    // --- 3. CREATE BOOK (ADMIN ONLY) ---
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/books - Success as ADMIN")
    void createBook_Success() throws Exception {
        when(bookService.createBook(any(BookCreationRequest.class))).thenReturn(defaultResponse);

        mockMvc.perform(post("/api/books")
                        .with(csrf()) // English Comment: Crucial to prevent 403
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Effective Java"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/books - Forbidden for regular USER")
    void createBook_Forbidden() throws Exception {
        mockMvc.perform(post("/api/books")
                        .with(csrf()) // English Comment: Added CSRF here too
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }

    // --- 4. UPDATE BOOK (ADMIN ONLY) ---
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/books/{id} - Success as ADMIN")
    void updateBook_Success() throws Exception {
        when(bookService.updateBook(eq(1L), any(BookCreationRequest.class))).thenReturn(defaultResponse);

        mockMvc.perform(put("/api/books/1")
                        .with(csrf()) // English Comment: Crucial to prevent 403
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author").value("Joshua Bloch"));
    }

    // --- 5. DELETE BOOK (ADMIN ONLY) ---
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/books/{id} - Success as ADMIN")
    void deleteBook_Success() throws Exception {
        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/api/books/1")
                        .with(csrf())) // English Comment: Crucial to prevent 403
                .andExpect(status().isNoContent());
    }

    // --- 6. VALIDATION TEST ---
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/books - Failure due to Validation (ISBN length)")
    void createBook_ValidationFailure() throws Exception {
        BookCreationRequest invalidRequest = new BookCreationRequest(
                "Title", "Author", "12345", BookGenre.ART,
                LocalDate.now(), 5.0, "Description", 10, null
        );

        mockMvc.perform(post("/api/books")
                        .with(csrf()) // English Comment: Crucial to prevent 403
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/books - Failure: ISBN already exists")
    void createBook_Conflict() throws Exception {
        // 1. Mock Service 抛出自定义异常
        when(bookService.createBook(any(BookCreationRequest.class)))
                .thenThrow(new AlreadyExistsException("Book with this ISBN already exists"));

        // 2. 验证是否返回 409 Conflict
        mockMvc.perform(post("/api/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/books/{id} - Failure: ISBN taken by another book")
    void updateBook_Conflict() throws Exception {
        when(bookService.updateBook(eq(1L), any(BookCreationRequest.class)))
                .thenThrow(new AlreadyExistsException("ISBN already taken"));

        mockMvc.perform(put("/api/books/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict());
    }
}