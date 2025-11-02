package org.example.cv.services;

import java.util.List;

import org.example.cv.models.responses.ChatMessageDTO;
import org.example.cv.models.responses.UserResponse;

public interface ChatService {
    ChatMessageDTO.Response saveAndProcessProjectMessage(ChatMessageDTO.Request request);

    ChatMessageDTO.Response saveAndProcessPrivateMessage(ChatMessageDTO.Request request);

    List<ChatMessageDTO.Response> getProjectChatHistory(Long projectId, int page, int size);

    List<ChatMessageDTO.Response> getPrivateChatHistory(Long otherUserId, int page);

    List<ChatMessageDTO.ConversationDTO> getConversations();

    List<UserResponse> getChatMembers(String search);

    List<ChatMessageDTO.ProjectConversationDTO> getProjectConversations();
}
