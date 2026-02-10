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

@Service
@RequiredArgsConstructor
public class BorrowingService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowLogRepository borrowLogRepository;

    // TODO: This method is the single entry for borrowing.
// Future concurrency control / transaction upgrade goes here.
    @Transactional
    public LogResponse borrowBook(Long userId, Long bookId) {
        // 1. 获取用户并校验资格
        User user = userRepository.findByIdOrThrow(userId);
        UserStatusResponse status = getUserBorrowingStatus(userId);

        if (!status.canBorrow()) {
            // English Comment: Build a detailed error message based on the status flags
            String reason = status.hasOverdue() ? "You have overdue books." :
                    (status.borrowCount() >= LibraryConstants.MAX_BORROW_LIMIT ? "Borrowing limit reached." : "Account disabled.");
            throw new IllegalStateException("User not eligible: " + reason);
        }

        // 2. 原子扣减库存 (核心并发安全)
        int updatedRows = bookRepository.decrementStock(bookId);
        if (updatedRows == 0) {
            throw new IllegalStateException("Book is currently out of stock.");
        }

        // 3. 获取书籍信息 (用于后续显示)
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found."));

        // 4. MongoDB: 记录借阅日志
        BorrowLog log = BorrowLog.builder()
                .userId(userId)
                .bookId(bookId)
                .borrowDate(LocalDateTime.now())
                .status(LogStatus.BORROWED)
                .build();
        BorrowLog savedLog = borrowLogRepository.save(log);

        // 5. 跨库补偿标记 (毕设加分项)
        // English Comment: Entry point for distributed transaction compensation if MongoDB fails.
        // In a production environment, we would use a message queue or transaction log here.

        // 6. 返回友好的日期格式
        LocalDate dueDate = LocalDate.now().plusDays(LibraryConstants.OVERDUE_DAYS);
        String message = String.format("Success! %s, you borrowed \"%s\". Due: %s.",
                user.getUsername(), book.getTitle(), dueDate);

        return new LogResponse(
                savedLog.getId(),
                user.getUsername(),
                book.getTitle(),
                savedLog.getBorrowDate(),
                null, // Return date is null at borrowing
                savedLog.getStatus(),
                message
        );
    }

    @Transactional
    public LogResponse returnBook(Long userId, Long bookId) {
        // 1. 获取用户和图书 (用于后续组装 Response，同时确保 ID 有效)
        User user = userRepository.findByIdOrThrow(userId);
        Book book = bookRepository.getBookById(bookId);

        // 2. Find the active borrow log
        BorrowLog log = borrowLogRepository.findFirstByUserIdAndBookIdAndStatusOrderByBorrowDateDesc(
                        userId, bookId, LogStatus.BORROWED)
                .orElseThrow(() -> new ResourceNotFoundException("No active borrowing record found."));

        // 3. Update MongoDB: Mark as returned
        log.setStatus(LogStatus.RETURNED);
        log.setReturnDate(LocalDateTime.now());
        BorrowLog savedLog = borrowLogRepository.save(log);

        // 4. Update MySQL: Increment book stock
        book.setAvailableStock(book.getAvailableStock() + 1);
        bookRepository.save(book);

        // 5. Logic A: Re-enable user if compliant
        updateUserStatusAfterReturn(userId);

        // 6. 组装个性化消息
        String message = String.format("Thank you, %s! \"%s\" has been successfully returned. We appreciate your promptness.",
                user.getUsername(),
                book.getTitle());

        // 7. 返回 Response DTO
        return new LogResponse(
                savedLog.getId(),
                user.getUsername(),
                book.getTitle(),
                savedLog.getBorrowDate(),
                savedLog.getReturnDate(),
                savedLog.getStatus(),
                message
        );
    }

    public UserStatusResponse getUserBorrowingStatus(Long userId) {
        User user = userRepository.findByIdOrThrow(userId);

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(LibraryConstants.OVERDUE_DAYS);
        boolean hasOverdue = borrowLogRepository.existsByUserIdAndStatusAndBorrowDateBefore(
                userId, LogStatus.BORROWED, thirtyDaysAgo);
        long activeBorrowCount = borrowLogRepository.countByUserIdAndStatus(userId, LogStatus.BORROWED);

        // Logic A: Automatically update status ONLY IF NOT MANUALLY LOCKED
        // English Comment: Only auto-enable if the account wasn't locked by an Admin
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

    /**
     * Accelerator for Demo: Manually update borrow date to simulate overdue.
     * English Comment: This is a helper method for demonstration purposes only.
     */
    @Transactional
    public LogResponse updateBorrowDateForDemo(String logId, LocalDateTime newDate) {
        // 1. Find the log in MongoDB
        BorrowLog log = borrowLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow log not found with id: " + logId));

        // 2. Update the date
        log.setBorrowDate(newDate);
        BorrowLog savedLog = borrowLogRepository.save(log);

        // 3. Map to response (Reuse existing logic)
        User user = userRepository.findByIdOrThrow(log.getUserId());
        Book book = bookRepository.findById(log.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        return new LogResponse(
                savedLog.getId(),
                user.getUsername(),
                book.getTitle(),
                savedLog.getBorrowDate(),
                savedLog.getReturnDate(),
                savedLog.getStatus(),
                "Demo: Borrow date updated to " + newDate.toLocalDate()
        );
    }

    private void updateUserStatusAfterReturn(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 1. Check Manual Lock
        // English Comment: If the account was manually locked by an admin, skip automatic reactivation
        if (user.isManualLock()) {
            return;
        }

        // 2. Check Overdue Status
        // Check if the user still has any other overdue books
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(LibraryConstants.OVERDUE_DAYS);
        boolean hasOtherOverdue = borrowLogRepository.existsByUserIdAndStatusAndBorrowDateBefore(
                userId, LogStatus.BORROWED, thirtyDaysAgo);

        // 3. Check Borrowing Limit
        // Check if they are now below the borrowing limit
        long activeBorrowCount = borrowLogRepository.countByUserIdAndStatus(userId, LogStatus.BORROWED);

        // 4. Automatic Re-enable
        // If no more overdue books and under the limit, re-enable the account
        if (!hasOtherOverdue && activeBorrowCount < LibraryConstants.MAX_BORROW_LIMIT) {
            user.setEnabled(true);
            // English Comment: Ensure manualLock remains false when system re-enables the user
            user.setManualLock(false);
            userRepository.save(user);
        }
    }
}
