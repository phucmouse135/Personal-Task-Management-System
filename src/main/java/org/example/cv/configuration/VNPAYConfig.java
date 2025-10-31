package org.example.cv.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payment.vnpay")
@Data
public class VNPAYConfig {
    // Lấy từ tài khoản VNPAY Sandbox
    private String tmnCode;
    private String hashSecret;

    // URL của VNPAY Sandbox
    private String apiUrl;


    private String returnUrl;

    private String frontendSuccessUrl;
    private String frontendFailedUrl;
}
