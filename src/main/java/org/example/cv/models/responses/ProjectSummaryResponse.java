package org.example.cv.models.responses;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thông tin tóm tắt về dự án")
public record ProjectSummaryResponse(
        @Schema(example = "1") Long id, @Schema(example = "Task Manager App") String name) {}
