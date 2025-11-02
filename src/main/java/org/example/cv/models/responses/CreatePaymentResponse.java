package org.example.cv.models.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Schema(description = "Phản hồi sau khi tạo yêu cầu thanh toán")
public class CreatePaymentResponse {
    @Schema(description = "Trạng thái của yêu cầu thanh toán", example = "SUCCESS")
    private String status;

    @Schema(description = "Thông điệp phản hồi", example = "Yêu cầu thanh toán đã được tạo thành công.")
    private String message;

    @Schema(description = "URL để thực hiện thanh toán", example = "https://pay.vnpay.vn/...")
    private String paymentUrl;
}
