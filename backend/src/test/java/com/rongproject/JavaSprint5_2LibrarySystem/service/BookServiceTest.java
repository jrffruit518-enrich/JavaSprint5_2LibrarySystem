package com.rongproject.JavaSprint5_2LibrarySystem.service;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.BookCreationRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.BookResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.Book;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.BookGenre;
import com.rongproject.JavaSprint5_2LibrarySystem.exceptions.ResourceNotFoundException;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.BookRepository;
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

    @InjectMocks
    private BookService bookService;

    private Book mockBook;
    private BookCreationRequest mockRequest;

    @BeforeEach
    void setUp() {
        // Initialize common test data
        mockBook = new Book();
        mockBook.setId(1L);
        mockBook.setTitle("Test Book");
        mockBook.setIsbn("1234567890");

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
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(mockBook);

        BookResponse response = bookService.createBook(mockRequest);

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
        when(bookRepository.getBookById(1L)).thenReturn(mockBook);

        BookResponse response = bookService.getBookById(1L);

        assertEquals(1L, response.id());
    }

    @Test
    @DisplayName("Get Book By ID - Failure (Not Found)")
    void getBookById_ThrowsException_WhenNotFound() {
        // Mocking the default method behavior from repository
        when(bookRepository.getBookById(99L)).thenThrow(new ResourceNotFoundException("Not Found"));

        assertThrows(ResourceNotFoundException.class, () -> bookService.getBookById(99L));
    }

    // --- Update Book Tests ---

    @Test
    @DisplayName("Update Book - Success")
    void updateBook_Success() {
        when(bookRepository.getBookById(1L)).thenReturn(mockBook);
        when(bookRepository.save(any(Book.class))).thenReturn(mockBook);

        BookResponse response = bookService.updateBook(1L, mockRequest);

        assertNotNull(response);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("Update Book - Failure (ISBN taken by another book)")
    void updateBook_ThrowsException_WhenNewIsbnExists() {
        // Old ISBN is "1234567890", new request ISBN is "0000000000"
        BookCreationRequest newRequest = new BookCreationRequest(
                "New Title", "Author", "0000000000",
                BookGenre.FICTION, LocalDate.now(), 4.0, "Desc", 5, "url"
        );

        when(bookRepository.getBookById(1L)).thenReturn(mockBook);
        when(bookRepository.existsByIsbn("0000000000")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> bookService.updateBook(1L, newRequest));
        verify(bookRepository, never()).save(any(Book.class));
    }

    // --- Delete Book Tests ---

    @Test
    @DisplayName("Delete Book - Success")
    void deleteBook_Success() {
        when(bookRepository.getBookById(1L)).thenReturn(mockBook);

        bookService.deleteBook(1L);

        verify(bookRepository, times(1)).delete(mockBook);
    }
}
