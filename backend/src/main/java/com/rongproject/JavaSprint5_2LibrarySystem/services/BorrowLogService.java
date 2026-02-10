package com.rongproject.JavaSprint5_2LibrarySystem.services;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.LogResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.Book;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.BorrowLog;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.User;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.LogStatus;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.BookRepository;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.BorrowLogRepository;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowLogService {
    private final BorrowLogRepository borrowLogRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    /**
     * Records a new borrowing transaction.
     */
    @Transactional
    public LogResponse borrowBook(Long userId, Long bookId) {
        // 1. Validate User
        User user = userRepository.findByIdOrThrow(userId);
        if (!user.isEnabled()) {
            throw new RuntimeException("User account is disabled and cannot borrow books.");
        }

        // 2. Validate Book and Stock
        Book book = bookRepository.getBookById(bookId);
        if (book.getAvailableStock() <= 0) {
            throw new RuntimeException("Book is currently out of stock.");
        }

        // 3. Check if user already has an active borrow for this specific book
        boolean alreadyBorrowed = borrowLogRepository.findByUserIdAndBookIdAndStatus(
                userId, bookId, LogStatus.BORROWED).isPresent();
        if (alreadyBorrowed) {
            throw new RuntimeException("User has already borrowed this book and not returned it yet.");
        }

        // 4. Update Stock
        book.setAvailableStock(book.getAvailableStock() - 1);
        bookRepository.save(book);

        // 5. Create Log
        BorrowLog log = BorrowLog.builder()
                .userId(userId)
                .bookId(bookId)
                .borrowDate(LocalDateTime.now())
                .status(LogStatus.BORROWED)
                .build();

        BorrowLog savedLog = borrowLogRepository.save(log);
        return mapToResponse(savedLog);
    }


    @Transactional
    public LogResponse returnBook(Long userId, Long bookId) {
        // 1. Find the active log
        BorrowLog log = borrowLogRepository.findByUserIdAndBookIdAndStatus(userId, bookId, LogStatus.BORROWED)
                .orElseThrow(() -> new RuntimeException("No active borrowing record found for this book and user."));

        // 2. Update Log
        log.setStatus(LogStatus.RETURNED);
        log.setReturnDate(LocalDateTime.now());
        BorrowLog savedLog = borrowLogRepository.save(log);

        // 3. Update Book Stock
        Book book = bookRepository.getBookById(bookId);
        book.setAvailableStock(book.getAvailableStock() + 1);
        bookRepository.save(book);

        return mapToResponse(savedLog);
    }

    public List<LogResponse> getAllLogs() {
        return borrowLogRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<LogResponse> getLogsByUserName(String name) {
        User user = userRepository.findByUsername(name)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + name));

        // Use a repository method instead of filtering in memory
        return borrowLogRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public LogResponse updateBorrowDateForDemo(String recordId, LocalDateTime newBorrowDate) {
        BorrowLog record = borrowLogRepository.findByIdOrThrow(recordId);
        record.setBorrowDate(newBorrowDate);

        // 如果你的逻辑是根据日期动态判断，这里修改后，下一次查询就会触发禁用逻辑
        return mapToResponse(borrowLogRepository.save(record));
    }

    private LogResponse mapToResponse(BorrowLog log) {
        User user = userRepository.findByIdOrThrow(log.getUserId());
        Book book = bookRepository.getBookById(log.getBookId());

        String message;
        if (log.getStatus() == LogStatus.BORROWED) {
            message = String.format("%s，你已成功借阅了《%s》，请按时归还。",
                    user.getUsername(), book.getTitle());
        } else {
            message = String.format("%s，你已成功归还了《%s》，谢谢。",
                    user.getUsername(), book.getTitle());
        }

        return new LogResponse(
                log.getId(),
                user.getUsername(),
                book.getTitle(),
                log.getBorrowDate(),
                log.getReturnDate(),
                log.getStatus(),
                message
        );
    }


}
