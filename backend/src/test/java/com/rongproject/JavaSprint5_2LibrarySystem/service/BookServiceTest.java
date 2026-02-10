package com.rongproject.JavaSprint5_2LibrarySystem.service;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.BookCreationRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.BookResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.Book;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.BookGenre;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.LogStatus;
import com.rongproject.JavaSprint5_2LibrarySystem.exceptions.ResourceNotFoundException;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.BookRepository;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.BorrowLogRepository;
import com.rongproject.JavaSprint5_2LibrarySystem.services.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowLogRepository borrowLogRepository;

    @InjectMocks
    private BookService bookService;

    private Book mockBook;
    private BookCreationRequest mockRequest;

    @BeforeEach
    void setUp() {
        // English Comment: Initialize standard book object for all tests
        mockBook = new Book();
        mockBook.setId(1L);
        mockBook.setTitle("Test Book");
        mockBook.setIsbn("1234567890");
        mockBook.setAvailableStock(10); // English Comment: Ensure stock is set for logic checks

        mockRequest = new BookCreationRequest(
                "Test Book", "Author", "1234567890",
                BookGenre.SCIENCE, LocalDate.now(), 4.5,
                "Description", 10, "url"
        );
    }

    // --- Create Book Tests ---

    @Test
    @DisplayName("Create Book - Success")
    void createBook_Success() {
        // English Comment: Arrange
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(mockBook);

        // English Comment: Act
        BookResponse response = bookService.createBook(mockRequest);

        // English Comment: Assert
        assertNotNull(response);
        assertEquals("Test Book", response.title());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("Create Book - Failure (Duplicate ISBN)")
    void createBook_ThrowsException_WhenIsbnExists() {
        when(bookRepository.existsByIsbn(anyString())).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> bookService.createBook(mockRequest));
        verify(bookRepository, never()).save(any(Book.class));
    }

    // --- Get Book Tests ---

    @Test
    @DisplayName("Get Book By ID - Success")
    void getBookById_Success() {
        // English Comment: Updated to use the common findByIdOrThrow or findById pattern
        when(bookRepository.findById(1L)).thenReturn(Optional.of(mockBook));

        BookResponse response = bookService.getBookById(1L);

        assertEquals(1L, response.id());
        assertEquals("Test Book", response.title());
    }

    @Test
    @DisplayName("Get Book By ID - Failure (Not Found)")
    void getBookById_ThrowsException_WhenNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.getBookById(99L));
    }

    // --- Update Book Tests ---

    @Test
    @DisplayName("Update Book - Success")
    void updateBook_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(mockBook));
        when(bookRepository.save(any(Book.class))).thenReturn(mockBook);

        BookResponse response = bookService.updateBook(1L, mockRequest);

        assertNotNull(response);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("Update Book - Failure (ISBN taken by another book)")
    void updateBook_ThrowsException_WhenNewIsbnExists() {
        BookCreationRequest newRequest = new BookCreationRequest(
                "New Title", "Author", "9999999999", // New ISBN
                BookGenre.FICTION, LocalDate.now(), 4.0, "Desc", 5, "url"
        );

        when(bookRepository.findById(1L)).thenReturn(Optional.of(mockBook));
        // English Comment: Simulate the NEW ISBN already belongs to a DIFFERENT book
        when(bookRepository.existsByIsbn("9999999999")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> bookService.updateBook(1L, newRequest));
        verify(bookRepository, never()).save(any(Book.class));
    }

    // --- Delete Book Tests ---

    @Test
    @DisplayName("Delete Book - Success")
    void deleteBook_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(mockBook));
        // English Comment: Book is not currently in anyone's possession
        when(borrowLogRepository.existsByBookIdAndStatus(1L, LogStatus.BORROWED)).thenReturn(false);

        bookService.deleteBook(1L);

        verify(bookRepository, times(1)).delete(mockBook);
    }

    @Test
    @DisplayName("Delete Book - Failure: Currently Borrowed")
    void deleteBook_Fail_BeingBorrowed() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(mockBook));
        // English Comment: Simulate active borrowing record in MongoDB
        when(borrowLogRepository.existsByBookIdAndStatus(1L, LogStatus.BORROWED)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookService.deleteBook(1L);
        });

        assertTrue(exception.getMessage().toLowerCase().contains("borrowed"));
        verify(bookRepository, never()).delete(any());
    }
}