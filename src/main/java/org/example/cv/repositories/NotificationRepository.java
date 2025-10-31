package org.example.cv.repositories;

import org.example.cv.models.entities.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends BaseRepository<NotificationEntity, Long> {

    /**
     * Lấy danh sách thông báo của user, sắp xếp theo thời gian tạo giảm dần
     */
    Page<NotificationEntity> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    /**
     * Đếm số thông báo chưa đọc của user
     */
    long countByRecipientIdAndIsReadFalse(Long recipientId);

    /**
     * Đánh dấu tất cả là đã đọc cho user
     */
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.recipient.id = :recipientId AND n.isRead = false")
    void markAllAsReadForRecipient(Long recipientId);
}
