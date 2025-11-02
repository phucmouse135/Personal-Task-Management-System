package org.example.cv.constants;

public enum NotificationType {
    TASK_ASSIGNED, // Được gán task mới
    TASK_UPDATED, // Task được cập nhật (trạng thái, deadline...)
    TASK_COMPLETED, // Task đã hoàn thành
    TASK_COMMENT_ADDED, // Có bình luận mới (cho tương lai)
    TASK_OVERDUE, // Task bị trễ hạn (sẽ cần 1 scheduled job để quét)
    PAYMENT_SUCCESS // Thanh toán thành công
}
