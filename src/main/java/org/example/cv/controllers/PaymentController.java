package org.example.cv.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.example.cv.models.entities.PaymentEntity;
import org.example.cv.models.requests.CreatePaymentRequest;
import org.example.cv.models.responses.ApiResponse;
import org.example.cv.models.responses.CreatePaymentResponse;
import org.example.cv.services.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Get all payments with pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PaymentEntity>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PaymentEntity> payments = paymentService.getAllPayments(pageable, status);

        return ResponseEntity.ok(ApiResponse.<Page<PaymentEntity>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách thanh toán thành công")
                .result(payments)
                .build());
    }

    /**
     * Endpoint để tạo yêu cầu thanh toán.
     * Trả về URL của VNPAY để redirect.
     */
    @PostMapping(value = "/create", produces = "application/json")
    //    @PreAuthorize("isAuthenticated()") // Yêu cầu đăng nhập
    public ResponseEntity<ApiResponse<CreatePaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request, HttpServletRequest httpServletRequest) {
        CreatePaymentResponse response = paymentService.createPayment(request, httpServletRequest);
        return ResponseEntity.ok(ApiResponse.<CreatePaymentResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Yêu cầu thanh toán đã được tạo thành công.")
                .result(response)
                .build());
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
