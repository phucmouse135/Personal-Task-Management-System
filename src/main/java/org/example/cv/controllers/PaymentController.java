package org.example.cv.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.cv.models.requests.CreatePaymentRequest;
import org.example.cv.models.responses.ApiResponse;
import org.example.cv.models.responses.CreatePaymentResponse;
import org.example.cv.services.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Endpoint để tạo yêu cầu thanh toán.
     * Trả về URL của VNPAY để redirect.
     */
    @PostMapping(value = "/create" , produces = "application/json")
//    @PreAuthorize("isAuthenticated()") // Yêu cầu đăng nhập
    public ResponseEntity<ApiResponse<CreatePaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpServletRequest
    ) {
        CreatePaymentResponse response = paymentService.createPayment(request, httpServletRequest);
        return ResponseEntity.ok(
                ApiResponse.<CreatePaymentResponse>builder()
                        .code(HttpStatus.CREATED.value())
                        .message("Yêu cầu thanh toán đã được tạo thành công.")
                        .result(response)
                        .build()
        );
    }

    /**
     * Endpoint VNPAY gọi về (Return URL).
     * Xử lý kết quả và redirect về Frontend.
     */
    @GetMapping("/callback")
    public RedirectView handleCallback(HttpServletRequest request) {
        String redirectUrl = paymentService.handleCallback(request);
        // Chuyển hướng trình duyệt của user về trang Frontend
        return new RedirectView(redirectUrl);
    }
}
