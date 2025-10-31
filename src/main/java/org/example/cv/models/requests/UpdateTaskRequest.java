package org.example.cv.models.requests; // UpdateTaskRequest.java

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.example.cv.constants.TaskPriority;
import org.example.cv.constants.TaskStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload để cập nhật một task (PUT - thay thế toàn bộ)")
public record UpdateTaskRequest(
        @Schema(description = "Tiêu đề của task", example = "Cập nhật API") @NotBlank @Size(max = 255) String title,
        @Schema(description = "Mô tả chi tiết", example = "Cập nhật các endpoint") String description,
        @Schema(description = "Trạng thái mới", example = "IN_PROGRESS") @NotNull TaskStatus status,
        @Schema(description = "Độ ưu tiên mới", example = "HIGH") @NotNull TaskPriority priority,
        @Schema(description = "Hạn chót mới", example = "2025-12-01T18:00:00")
                @Future(message = "Deadline phải ở tương lai")
                Instant deadline,
        @Schema(description = "ID của nhung người được gán mới", example = "[1,2]") @NotNull List<Long> assignees
        // Lưu ý: Thường thì projectId không được phép thay đổi khi update
        ) {}
