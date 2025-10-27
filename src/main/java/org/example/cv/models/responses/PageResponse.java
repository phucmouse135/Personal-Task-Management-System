package org.example.cv.models.responses;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Kết quả trả về có phân trang")
public record PagedResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages, boolean last) {}
