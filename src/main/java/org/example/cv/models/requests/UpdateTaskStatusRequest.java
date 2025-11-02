package org.example.cv.models.requests;

import jakarta.validation.constraints.NotNull;

import org.example.cv.constants.TaskStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload để cập nhật trạng thái của task (PATCH - chỉ status)")
public record UpdateTaskStatusRequest(
        @Schema(description = "Trạng thái mới của task", example = "IN_PROGRESS")
                @NotNull(message = "Status không được để trống")
                TaskStatus status) {}
