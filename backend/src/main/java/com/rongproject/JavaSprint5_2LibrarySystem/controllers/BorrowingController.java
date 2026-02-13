package com.rongproject.JavaSprint5_2LibrarySystem.controllers;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.LogResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserStatusResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.security.CustomUserDetails;
import com.rongproject.JavaSprint5_2LibrarySystem.services.BorrowingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping({"/api/borrowings", "/borrowings"})
@RequiredArgsConstructor
@Tag(name = "Borrowing Management", description = "The only entrance for library transactions")
public class BorrowingController {

    private final BorrowingService borrowingService;

    // 1. 借书：调用 BorrowingService (包含 MySQL 扣库存 和 MongoDB 记日志)
    @PostMapping("/borrow/{bookId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<LogResponse> borrowBook(
            @PathVariable Long bookId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return new ResponseEntity<>(borrowingService.borrowBook(userDetails.getId(), bookId), HttpStatus.CREATED);
    }

    // 2. 还书：调用 BorrowingService (包含 MySQL 加库存 和 MongoDB 更新日志)
    @PostMapping("/return/{bookId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<LogResponse> returnBook(
            @PathVariable Long bookId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(borrowingService.returnBook(userDetails.getId(), bookId));
    }

    // 3. 管理员查询：调用 BorrowLogService 获取 MongoDB 的全量历史
    @GetMapping("/all-logs")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Admin: Get all borrowing history from MongoDB")
    public ResponseEntity<List<LogResponse>> getAllLogs() {
        return ResponseEntity.ok(borrowingService.getAllLogs());
    }

    // 4. 用户/管理员查询：按用户名获取日志
    @GetMapping("/user-logs/{username}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #username == authentication.name")
    public ResponseEntity<List<LogResponse>> getLogsByUserName(@PathVariable String username) {
        return ResponseEntity.ok(borrowingService.getLogsByUserName(username));
    }

    // 5. 资格检查
    @GetMapping("/{userId}/status")
    public ResponseEntity<UserStatusResponse> getStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(borrowingService.getUserBorrowingStatus(userId));
    }

    // 6. Demo 日期修改：调用 BorrowingService 的 demo 方法
    @PatchMapping("/{recordId}/date-demo")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<LogResponse> updateBorrowDateForDemo(
            @PathVariable String recordId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newBorrowDate) {
        return ResponseEntity.ok(borrowingService.updateBorrowDateForDemo(recordId, newBorrowDate));
    }

    // 在 BorrowingController.java 中
    @GetMapping("/user/{userId}")  // 这样完整路径就是 /api/borrowings/user/1
    public ResponseEntity<List<LogResponse>> getUserLogs(@PathVariable Long userId) {
        return ResponseEntity.ok(borrowingService.getLogsByUserId(userId));
    }
}