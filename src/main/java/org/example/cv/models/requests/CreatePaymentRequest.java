package org.example.cv.models.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu tạo thanh toán cho dự án hoặc công việc")
public class CreatePaymentRequest {
    @NotNull(message = "ID dự án là bắt buộc")
    @Schema(description = "ID của dự án liên quan đến thanh toán", example = "123")
    private Long projectId;

    // private Long taskId; // Tùy chọn

    @NotNull
    @Min(value = 10000, message = "Số tiền tối thiểu là 10.000 VND")
    @Schema(description = "Số tiền thanh toán (VND)", example = "500000")
    private Long amount; // Số tiền (VND)
}
