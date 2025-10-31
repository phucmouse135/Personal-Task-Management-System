package org.example.cv.configuration;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Interceptor này chạy đầu tiên, chịu trách nhiệm xác thực JWT
 * được gửi trong header của frame CONNECT (STOMP).
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder; // Bean đã có từ SecurityConfig
    private final JwtAuthenticationConverter jwtAuthConverter; // Converter tùy chỉnh của bạn

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        // Lấy STOMP accessor để đọc header
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Chỉ kiểm tra frame CONNECT
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            // Lấy header 'Authorization' (giống hệt Postman/HTTP)
            final String authHeader = accessor.getFirstNativeHeader("Authorization");
            log.debug("WebSocket Auth: Đang kiểm tra header Authorization...");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                final String token = authHeader.substring(7); // Bỏ "Bearer "

                try {
                    // 1. Xác thực và giải mã JWT
                    final Jwt jwt = jwtDecoder.decode(token);

                    // 2. Chuyển đổi JWT thành Authentication (chứa UserPrincipal)
                    // Đây là logic Onboarding (JIT Provisioning) của bạn
                    Authentication authentication = jwtAuthConverter.convert(jwt);

                    if (authentication == null || !authentication.isAuthenticated()) {
                        throw new AccessDeniedException("Xác thực thất bại.");
                    }

                    // 3. Gán Principal (chứa UserPrincipal) vào session WebSocket
                    // Kể từ đây, mọi MessageMapping đều có thể @AuthenticationPrincipal
                    accessor.setUser(authentication);

                } catch (Exception e) {
                    log.warn("WebSocket Auth: Xác thực JWT thất bại: {}", e.getMessage());
                    // Ném lỗi để từ chối kết nối
                    throw new AccessDeniedException("Token không hợp lệ hoặc đã hết hạn.");
                }
            } else {
                log.warn("WebSocket Auth: Thiếu header Authorization trong frame CONNECT.");
                throw new AccessDeniedException("Yêu cầu header Authorization.");
            }
        }

        // Cho các frame khác (SUBSCRIBE, SEND...) đi qua
        // Chúng sẽ được kiểm tra bằng Principal đã gán ở trên
        return message;
    }
}
