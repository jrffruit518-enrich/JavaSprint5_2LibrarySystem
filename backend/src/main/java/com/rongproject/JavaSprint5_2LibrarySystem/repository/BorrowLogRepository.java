package com.rongproject.JavaSprint5_2LibrarySystem.repository;

import com.rongproject.JavaSprint5_2LibrarySystem.Entity.BorrowLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BorrowLogRepository extends MongoRepository<BorrowLog,String> {
    // You can add custom query methods here if needed
    BorrowLog findByUserId(Long userId);
}
