package org.example.cv.configuration;

import lombok.RequiredArgsConstructor;
import org.example.cv.services.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final AuditLogInterceptor auditLogInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(auditLogInterceptor)
                .addPathPatterns("/projects/**")
                .addPathPatterns("/api/v1/tasks/**");// Áp dụng cho tất cả các đường dẫn
    }
}
