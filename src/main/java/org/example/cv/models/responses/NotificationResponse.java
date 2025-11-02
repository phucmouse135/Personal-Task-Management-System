package org.example.cv.models.responses;

import java.time.Instant;

import org.example.cv.constants.NotificationType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Thông tin chi tiết của một thông báo")
public class NotificationResponse {
    @Schema(example = "123")
    private Long id;

    @Schema(example = "45")
    private Long taskId; // Chỉ cần ID để client tự điều hướng

    @Schema(example = "TASK_ASSIGNED")
    private NotificationType type;

    @Schema(example = "Admin đã gán cho bạn task mới: 'Thiết kế API'")
    private String message;

    @Schema(example = "false")
    private boolean isRead;

    @Schema(example = "2025-10-27T10:00:00Z")
    private Instant createdAt;

    @Schema(description = "Thông tin tóm tắt về người thực hiện (actor)")
    private UserSummaryResponse actor;
}
