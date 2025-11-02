package org.example.cv.models.entities;

import jakarta.persistence.*;

import org.example.cv.constants.NotificationType;
import org.example.cv.models.entities.base.BaseEntity;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@NamedEntityGraphs(
        @NamedEntityGraph(
                name = "Notification.withRecipientAndActor",
                attributeNodes = {@NamedAttributeNode("recipient"), @NamedAttributeNode("actor")}))
public class NotificationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private UserEntity recipient; // Người nhận thông báo

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private TaskEntity task; // Task liên quan (có thể null)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String message; // Nội dung thông báo

    @Column(nullable = false)
    private boolean isRead = false; // Mặc định là chưa đọc

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private UserEntity actor; // Người thực hiện hành động (có thể null)
}
