package org.example.cv.models.requests; // TaskFilterRequest.java

import java.time.Instant;
import java.util.Set;

import org.example.cv.constants.TaskPriority;
import org.example.cv.constants.TaskStatus;
import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Các tham số để lọc danh sách task")
public record TaskFilterRequest(
        @Schema(description = "Lọc theo ID dự án", example = "1") Long projectId,
        @Schema(description = "Lọc theo ID người được gán", example = "10") Long assigneeId,
        @Schema(description = "Lọc theo trạng thái (có thể nhiều)", example = "TODO,IN_PROGRESS")
                Set<TaskStatus> statuses,
        @Schema(description = "Lọc theo độ ưu tiên", example = "HIGH,URGENT") Set<TaskPriority> priorities,
        @Schema(description = "Lọc task có hạn chót từ ngày", example = "2025-11-01")
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                Instant deadlineFrom,
        @Schema(description = "Lọc task có hạn chót đến ngày", example = "2025-11-30")
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                Instant deadlineTo) {}
