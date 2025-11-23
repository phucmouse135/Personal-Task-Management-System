package org.example.cv.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.example.cv.event.AuditLogEvent;
import org.example.cv.utils.AuthenticationUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.util.annotation.NonNullApi;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLogInterceptor implements HandlerInterceptor {
    private final ApplicationEventPublisher publisher;

    @Override
    public boolean preHandle(HttpServletRequest request,  HttpServletResponse response, Object handler)
            throws Exception {
        log.info("AuditLogInterceptor: preHandle called for URI: {}", request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        log.info("AuditLogInterceptor: afterCompletion called for URI: {}", request.getRequestURI());
        String entityType = (String) request.getAttribute("entityType");
        Long entityId = (Long) request.getAttribute("entityId");
        String action = (String) request.getAttribute("action");
        String details = (String) request.getAttribute("details");
        if (entityType == null || entityId == null || action == null) {
            log.debug("No audit data found for URI {}", request.getRequestURI());
            return;
        }

        Long actorId = AuthenticationUtils.getCurrentUserId();

        // ✅ Phát event bất đồng bộ
        publisher.publishEvent(new AuditLogEvent(this, entityType, entityId, action, details, actorId));
        log.info("📤 AuditLogEvent published for {} {}", action, entityType);
    }
}
