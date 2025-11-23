package org.example.cv.models.entities;

import jakarta.persistence.*;

import org.example.cv.models.entities.base.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "chat_messages")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "ChatMessageEntity", description = "Entity representing a chat message")
public class ChatMessageEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Schema(description = "Sender of the chat message")
    @JoinColumn(name = "sender_id", nullable = false)
    UserEntity sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id") // Nullable, dùng cho chat riêng
    UserEntity receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id") // Nullable, dùng cho chat dự án
    ProjectEntity project;

    @Column(columnDefinition = "TEXT", nullable = false)
    String content;
}
