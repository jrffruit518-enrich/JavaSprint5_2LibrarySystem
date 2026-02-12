package com.rongproject.JavaSprint5_2LibrarySystem.service;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.BookCreationRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.BookResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.Book;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.BookGenre;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.LogStatus;
import com.rongproject.JavaSprint5_2LibrarySystem.exceptions.AlreadyExistsException;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
// English Comment: Set strictness to LENIENT if you still see UnnecessaryStubbingException during debugging
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class BookServiceTest {

    @Mock private BookRepository bookRepository;
    @Mock private BorrowLogRepository borrowLogRepository;

    @InjectMocks
    private BookService bookService;

    private Book mockBook;
    private BookCreationRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockBook = new Book();
        mockBook.setId(1L);
        mockBook.setTitle("Test Book");
        mockBook.setIsbn("1234567890");
        mockBook.setAvailableStock(10);

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

        assertThrows(AlreadyExistsException.class, () -> bookService.createBook(mockRequest));
        verify(bookRepository, never()).save(any(Book.class));
    }

    // --- Get Book Tests ---

    @Test
    @DisplayName("Get Book By ID - Success")
    void getBookById_Success() {
        // English Comment: Fixed - Service calls getBookById, not findById
        when(bookRepository.getBookById(1L)).thenReturn(mockBook);

        BookResponse response = bookService.getBookById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
    }

    @Test
    @DisplayName("Get Book By ID - Failure (Not Found)")
    void getBookById_ThrowsException_WhenNotFound() {
        // English Comment: Fixed - Mock the exception directly if getBookById is designed to throw it
        when(bookRepository.getBookById(99L)).thenThrow(new ResourceNotFoundException("Not Found"));

        assertThrows(ResourceNotFoundException.class, () -> bookService.getBookById(99L));
    }

    // --- Update Book Tests ---

    @Test
    @DisplayName("Update Book - Success")
    void updateBook_Success() {
        // English Comment: Match the method name used in Service (getBookById)
        when(bookRepository.getBookById(1L)).thenReturn(mockBook);
        when(bookRepository.save(any(Book.class))).thenReturn(mockBook);

        BookResponse response = bookService.updateBook(1L, mockRequest);

        assertNotNull(response);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("Update Book - Failure (ISBN taken)")
    void updateBook_ThrowsException_WhenNewIsbnExists() {
        BookCreationRequest newRequest = new BookCreationRequest(
                "New Title", "Author", "9999999999",
                BookGenre.FICTION, LocalDate.now(), 4.0, "Desc", 5, "url"
        );

        when(bookRepository.getBookById(1L)).thenReturn(mockBook);
        when(bookRepository.existsByIsbn("9999999999")).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> bookService.updateBook(1L, newRequest));
    }

    // --- Delete Book Tests ---

    @Test
    @DisplayName("Delete Book - Success")
    void deleteBook_Success() {
        when(bookRepository.getBookById(1L)).thenReturn(mockBook);
        when(borrowLogRepository.existsByBookIdAndStatus(1L, LogStatus.BORROWED)).thenReturn(false);

        bookService.deleteBook(1L);

        // English Comment: verify that delete was called with the actual mockBook instance
        verify(bookRepository).delete(mockBook);
    }

    @Test
    @DisplayName("Delete Book - Failure: Currently Borrowed")
    void deleteBook_Fail_BeingBorrowed() {
        when(bookRepository.getBookById(1L)).thenReturn(mockBook);
        when(borrowLogRepository.existsByBookIdAndStatus(1L, LogStatus.BORROWED)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> bookService.deleteBook(1L));
        verify(bookRepository, never()).delete(any());
    }
}