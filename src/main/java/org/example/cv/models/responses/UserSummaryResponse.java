package org.example.cv.models.responses;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thông tin tóm tắt về người dùng")
public record UserSummaryResponse(
        @Schema(example = "10") Long id,
        @Schema(example = "johndoe") String username,
        @Schema(example = "John Doe") String fullName) {}
