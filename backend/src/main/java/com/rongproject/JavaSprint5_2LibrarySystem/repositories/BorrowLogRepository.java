package com.rongproject.JavaSprint5_2LibrarySystem.repositories;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.LogResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.BorrowLog;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.LogStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BorrowLogRepository extends MongoRepository<BorrowLog,String> {
    // You can add custom query methods here if needed
    Optional<BorrowLog> findFirstByUserIdAndBookIdAndStatusOrderByBorrowDateDesc(
            Long userId,
            Long bookId,
            LogStatus status
    );
    // Check if any book borrowed before a certain date is still not returned
    boolean existsByUserIdAndStatusAndBorrowDateBefore(Long userId, LogStatus status, LocalDateTime date);

    // Count currently borrowed books
    long countByUserIdAndStatus(Long userId, LogStatus status);

    Optional<BorrowLog> findByUserIdAndBookIdAndStatus(Long userId, Long bookId, LogStatus status);


    List<BorrowLog> findByUserId(Long id);

    boolean existsByUserIdAndStatus(Long id,LogStatus logStatus);

    // English Comment: Check if a specific book is currently out on loan
    boolean existsByBookIdAndStatus(Long bookId, LogStatus status);
}
