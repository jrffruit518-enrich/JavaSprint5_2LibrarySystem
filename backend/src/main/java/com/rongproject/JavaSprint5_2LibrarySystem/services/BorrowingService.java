package com.rongproject.JavaSprint5_2LibrarySystem.services;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Library Project - Unified Borrowing Service
 * Coordinates MySQL (Book/User) and MongoDB (BorrowLog)
 */
@Service
@RequiredArgsConstructor
public class BorrowingService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowLogRepository borrowLogRepository;

    // --- Core Transactional Methods (MySQL + MongoDB) ---

    @Transactional
    public LogResponse borrowBook(Long userId, Long bookId) {
        // 1. Fetch user and validate eligibility
        User user = userRepository.findByIdOrThrow(userId);
        UserStatusResponse status = getUserBorrowingStatus(userId);

        if (!status.canBorrow()) {
            String reason = status.hasOverdue() ? "You have overdue books." :
                    (status.borrowCount() >= LibraryConstants.MAX_BORROW_LIMIT ? "Borrowing limit reached." : "Account disabled.");
            throw new IllegalStateException("User not eligible: " + reason);
        }

        // 2. Atomic stock decrement (MySQL)
        int updatedRows = bookRepository.decrementStock(bookId);
        if (updatedRows == 0) {
            throw new IllegalStateException("Book is currently out of stock.");
        }

        // 3. Fetch book details
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found."));

        // 4. Record borrow log (MongoDB)
        BorrowLog log = BorrowLog.builder()
                .userId(userId)
                .bookId(bookId)
                .borrowDate(LocalDateTime.now())
                .status(LogStatus.BORROWED)
                .build();
        BorrowLog savedLog = borrowLogRepository.save(log);

        // 5. Build success response
        LocalDate dueDate = LocalDate.now().plusDays(LibraryConstants.OVERDUE_DAYS);
        String message = String.format("Success! %s, you borrowed \"%s\". Due: %s.",
                user.getUsername(), book.getTitle(), dueDate);

        return convertToLogResponse(savedLog);
    }

    @Transactional
    public LogResponse returnBook(Long userId, Long bookId) {
        // 1. Fetch user and book
        User user = userRepository.findByIdOrThrow(userId);
        Book book = bookRepository.getBookById(bookId);

        // 2. Find the active borrow log (MongoDB)
        BorrowLog log = borrowLogRepository.findFirstByUserIdAndBookIdAndStatusOrderByBorrowDateDesc(
                        userId, bookId, LogStatus.BORROWED)
                .orElseThrow(() -> new ResourceNotFoundException("No active borrowing record found."));

        // 3. Update MongoDB: Mark as returned
        log.setStatus(LogStatus.RETURNED);
        log.setReturnDate(LocalDateTime.now());
        BorrowLog savedLog = borrowLogRepository.save(log);

        // 4. Atomic stock increment (MySQL)
        book.setAvailableStock(book.getAvailableStock() + 1);
        bookRepository.save(book);

        // 5. Automatic status update
        updateUserStatusAfterReturn(userId);

        return convertToLogResponse(savedLog);
    }

    // --- Query Methods (Migrated & Refined) ---

    /**
     * Admin: Get all records from MongoDB
     */
    public List<LogResponse> getAllLogs() {
        return borrowLogRepository.findAll()
                .stream()
                .map(this::convertToLogResponse)
                .toList();
    }

    public List<LogResponse> getLogsByUserId(Long userId) {
        return borrowLogRepository.findByUserId(userId)
                .stream()
                .map(this::convertToLogResponse)
                .toList();
    }

    /**
     * Admin/User: Get logs by username
     */
    public List<LogResponse> getLogsByUserName(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return borrowLogRepository.findByUserId(user.getId())
                .stream()
                .map(this::convertToLogResponse)
                .toList();
    }

    public List<LogResponse> getOngoingLoans(Long userId) {
        List<LogStatus> activeStatuses = List.of(LogStatus.BORROWED, LogStatus.OVERDUE);
        return borrowLogRepository.findByUserIdAndStatusIn(userId, activeStatuses)
                .stream()
                .map(this::convertToLogResponse)
                .toList();
    }

    public List<LogResponse> getLoanHistory(Long userId, String searchKeyword) {
        List<BorrowLog> history = borrowLogRepository.findByUserIdAndStatus(userId, LogStatus.RETURNED);

        return history.stream()
                .map(this::convertToLogResponse)
                .filter(log -> searchKeyword == null || searchKeyword.isBlank() ||
                        log.bookTitle().toLowerCase().contains(searchKeyword.toLowerCase()))
                .toList();
    }

    public UserStatusResponse getUserBorrowingStatus(Long userId) {
        User user = userRepository.findByIdOrThrow(userId);

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(LibraryConstants.OVERDUE_DAYS);
        boolean hasOverdue = borrowLogRepository.existsByUserIdAndStatusAndBorrowDateBefore(
                userId, LogStatus.BORROWED, thirtyDaysAgo);
        long activeBorrowCount = borrowLogRepository.countByUserIdAndStatus(userId, LogStatus.BORROWED);

        // Auto-enable logic
        if (!user.isEnabled() && !user.isManualLock() && !hasOverdue && activeBorrowCount < LibraryConstants.MAX_BORROW_LIMIT) {
            user.setEnabled(true);
            userRepository.save(user);
        }

        return new UserStatusResponse(
                user.isEnabled() && !hasOverdue && activeBorrowCount < LibraryConstants.MAX_BORROW_LIMIT,
                user.isEnabled(),
                hasOverdue,
                activeBorrowCount
        );
    }

    // --- Demo & Utility Methods ---

    @Transactional
    public LogResponse updateBorrowDateForDemo(String logId, LocalDateTime newDate) {
        BorrowLog log = borrowLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow log not found with id: " + logId));

        log.setBorrowDate(newDate);
        BorrowLog savedLog = borrowLogRepository.save(log);

        return convertToLogResponse(savedLog);
    }

    private void updateUserStatusAfterReturn(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isManualLock()) return;

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(LibraryConstants.OVERDUE_DAYS);
        boolean hasOtherOverdue = borrowLogRepository.existsByUserIdAndStatusAndBorrowDateBefore(
                userId, LogStatus.BORROWED, thirtyDaysAgo);
        long activeBorrowCount = borrowLogRepository.countByUserIdAndStatus(userId, LogStatus.BORROWED);

        if (!hasOtherOverdue && activeBorrowCount < LibraryConstants.MAX_BORROW_LIMIT) {
            user.setEnabled(true);
            user.setManualLock(false);
            userRepository.save(user);
        }
    }

    /**
     * Centralized mapper to LogResponse DTO
     */
    private LogResponse convertToLogResponse(BorrowLog log) {
        String bookTitle = bookRepository.findById(log.getBookId())
                .map(Book::getTitle)
                .orElse("Unknown Book");

        String username = userRepository.findById(log.getUserId())
                .map(User::getUsername)
                .orElse("Unknown User");

        String message;
        if (log.getStatus() == LogStatus.BORROWED) {
            message = String.format("%s, you have borrowed \"%s\". Please return it on time.", username, bookTitle);
        } else {
            message = String.format("%s, you have returned \"%s\". Thank you!", username, bookTitle);
        }

        return new LogResponse(
                log.getId(),
                username,
                bookTitle,
                log.getBorrowDate(),
                log.getReturnDate(),
                log.getStatus(),
                message
        );
    }
}