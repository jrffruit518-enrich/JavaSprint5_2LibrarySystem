package com.rongproject.JavaSprint5_2LibrarySystem.service;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.LogResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.Book;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.BorrowLog;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.User;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.LogStatus;
import com.rongproject.JavaSprint5_2LibrarySystem.exceptions.ResourceNotFoundException;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.BookRepository;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.BorrowLogRepository;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.UserRepository;
import com.rongproject.JavaSprint5_2LibrarySystem.services.BorrowingService; // 确保类名一致
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BorrowLogServiceTest {

    @Mock private BorrowLogRepository borrowLogRepository;
    @Mock private UserRepository userRepository;
    @Mock private BookRepository bookRepository;

    @InjectMocks
    private BorrowingService borrowingService; // 注意：我们现在统称为 BorrowingService

    private User mockUser;
    private Book mockBook;
    private BorrowLog mockLog;

    @BeforeEach
    void setUp() {
        // English Comment: Standard setup for user, book and log entities
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("Tester");
        mockUser.setEnabled(true);
        mockUser.setManualLock(false);

        mockBook = new Book();
        mockBook.setId(10L);
        mockBook.setTitle("Spring Boot Guide");
        mockBook.setAvailableStock(2);

        mockLog = BorrowLog.builder()
                .id("mongo-666")
                .userId(1L)
                .bookId(10L)
                .status(LogStatus.BORROWED)
                .borrowDate(LocalDateTime.now())
                .build();
    }

    // --- 1. borrowBook Tests (Atomic Logic) ---

    @Test
    @DisplayName("borrowBook - Success with Atomic Stock")
    void borrowBook_Success() {
        // English Comment: Arrange
        when(userRepository.findByIdOrThrow(1L)).thenReturn(mockUser);
        // English Comment: Mocking the separate check for user borrowing status
        when(borrowLogRepository.existsByUserIdAndStatusAndBorrowDateBefore(anyLong(), any(), any())).thenReturn(false);
        when(borrowLogRepository.countByUserIdAndStatus(anyLong(), any())).thenReturn(0L);

        // English Comment: Mock the new atomic decrement method
        when(bookRepository.decrementStock(10L)).thenReturn(1);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(mockBook));
        when(borrowLogRepository.save(any(BorrowLog.class))).thenReturn(mockLog);

        // English Comment: Act
        LogResponse response = borrowingService.borrowBook(1L, 10L);

        // English Comment: Assert
        assertNotNull(response);
        verify(bookRepository).decrementStock(10L);
        verify(borrowLogRepository).save(any(BorrowLog.class));
    }

    // --- 2. returnBook Tests ---

    @Test
    @DisplayName("returnBook - Success Scenario")
    void returnBook_Success() {
        // English Comment: Arrange
        when(userRepository.findByIdOrThrow(1L)).thenReturn(mockUser);
        when(bookRepository.getBookById(10L)).thenReturn(mockBook);
        when(borrowLogRepository.findFirstByUserIdAndBookIdAndStatusOrderByBorrowDateDesc(1L, 10L, LogStatus.BORROWED))
                .thenReturn(Optional.of(mockLog));
        when(borrowLogRepository.save(any(BorrowLog.class))).thenReturn(mockLog);

        // English Comment: Act
        LogResponse response = borrowingService.returnBook(1L, 10L);

        // English Comment: Assert
        assertEquals(LogStatus.RETURNED, mockLog.getStatus());
        assertEquals(3, mockBook.getAvailableStock()); // Stock: 2 -> 3
        verify(bookRepository).save(mockBook);
    }

    // --- 3. updateBorrowDateForDemo (The Accelerator) ---

    @Test
    @DisplayName("updateBorrowDateForDemo - Move date back successfully")
    void updateBorrowDate_Success() {
        // English Comment: Arrange
        LocalDateTime overdueDate = LocalDateTime.now().minusDays(40);
        // English Comment: Assumes your repository has findByIdOrThrow for Mongo records
        when(borrowLogRepository.findById("mongo-666")).thenReturn(Optional.of(mockLog));
        when(borrowLogRepository.save(any())).thenReturn(mockLog);

        // English Comment: Needed for LogResponse mapping
        when(userRepository.findByIdOrThrow(1L)).thenReturn(mockUser);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(mockBook));

        // English Comment: Act
        LogResponse response = borrowingService.updateBorrowDateForDemo("mongo-666", overdueDate);

        // English Comment: Assert
        assertEquals(overdueDate, mockLog.getBorrowDate());
        verify(borrowLogRepository).save(mockLog);
    }

    // --- 4. User Status Recovery Test ---

    @Test
    @DisplayName("getUserBorrowingStatus - Re-enable user when clear")
    void getUserStatus_AutoEnable() {
        // English Comment: Arrange - User is disabled but has no overdue or limit issues
        mockUser.setEnabled(false);
        mockUser.setManualLock(false);
        when(userRepository.findByIdOrThrow(1L)).thenReturn(mockUser);
        when(borrowLogRepository.existsByUserIdAndStatusAndBorrowDateBefore(any(), any(), any())).thenReturn(false);
        when(borrowLogRepository.countByUserIdAndStatus(any(), any())).thenReturn(0L);

        // English Comment: Act
        var status = borrowingService.getUserBorrowingStatus(1L);

        // English Comment: Assert
        assertTrue(mockUser.isEnabled());
        assertTrue(status.canBorrow());
        verify(userRepository).save(mockUser);
    }
}