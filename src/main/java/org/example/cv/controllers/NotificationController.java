package org.example.cv.controllers;

import org.example.cv.models.responses.ApiResponse;
import org.example.cv.models.responses.NotificationResponse;
import org.example.cv.models.responses.PageResponse;
import org.example.cv.services.NotificationService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Controller", description = "API cho quản lý Thông báo")
@Slf4j
public class NotificationController {
    private final NotificationService notificationService;

    /**
     * Lấy danh sách thông báo cho người dùng
     * @param userId
     * @param pageable
     * @return
     */
    @Operation(summary = "Lấy danh sách thông báo cho người dùng")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getNotificationsForUser(
            @PathVariable Long userId, Pageable pageable) {
        log.info("Lấy danh sách thông báo cho người dùng với ID: {}", userId);
        return ResponseEntity.ok(ApiResponse.<PageResponse<NotificationResponse>>builder()
                .code(200)
                .message("Lấy danh sách thông báo thành công")
                .result(notificationService.getNotificationsForUser(userId, pageable))
                .build());
    }

    /**
     * Đánh dấu một thông báo là đã đọc
     * @param notificationId
     * @param userId
     * @return
     */
    @Operation(summary = "Đánh dấu một thông báo là đã đọc")
    @PostMapping("/{notificationId}/read/user/{userId}")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long notificationId, @PathVariable Long userId) {
        log.info("Đánh dấu thông báo với ID: {} là đã đọc cho người dùng với ID: {}", notificationId, userId);
        notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Đánh dấu thông báo là đã đọc thành công")
                .build());
    }

    /**
     * Đánh dấu tất cả thông báo của người dùng là đã đọc
     * @param userId
     * @return
     */
    @Operation(summary = "Đánh dấu tất cả thông báo của người dùng là đã đọc")
    @PostMapping("/read-all/user/{userId}")
    public void markAllAsReadForUser(@PathVariable Long userId) {
        log.info("Đánh dấu tất cả thông báo là đã đọc cho người dùng với ID: {}", userId);
        notificationService.markAllAsReadForUser(userId);
    }
}
