package org.example.cv.models.responses;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Lớp này chứa các DTOs cho Chat
 */
@Schema(description = "Chat message DTO container")
public class ChatMessageDTO {

    /**
     * DTO khi Client gửi tin nhắn lên (cho cả chat riêng và chat dự án)
     */
    @Data
    @Schema(description = "Request payload to send a chat message")
    public static class Request {
        @NotBlank
        @Size(max = 2000)
        @Schema(description = "Message content", example = "Hello, can you review the latest changes?", required = true)
        private String content;

        @Positive(message = "projectId must be a positive number")
        @Schema(description = "Project id when sending a project-scoped message", example = "123", nullable = true)
        private Long projectId; // ID dự án (nếu là chat dự án)

        @Positive(message = "receiverId must be a positive number")
        @Schema(description = "Receiver user id when sending a direct message", example = "456", nullable = true)
        private Long receiverId; // ID người nhận (nếu là chat riêng)
    }

    /**
     * DTO khi Server broadcast tin nhắn về cho Clients
     */
    @Data
    @Schema(description = "Response payload broadcasted to clients for a chat message")
    public static class Response {
        @Schema(description = "Message identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        private Long id;

        @Schema(
                description = "Message content",
                example = "Hello, can you review the latest changes?",
                accessMode = Schema.AccessMode.READ_ONLY)
        private String content;

        @Schema(
                description = "Creation timestamp in ISO-8601",
                example = "2025-10-27T08:00:00Z",
                accessMode = Schema.AccessMode.READ_ONLY)
        private Instant createdAt;

        @Schema(
                description = "Project id if message belongs to a project",
                example = "123",
                nullable = true,
                accessMode = Schema.AccessMode.READ_ONLY)
        private Long projectId;

        @Schema(
                description = "Receiver user id if direct message",
                example = "456",
                nullable = true,
                accessMode = Schema.AccessMode.READ_ONLY)
        private Long receiverId;

        @Schema(description = "Sender user id", example = "789", accessMode = Schema.AccessMode.READ_ONLY)
        private Long senderId;

        @Schema(description = "Sender username", example = "alice", accessMode = Schema.AccessMode.READ_ONLY)
        private String senderUsername;
    }
}
