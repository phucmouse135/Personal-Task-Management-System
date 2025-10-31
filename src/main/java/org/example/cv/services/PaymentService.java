package org.example.cv.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cv.configuration.VNPAYConfig;
import org.example.cv.constants.PaymentStatus;
import org.example.cv.event.PaymentSuccessEvent;
import org.example.cv.models.entities.PaymentEntity;
import org.example.cv.models.entities.ProjectEntity;
import org.example.cv.models.entities.UserEntity;
import org.example.cv.models.requests.CreatePaymentRequest;
import org.example.cv.models.responses.CreatePaymentResponse;
import org.example.cv.repositories.PaymentRepository;
import org.example.cv.repositories.ProjectRepository;
import org.example.cv.repositories.UserRepository;
import org.example.cv.utils.AuthenticationUtils;
import org.example.cv.utils.VNPAYUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final VNPAYConfig vnpayConfig;
    private final PaymentRepository paymentRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request, HttpServletRequest httpServletRequest) {

        ProjectEntity project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy dự án"));

        UserEntity user = userRepository.findById(Objects.requireNonNull(AuthenticationUtils.getCurrentUserId()))
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));

        // TODO: Security Check
        // if (!project.getOwner().getId().equals(user.getId())) {
        //     throw new AccessDeniedException("Bạn không có quyền thanh toán cho dự án này");
        // }

        // 1. Tạo giao dịch PENDING trong DB
        PaymentEntity payment = PaymentEntity.builder()
                .user(user)
                .project(project)
                .amount(request.getAmount())
                .status(PaymentStatus.PENDING)
                .build();
        paymentRepository.save(payment);

        // 2. Chuẩn bị dữ liệu gửi sang VNPAY
        String vnp_TxnRef = String.valueOf(payment.getId()); // Mã giao dịch của hệ thống
        String vnp_IpAddr = VNPAYUtil.getIpAddress(httpServletRequest);
        String vnp_TmnCode = vnpayConfig.getTmnCode();

        // VNPAY yêu cầu amount * 100
        long amountInVND = request.getAmount() * 100;

        Map<String, String> vnp_Params = new java.util.HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amountInVND));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan cho du an " + project.getId());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnpayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_CreateDate", VNPAYUtil.getCreateDate());

        // 3. Tạo chữ ký
        String hashData = VNPAYUtil.hashAllFields(vnp_Params, vnpayConfig.getHashSecret());
        vnp_Params.put("vnp_SecureHash", hashData);

        // 4. Xây dựng URL
        String paymentUrl = VNPAYUtil.buildPaymentUrl(vnpayConfig.getApiUrl(), vnp_Params);

        return new CreatePaymentResponse("OK", "Tạo thanh toán thành công", paymentUrl);
    }

    @Transactional
    public String handleCallback(HttpServletRequest request) {
        Map<String, String> params = VNPAYUtil.getParamsFromRequest(request);

        String vnp_SecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash"); // Xóa hash khỏi map trước khi xác thực

        // 1. Xác thực chữ ký
        String calculatedHash = VNPAYUtil.hashAllFields(params, vnpayConfig.getHashSecret());
        if (!calculatedHash.equals(vnp_SecureHash)) {
            log.warn("VNPAY Callback: Chữ ký không hợp lệ!");
            return vnpayConfig.getFrontendFailedUrl(); // Redirect về trang thất bại
        }

        String vnp_TxnRef = params.get("vnp_TxnRef"); // Đây là paymentId của chúng ta
        String vnp_ResponseCode = params.get("vnp_ResponseCode");
        String vnp_TransactionNo = params.get("vnp_TransactionNo");
        String vnp_BankCode = params.get("vnp_BankCode");
        String vnp_PayDate = params.get("vnp_PayDate");

        try {
            PaymentEntity payment = paymentRepository.findById(Long.valueOf(vnp_TxnRef))
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy giao dịch"));

            // 2. Kiểm tra giao dịch đã được xử lý chưa
            if (payment.getStatus() != PaymentStatus.PENDING) {
                log.warn("VNPAY Callback: Giao dịch {} đã được xử lý.", vnp_TxnRef);
                return (payment.getStatus() == PaymentStatus.COMPLETED) ?
                        vnpayConfig.getFrontendSuccessUrl() :
                        vnpayConfig.getFrontendFailedUrl();
            }

            // 3. Cập nhật trạng thái giao dịch
            if ("00".equals(vnp_ResponseCode)) {
                // Thành công
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setVnpTransactionNo(vnp_TransactionNo);
                payment.setVnpBankCode(vnp_BankCode);
                payment.setVnpPayDate(vnp_PayDate);
                paymentRepository.save(payment);

                // Bắn sự kiện để gửi thông báo, cộng tiền, v.v.
                eventPublisher.publishEvent(new PaymentSuccessEvent(this, payment));

                log.info("VNPAY Callback: Giao dịch {} thành công.", vnp_TxnRef);
                return vnpayConfig.getFrontendSuccessUrl();
            } else {
                // Thất bại
                payment.setStatus(PaymentStatus.FAILED);
                payment.setVnpTransactionNo(vnp_TransactionNo); // Vẫn lưu mã lỗi
                paymentRepository.save(payment);

                log.warn("VNPAY Callback: Giao dịch {} thất bại, Mã lỗi: {}", vnp_TxnRef, vnp_ResponseCode);
                return vnpayConfig.getFrontendFailedUrl();
            }

        } catch (Exception e) {
            log.error("VNPAY Callback: Lỗi xử lý callback: {}", e.getMessage());
            return vnpayConfig.getFrontendFailedUrl();
        }
    }
}
