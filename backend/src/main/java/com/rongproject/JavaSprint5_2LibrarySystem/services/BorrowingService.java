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

@Service
@RequiredArgsConstructor
public class BorrowingService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowLogRepository borrowLogRepository;

    @Transactional
    public LogResponse borrowBook(Long userId, Long bookId) {
        User user = userRepository.findByIdOrThrow(userId);
        // 1. First, check if the user is allowed to borrow
        UserStatusResponse status = getUserBorrowingStatus(userId);
        if (!status.canBorrow()) {
            throw new IllegalStateException("User is not eligible to borrow.");
        }

        // 2. Then, check book existence and stock
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        if (book.getAvailableStock() <= 0) {
            throw new IllegalStateException("Book is currently out of stock.");
        }

        // 3. MySQL: Update stock
        book.setAvailableStock(book.getAvailableStock() - 1);
        bookRepository.save(book);

        // 4. MongoDB: Create and save the borrow log
        BorrowLog log = BorrowLog.builder()
                .userId(userId)
                .bookId(bookId)
                .borrowDate(LocalDateTime.now())
                .status(LogStatus.BORROWED)
                .build();

        BorrowLog savedLog = borrowLogRepository.save(log);

        // 5. 组装最后一句提示语
        // 注意：dueDate.toLocalDate() 可以让日期显示更整洁 (只显示年月日)
        LocalDateTime dueDate = LocalDateTime.now().plusDays(LibraryConstants.OVERDUE_DAYS);
        String welcomeMessage = String.format("Success! %s, you have borrowed \"%s\". Please return it by %s.",
                user.getUsername(),
                book.getTitle(),
                dueDate.toLocalDate());

        return new LogResponse(
                savedLog.getId(),
                userRepository.findById(userId).map(User::getUsername).orElse("Unknown"),
                book.getTitle(),
                savedLog.getBorrowDate(),
                savedLog.getReturnDate(),
                savedLog.getStatus(),
                "Book borrowed successfully."
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

        // Check MongoDB for overdue and count
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(LibraryConstants.OVERDUE_DAYS);
        boolean hasOverdue = borrowLogRepository.existsByUserIdAndStatusAndBorrowDateBefore(
                userId, LogStatus.BORROWED, thirtyDaysAgo);
        long activeBorrowCount = borrowLogRepository.countByUserIdAndStatus(userId, LogStatus.BORROWED);

        // Logic A: Automatically update status if they were blocked but now cleared
        // (This ensures the button turns blue again after returning books)
        if (!user.isEnabled() && !hasOverdue && activeBorrowCount < LibraryConstants.MAX_BORROW_LIMIT) {
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

    private void updateUserStatusAfterReturn(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if the user still has any other overdue books
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(LibraryConstants.OVERDUE_DAYS);
        boolean hasOtherOverdue = borrowLogRepository.existsByUserIdAndStatusAndBorrowDateBefore(
                userId, LogStatus.BORROWED, thirtyDaysAgo);

        // Check if they are now below the borrowing limit
        long activeBorrowCount = borrowLogRepository.countByUserIdAndStatus(userId, LogStatus.BORROWED);

        // If no more overdue books and under the limit, re-enable the account
        if (!hasOtherOverdue && activeBorrowCount < LibraryConstants.MAX_BORROW_LIMIT) {
            user.setEnabled(true);
            userRepository.save(user);
        }
    }
}
