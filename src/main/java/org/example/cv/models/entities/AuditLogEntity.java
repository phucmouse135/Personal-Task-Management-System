package org.example.cv.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "actor_id", nullable = false)
    Long actorId;

    // Hành động: CREATE, UPDATE, DELETE
    @Column(name = "action_type", nullable = false)
    String actionType;

    // Tên Entity: TASK, PROJECT
    @Column(name = "entity_type", nullable = false)
    String entityType;

    // ID của Entity bị tác động
    @Column(name = "entity_id", nullable = false)
    Long entityId;

    // Nội dung thay đổi (Có thể lưu dạng JSON nếu muốn chi tiết)
    @Column(name = "details", columnDefinition = "TEXT")
    String details;

    @Column(name = "timestamp", nullable = false)
    @Builder.Default
    Instant timestamp = Instant.now();
}
