package org.example.cv.controllers;

import java.util.List;

import org.example.cv.models.responses.ApiResponse;
import org.example.cv.models.responses.ChatMessageDTO;
import org.example.cv.services.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat Controller", description = "Handles chat messages for projects and private conversations")
public class ChatController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatService chatService;

    /**
     * Handle project chat messages
     * @param request
     */
    @MessageMapping("/chat.project")
    public void handleProjectMessage(@Payload @Validated ChatMessageDTO.Request request) {
        // 1. Save and process the message
        log.info("Received request to handle project message");
        ChatMessageDTO.Response response = chatService.saveAndProcessProjectMessage(request);

        // 2. Send the message to the project topic
        String destination = "/topic/chat.project." + request.getProjectId();
        simpMessagingTemplate.convertAndSend(destination, response);
        log.info("Sent response to project message");
    }

    /**
     * Handle private chat messages
     * @param request
     */
    @MessageMapping("/chat.private")
    public void handlePrivateMessage(@Payload @Validated ChatMessageDTO.Request request) {
        // 1. Save and process the message
        log.info("Received request to handle private message");
        ChatMessageDTO.Response response = chatService.saveAndProcessPrivateMessage(request);

        // 2. Send the message to the receiver's private queue
        String destination = "/queue/chat.private." + request.getReceiverId();
        simpMessagingTemplate.convertAndSend(destination, response);
        log.info("Sent response to private message");
    }

    /**
     * Get project chat history
     * @param projectId
     * @param page
     * @param size
     * @return
     */
    @Operation(summary = "Get project chat history")
    @GetMapping("/api/chat/project/{projectId}")
    public ResponseEntity<ApiResponse<List<ChatMessageDTO.Response>>> getProjectChatHistory(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Received request to get project chat history");
        List<ChatMessageDTO.Response> chatHistory = chatService.getProjectChatHistory(projectId, page, size);
        return ResponseEntity.ok(new ApiResponse<>(200, "Project chat history retrieved successfully", chatHistory));
    }

    /**
     * Get private chat history with another user
     * @param otherUserId
     * @param page
     * @return
     */
    @Operation(summary = "Get private chat history with another user")
    @GetMapping("/api/chat/private/{otherUserId}")
    public ResponseEntity<ApiResponse<List<ChatMessageDTO.Response>>> getPrivateChatHistory(
            @PathVariable Long otherUserId, @RequestParam(defaultValue = "0") int page) {
        log.info("Received request to get private chat history");
        List<ChatMessageDTO.Response> chatHistory = chatService.getPrivateChatHistory(otherUserId, page);
        return ResponseEntity.ok(new ApiResponse<>(200, "Private chat history retrieved successfully", chatHistory));
    }
}
