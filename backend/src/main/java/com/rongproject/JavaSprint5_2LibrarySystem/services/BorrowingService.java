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

import java.time.LocalDateTime;
import java.util.List;

/**
 * Library Project - Unified Borrowing Service
 * Coordinates MySQL (Book/User) and MongoDB (BorrowLog)
 * Jules Version 2.7 - Fixed ID Mapping and Message Generation
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

        // 3. Record borrow log (MongoDB)
        BorrowLog log = BorrowLog.builder()
                .userId(userId)
                .bookId(bookId)
                .borrowDate(LocalDateTime.now())
                .status(LogStatus.BORROWED)
                .build();
        BorrowLog savedLog = borrowLogRepository.save(log);

        // 4. Return response via centralized mapper
        return convertToLogResponse(savedLog);
    }

    @Transactional
    public LogResponse returnBook(Long userId, Long bookId) {
        User user = userRepository.findByIdOrThrow(userId);
        Book book = bookRepository.getBookById(bookId);

        // Find active record
        BorrowLog log = borrowLogRepository.findFirstByUserIdAndBookIdAndStatusOrderByBorrowDateDesc(
                        userId, bookId, LogStatus.BORROWED)
                .orElseThrow(() -> new ResourceNotFoundException("No active borrowing record found."));

        // Update MongoDB
        log.setStatus(LogStatus.RETURNED);
        log.setReturnDate(LocalDateTime.now());
        BorrowLog savedLog = borrowLogRepository.save(log);

        // Update MySQL stock
        book.setAvailableStock(book.getAvailableStock() + 1);
        bookRepository.save(book);

        updateUserStatusAfterReturn(userId);

        return convertToLogResponse(savedLog);
    }

    // --- Query Methods ---

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

    public List<LogResponse> getLogsByUserName(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return getLogsByUserId(user.getId());
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

    // --- Private Mapper (The Solution) ---

    /**
     * Centralized mapper to LogResponse DTO
     * English Comment: Maps MongoDB logs to Record DTO with MySQL data hydration.
     */
    private LogResponse convertToLogResponse(BorrowLog log) {
        // Hydrate data from MySQL
        String bookTitle = bookRepository.findById(log.getBookId())
                .map(Book::getTitle)
                .orElse("Unknown Book");

        String username = userRepository.findById(log.getUserId())
                .map(User::getUsername)
                .orElse("Unknown User");

        // Dynamic Message Creation
        String message;
        if (log.getStatus() == LogStatus.BORROWED) {
            message = String.format("%s, you successfully borrowed \"%s\".", username, bookTitle);
        } else {
            message = String.format("%s, you have returned \"%s\". Status: %s.", username, bookTitle, log.getStatus());
        }

        // Construct Record with all IDs
        LogResponse response = new LogResponse(
                log.getId(),        // logId (String)
                log.getUserId(),    // userId (Long)
                username,           // username
                log.getBookId(),    // bookId (Long)
                bookTitle,          // bookTitle
                log.getBorrowDate(),// borrowDate
                log.getReturnDate(),// returnDate
                log.getStatus(),    // status
                message             // message

        );
        // Jules 诊断点：如果这行没打印，说明上面某行报错了
        System.out.println(">>> [JULES] Converted Log ID: " + log.getId() + " | Book: " + bookTitle);

        return response;
    }

    // --- Demo & Utility Methods (Restored by Jules) ---

    /**
     * Admin only: Manually update borrow date to simulate overdue scenarios.
     * English Comment: Essential for testing the 30-day overdue logic without waiting for a month.
     */
    @Transactional
    public LogResponse updateBorrowDateForDemo(String logId, LocalDateTime newDate) {
        // 1. Find the log in MongoDB
        BorrowLog log = borrowLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow log not found with id: " + logId));

        // 2. Update the date
        log.setBorrowDate(newDate);
        BorrowLog savedLog = borrowLogRepository.save(log);

        // 3. Return via the new mapper to ensure bookTitle and message are included
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
            userRepository.save(user);
        }
    }
}