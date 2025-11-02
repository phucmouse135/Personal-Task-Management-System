package org.example.cv.models.responses;

import java.time.Instant;
import java.util.List;

import org.example.cv.constants.TaskPriority;
import org.example.cv.constants.TaskStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thông tin chi tiết của một Task")
public record TaskResponse(
        @Schema(example = "123") Long id,
        @Schema(example = "Thiết kế API") String title,
        @Schema(example = "Thiết kế các endpoint RESTful cho Task") String description,
        @Schema(example = "IN_PROGRESS") TaskStatus status,
        @Schema(example = "HIGH") TaskPriority priority,
        @Schema(example = "2025-11-30T18:00:00") Instant deadline,
        @Schema(example = "2025-10-25T14:30:00") Instant createdAt,
        ProjectSummaryResponse project,
        List<UserSummaryResponse> assignees,
        @Schema(description = "Task có bị trễ hạn hay không", example = "false") boolean overdue) {
    // Logic kiểm tra trễ hạn được thêm vào DTO
    public boolean isOverdue() {
        return deadline != null
                && status != TaskStatus.DONE
                && status != TaskStatus.CANCELLED
                && deadline.isBefore(Instant.now());
    }
}
