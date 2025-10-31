package org.example.cv.services.impl;

import jakarta.persistence.EntityListeners;
import org.example.cv.constants.NotificationType;
import org.example.cv.event.AuditLogEvent;
import org.example.cv.event.PaymentSuccessEvent;
import org.example.cv.event.TaskEvent;
import org.example.cv.exceptions.AppException;
import org.example.cv.exceptions.ErrorCode;
import org.example.cv.models.entities.NotificationEntity;
import org.example.cv.models.entities.TaskEntity;
import org.example.cv.models.entities.UserEntity;
import org.example.cv.models.responses.NotificationResponse;
import org.example.cv.models.responses.PageResponse;
import org.example.cv.repositories.NotificationRepository;
import org.example.cv.services.NotificationService;
import org.example.cv.utils.mapper.NotificationMapper;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;

    /**
     * Lắng nghe sự kiện TaskEvent (SAU KHI transaction commit thành công)
     * và xử lý một cách bất đồng bộ.
     */
    @Async
    @EventListener(TaskEvent.class)
    @TransactionalEventListener // Chỉ chạy nếu transaction của TaskService (POST/PUT) thành
    public void handleTaskEvent(TaskEvent event) {
        log.info(
                "Nhận TaskEvent: {} cho task ID {}",
                event.getType(),
                event.getTask().getId());

        TaskEntity task = event.getTask();
        UserEntity actor = event.getActor();
        UserEntity recipient = event.getAssignee();

        String message = buildMessage(task, actor, event.getType());

        NotificationEntity notification = NotificationEntity.builder()
                .recipient(recipient)
                .actor(actor)
                .task(task)
                .type(event.getType())
                .message(message)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Đã tạo thông báo cho user {}", recipient.getId());

        // TODO: Gửi real-time notification qua WebSocket
        NotificationResponse response = notificationMapper.toResponse(notification);
        String destination = "user/" + recipient.getId() + "/queue/notifications";
        simpMessagingTemplate.convertAndSend(destination, response);
        log.info("Đã gửi real-time notification đến {}", destination);
    }

    @Async
    @TransactionalEventListener
    @EventListener(PaymentSuccessEvent.class)
    public void handlePaymentSuccessEvent(PaymentSuccessEvent event){
        log.info( "Nhận PaymentSuccessEvent cho user ID {}" ,event.getPayment().getUser().getId());

        UserEntity recipient = event.getPayment().getUser();
        String message = String.format("Thanh toán thành công số tiền: %s VND", event.getPayment().getAmount());
        NotificationEntity notification = NotificationEntity.builder()
                .recipient(recipient)
                .type(NotificationType.PAYMENT_SUCCESS)
                .message(message)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
        log.info("Đã tạo thông báo thanh toán cho user {}", recipient.getId());
        NotificationResponse response = notificationMapper.toResponse(notification);
        String destination = "user/" + recipient.getId() + "/queue/notifications";
        simpMessagingTemplate.convertAndSend(destination, response);
        log.info("Đã gửi real-time notification đến {}", destination);

    }

    private String buildMessage(TaskEntity task, UserEntity actor, NotificationType type) {
        String actorName = actor.getFirstName() != null ? actor.getFirstName() : actor.getUsername();
        String taskTitle = task.getTitle();

        return switch (type) {
            case TASK_ASSIGNED -> String.format("%s đã gán cho bạn task: '%s'", actorName, taskTitle);
            case TASK_UPDATED -> String.format("%s đã cập nhật task: '%s'", actorName, taskTitle);
            case TASK_COMPLETED -> String.format("%s đã hoàn thành task: '%s'", actorName, taskTitle);
            default -> String.format("Có cập nhật mới cho task: '%s'", taskTitle);
        };
    }

    @Override
    public PageResponse<NotificationResponse> getNotificationsForUser(Long userId, Pageable pageable) {
        log.info("Lấy thông báo cho user {} trang {}", userId, pageable.getPageNumber());
        Page<NotificationEntity> notifications =
                notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
        return new PageResponse<>(
                notifications.map(notificationMapper::toResponse).getContent(),
                notifications.getNumber(),
                notifications.getSize(),
                notifications.getTotalElements(),
                notifications.getTotalPages(),
                notifications.isLast());
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        NotificationEntity notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_EXISTED));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        notification.setRead(true);
        notificationRepository.save(notification);
        log.info("Notification {} marked as read by user {}", notificationId, userId);
    }

    @Override
    public void markAllAsReadForUser(Long userId) {
        log.info("Đánh dấu tất cả thông báo là đã đọc cho user {}", userId);
        notificationRepository.markAllAsReadForRecipient(userId);
    }
}
