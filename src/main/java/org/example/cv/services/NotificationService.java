package org.example.cv.services;

import org.example.cv.event.PaymentSuccessEvent;
import org.example.cv.event.TaskEvent;
import org.example.cv.models.responses.NotificationResponse;
import org.example.cv.models.responses.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface NotificationService {
    void handlePaymentSuccessEvent(PaymentSuccessEvent paymentSuccessEvent);

    void handleTaskEvent(TaskEvent event);

    PageResponse<NotificationResponse> getNotificationsForUser(Long userId, Pageable pageable);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsReadForUser(Long userId);
}
