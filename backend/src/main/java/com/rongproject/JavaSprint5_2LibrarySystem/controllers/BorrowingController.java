package com.rongproject.JavaSprint5_2LibrarySystem.controllers;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.LogResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.UserStatusResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.BorrowLog;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.LogStatus;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.BorrowLogRepository;
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

/**
 * Library Project - Borrowing Controller
 * Jules Version 2.8 - Fixed PathMapping and Current User Context
 */
@RestController
@RequestMapping({"/api/borrowings", "/borrowings"})
@RequiredArgsConstructor
@Tag(name = "Borrowing Management", description = "The only entrance for library transactions")
public class BorrowingController {

    private final BorrowingService borrowingService;
    private final BorrowLogRepository borrowLogRepository;


    // 1. 借书
    @PostMapping("/borrow/{bookId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Borrow a book (MySQL Stock -1, MongoDB Log +1)")
    public ResponseEntity<LogResponse> borrowBook(
            @PathVariable Long bookId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return new ResponseEntity<>(borrowingService.borrowBook(userDetails.getId(), bookId), HttpStatus.CREATED);
    }

    // 2. 还书
    @PostMapping("/return/{bookId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Return a book (MySQL Stock +1, MongoDB Log Update)")
    public ResponseEntity<LogResponse> returnBook(
            @PathVariable Long bookId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(borrowingService.returnBook(userDetails.getId(), bookId));
    }

    /**
     * 3. 重要修复：获取当前登录用户的借阅记录
     * 解决 user/loans 无返回信息的问题
     * 前端调用路径: /api/borrowings/user/loans
     */
    @Operation(summary = "Get current user's borrowing history") // 建议保留，规范接口文档
    @GetMapping("/user/loans")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<LogResponse>> getUserLoans(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            System.out.println(">>> [JULES ERROR] UserDetails is NULL!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = userDetails.getId();
        System.out.println(">>> [JULES] Checking loans for userId: " + userId
                + " | username: " + userDetails.getUsername());

        // ── 新增：直接数有多少条符合条件的记录 ──
        long countDirect = borrowLogRepository.countByUserIdAndStatus(userId, LogStatus.BORROWED);
        System.out.println(">>> [JULES] countByUserIdAndStatus(BORROWED) = " + countDirect);

        List<BorrowLog> raw = borrowLogRepository.findByUserId(userId);
        System.out.println(">>> [JULES] findByUserId returned " + raw.size() + " raw documents");

        if (!raw.isEmpty()) {
            BorrowLog first = raw.get(0);
            System.out.println(">>> [JULES] First log → user_id type: " + first.getUserId().getClass().getSimpleName()
                    + " | value: " + first.getUserId());
        }

        List<LogResponse> response = borrowingService.getLogsByUserId(userId);
        System.out.println(">>> [JULES] Final response size: " + response.size());

        return ResponseEntity.ok(response);
    }

    // 4. 管理员查询：全量历史
    @GetMapping("/all-logs")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Admin: Get all borrowing history from MongoDB")
    public ResponseEntity<List<LogResponse>> getAllLogs() {
        return ResponseEntity.ok(borrowingService.getAllLogs());
    }

    // 5. 按用户名查询 (供管理员在搜索框使用)
    @GetMapping("/user-logs/{username}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #username == authentication.name")
    @Operation(summary = "Get logs by specific username")
    public ResponseEntity<List<LogResponse>> getLogsByUserName(@PathVariable String username) {
        return ResponseEntity.ok(borrowingService.getLogsByUserName(username));
    }

    // 6. 资格检查
    @GetMapping("/{userId}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #userId == principal.id")
    @Operation(summary = "Check if user is eligible to borrow")
    public ResponseEntity<UserStatusResponse> getStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(borrowingService.getUserBorrowingStatus(userId));
    }

    // 7. Demo 日期修改
    @PatchMapping("/{recordId}/date-demo")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Admin: Update date for testing overdue logic")
    public ResponseEntity<LogResponse> updateBorrowDateForDemo(
            @PathVariable String recordId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newBorrowDate) {
        return ResponseEntity.ok(borrowingService.updateBorrowDateForDemo(recordId, newBorrowDate));
    }

    /**
     * 8. 按用户ID查询 (保留但修正权限)
     * 路径: /api/borrowings/user/1
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #userId == principal.id")
    public ResponseEntity<List<LogResponse>> getUserLogs(@PathVariable Long userId) {
        return ResponseEntity.ok(borrowingService.getLogsByUserId(userId));
    }
}