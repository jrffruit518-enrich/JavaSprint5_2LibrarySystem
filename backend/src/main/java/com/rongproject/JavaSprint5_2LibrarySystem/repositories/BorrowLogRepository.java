package com.rongproject.JavaSprint5_2LibrarySystem.repositories;

import com.rongproject.JavaSprint5_2LibrarySystem.entities.BorrowLog;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.LogStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Library Project - Borrowing Log Repository (MongoDB)
 * Jules Version 2.9 - Combined with explicit field mapping for Long IDs.
 */
@Repository
public interface BorrowLogRepository extends MongoRepository<BorrowLog, String> {

    // 1. 核心修复：显式指向 user_id 字段，解决 User Loans 无信息的问题
    @Query("{ 'user_id' : ?0 }")
    List<BorrowLog> findByUserId(Long userId);

    // 2. 状态组合查询：用于获取“正在借阅中”的记录 (BORROWED, OVERDUE)
    @Query("{ 'user_id' : ?0, 'status' : { $in: ?1 } }")
    List<BorrowLog> findByUserIdAndStatusIn(Long userId, List<LogStatus> statuses);

    // 3. 基础状态查询：用于获取“还书历史” (RETURNED)
    @Query("{ 'user_id' : ?0, 'status' : ?1 }")
    List<BorrowLog> findByUserIdAndStatus(Long userId, LogStatus status);

    // 4. 还书逻辑专用：找到最新的一条借书记录
    Optional<BorrowLog> findFirstByUserIdAndBookIdAndStatusOrderByBorrowDateDesc(
            Long userId,
            Long bookId,
            LogStatus status
    );

    // 5. 逾期检查
    boolean existsByUserIdAndStatusAndBorrowDateBefore(Long userId, LogStatus status, LocalDateTime date);

    // 6. 计数统计：当前借阅了几本书
    long countByUserIdAndStatus(Long userId, LogStatus status);

    // 7. 存在性检查
    boolean existsByUserIdAndStatus(Long userId, LogStatus logStatus);

    // 8. 图书锁定检查：该书是否已被借出
    boolean existsByBookIdAndStatus(Long bookId, LogStatus status);

    // 9. 精准匹配（可选）
    Optional<BorrowLog> findByUserIdAndBookIdAndStatus(Long userId, Long bookId, LogStatus status);
}