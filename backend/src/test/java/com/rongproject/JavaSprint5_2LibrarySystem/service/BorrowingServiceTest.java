package com.rongproject.JavaSprint5_2LibrarySystem.service;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.LogResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserStatusResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.configs.LibraryConstants;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.Book;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.BorrowLog;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.User;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.LogStatus;
import com.rongproject.JavaSprint5_2LibrarySystem.exceptions.ResourceNotFoundException;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.BookRepository;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.BorrowLogRepository;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.UserRepository;
import com.rongproject.JavaSprint5_2LibrarySystem.services.BorrowingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BorrowingServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private BookRepository bookRepository;
    @Mock private BorrowLogRepository borrowLogRepository;

    @InjectMocks
    private BorrowingService borrowingService;

    private User mockUser;
    private Book mockBook;

    @BeforeEach
    void setUp() {
        // English Comment: Initialize standard test data
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");
        mockUser.setEnabled(true);
        mockUser.setManualLock(false);

        mockBook = new Book();
        mockBook.setId(100L);
        mockBook.setTitle("Unit Test Guide");
        mockBook.setAvailableStock(5);
    }

    // --- 1. borrowBook Tests ---

    @Test
    @DisplayName("borrowBook - Success Scenario (Atomic Update)")
    void borrowBook_Success() {
        // English Comment: Arrange - User is eligible and stock update returns 1 (success)
        when(userRepository.findByIdOrThrow(1L)).thenReturn(mockUser);
        when(borrowLogRepository.existsByUserIdAndStatusAndBorrowDateBefore(anyLong(), any(), any())).thenReturn(false);
        when(borrowLogRepository.countByUserIdAndStatus(anyLong(), any())).thenReturn(0L);

        // English Comment: Crucial - Mocking the atomic decrement result
        when(bookRepository.decrementStock(100L)).thenReturn(1);
        when(bookRepository.findById(100L)).thenReturn(Optional.of(mockBook));

        BorrowLog savedLog = BorrowLog.builder().id("mongo-id-123").userId(1L).bookId(100L).status(LogStatus.BORROWED).build();
        when(borrowLogRepository.save(any(BorrowLog.class))).thenReturn(savedLog);

        // English Comment: Act
        LogResponse result = borrowingService.borrowBook(1L, 100L);

        // English Comment: Assert
        assertNotNull(result);
        assertEquals("Unit Test Guide", result.bookTitle());
        verify(bookRepository, times(1)).decrementStock(100L);
        verify(borrowLogRepository, times(1)).save(any(BorrowLog.class));
    }

    @Test
    @DisplayName("borrowBook - Failure: Out of Stock (Atomic check)")
    void borrowBook_Fail_NoStock() {
        when(userRepository.findByIdOrThrow(1L)).thenReturn(mockUser);
        when(borrowLogRepository.existsByUserIdAndStatusAndBorrowDateBefore(anyLong(), any(), any())).thenReturn(false);
        when(borrowLogRepository.countByUserIdAndStatus(anyLong(), any())).thenReturn(0L);

        // English Comment: Simulate atomic decrement failure (stock was 0)
        when(bookRepository.decrementStock(100L)).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> {
            borrowingService.borrowBook(1L, 100L);
        });
    }

    // --- 2. returnBook Tests ---

    @Test
    @DisplayName("returnBook - Success and Auto-Unlock")
    void returnBook_Success() {
        when(userRepository.findByIdOrThrow(1L)).thenReturn(mockUser);
        when(bookRepository.getBookById(100L)).thenReturn(mockBook);

        BorrowLog activeLog = BorrowLog.builder().id("id").status(LogStatus.BORROWED).build();
        when(borrowLogRepository.findFirstByUserIdAndBookIdAndStatusOrderByBorrowDateDesc(anyLong(), anyLong(), any()))
                .thenReturn(Optional.of(activeLog));
        when(borrowLogRepository.save(any())).thenReturn(activeLog);

        // English Comment: Mock status check after return (user has no more overdues)
        when(borrowLogRepository.existsByUserIdAndStatusAndBorrowDateBefore(anyLong(), any(), any())).thenReturn(false);
        when(borrowLogRepository.countByUserIdAndStatus(anyLong(), any())).thenReturn(0L);

        LogResponse result = borrowingService.returnBook(1L, 100L);

        // English Comment: Assert status change and stock recovery
        assertEquals(LogStatus.RETURNED, activeLog.getStatus());
        assertEquals(6, mockBook.getAvailableStock());
        verify(bookRepository).save(mockBook);
    }

    // --- 3. getUserBorrowingStatus & Manual Lock Tests ---

    @Test
    @DisplayName("getUserBorrowingStatus - Respect Manual Lock")
    void getUserStatus_ManualLockStaysDisabled() {
        // English Comment: Scenario - Account is manually locked by Admin
        mockUser.setEnabled(false);
        mockUser.setManualLock(true);
        when(userRepository.findByIdOrThrow(1L)).thenReturn(mockUser);

        // English Comment: Even if there are NO overdues and NO borrow count
        when(borrowLogRepository.existsByUserIdAndStatusAndBorrowDateBefore(any(), any(), any())).thenReturn(false);
        when(borrowLogRepository.countByUserIdAndStatus(any(), any())).thenReturn(0L);

        UserStatusResponse response = borrowingService.getUserBorrowingStatus(1L);

        // English Comment: Verify user stays disabled because of manualLock
        assertFalse(mockUser.isEnabled());
        assertFalse(response.canBorrow());
        // English Comment: Ensure save was never called to re-enable
        verify(userRepository, never()).save(mockUser);
    }

    @Test
    @DisplayName("getUserBorrowingStatus - Auto-Recovery Success")
    void getUserStatus_AutoEnableWithoutManualLock() {
        // English Comment: Scenario - Account disabled by system (manualLock is false)
        mockUser.setEnabled(false);
        mockUser.setManualLock(false);
        when(userRepository.findByIdOrThrow(1L)).thenReturn(mockUser);
        when(borrowLogRepository.existsByUserIdAndStatusAndBorrowDateBefore(any(), any(), any())).thenReturn(false);
        when(borrowLogRepository.countByUserIdAndStatus(any(), any())).thenReturn(0L);

        UserStatusResponse response = borrowingService.getUserBorrowingStatus(1L);

        // English Comment: Verify auto-recovery works when NOT manually locked
        assertTrue(mockUser.isEnabled());
        assertTrue(response.canBorrow());
        verify(userRepository).save(mockUser);
    }
}