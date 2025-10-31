// CreateTaskRequest.java
package org.example.cv.models.requests;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.example.cv.constants.TaskPriority;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload để tạo một task mới")
public record CreateTaskRequest(
        @Schema(description = "Tiêu đề của task", example = "Thiết kế API") @NotBlank @Size(max = 255) String title,
        @Schema(description = "Mô tả chi tiết", example = "Thiết kế các endpoint RESTful cho Task") String description,
        @Schema(description = "Độ ưu tiên", example = "HIGH")
                TaskPriority priority, // Có thể null, service sẽ set default
        @Schema(description = "Hạn chót", example = "2025-11-30T18:00:00")
                @Future(message = "Deadline phải ở tương lai")
                Instant deadline,
        @Schema(description = "ID của dự án", example = "1") @NotNull Long projectId,
        @Schema(description = "ID của những người được gán", example = "[ 1,2]") @NotNull List<Long> assignees) {}
