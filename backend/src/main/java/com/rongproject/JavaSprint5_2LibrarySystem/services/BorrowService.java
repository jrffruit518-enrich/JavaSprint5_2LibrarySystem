package com.rongproject.JavaSprint5_2LibrarySystem.services;

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
public class BorrowService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowLogRepository borrowLogRepository;

    @Transactional
    public BorrowLog borrowBook(Long userId, Long bookId) {
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

        return borrowLogRepository.save(log);
    }

    @Transactional
    public void returnBook(Long userId, Long bookId) {
        // 1. Find the active borrow log in MongoDB
        // We look for the most recent borrowed record for this specific user and book
        BorrowLog log = borrowLogRepository.findFirstByUserIdAndBookIdAndStatusOrderByBorrowDateDesc(
                        userId, bookId, LogStatus.BORROWED)
                .orElseThrow(() -> new ResourceNotFoundException("No active borrowing record found."));

        // 2. Update MongoDB: Mark as returned
        log.setStatus(LogStatus.RETURNED);
        log.setReturnDate(LocalDateTime.now());
        borrowLogRepository.save(log);

        // 3. Update MySQL: Increment book stock
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        book.setAvailableStock(book.getAvailableStock() + 1);
        bookRepository.save(book);

        // 4. Logic A: Automatically re-enable user if they are now compliant
        updateUserStatusAfterReturn(userId);
    }

    public UserStatusResponse getUserBorrowingStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

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
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        boolean hasOtherOverdue = borrowLogRepository.existsByUserIdAndStatusAndBorrowDateBefore(
                userId, LogStatus.BORROWED, thirtyDaysAgo);

        // Check if they are now below the borrowing limit (e.g., < 10)
        long activeBorrowCount = borrowLogRepository.countByUserIdAndStatus(userId, LogStatus.BORROWED);

        // If no more overdue books and under the limit, re-enable the account
        if (!hasOtherOverdue && activeBorrowCount < 10) {
            user.setEnabled(true);
            userRepository.save(user);
        }
    }
}
