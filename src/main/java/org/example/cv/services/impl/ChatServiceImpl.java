package org.example.cv.services.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.example.cv.exceptions.AppException;
import org.example.cv.exceptions.ErrorCode;
import org.example.cv.models.entities.ChatMessageEntity;
import org.example.cv.models.entities.ProjectEntity;
import org.example.cv.models.entities.UserEntity;
import org.example.cv.models.responses.ChatMessageDTO;
import org.example.cv.models.responses.UserResponse;
import org.example.cv.repositories.ChatMessageRepository;
import org.example.cv.repositories.ProjectRepository;
import org.example.cv.repositories.UserRepository;
import org.example.cv.services.ChatService;
import org.example.cv.utils.AuthenticationUtils;
import org.example.cv.utils.mapper.ChatMessageMapper;
import org.example.cv.utils.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {
    private final ChatMessageRepository chatRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ChatMessageMapper chatMapper;
    private final UserMapper userMapper;

    private static final int CHAT_HISTORY_PAGE_SIZE = 50;

    @Transactional
    @Override
    public ChatMessageDTO.Response saveAndProcessProjectMessage(ChatMessageDTO.Request request) {
        log.debug("Save and process project message request");
        ProjectEntity project = projectRepository
                .findById(request.getProjectId())
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));
        UserEntity sender = userRepository
                .findById(Objects.requireNonNull(AuthenticationUtils.getCurrentUserId()))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!project.getMembers().contains(sender)) {
            throw new AppException(ErrorCode.USER_NOT_PROJECT_MEMBER);
        }

        ChatMessageEntity chatMessageEntity = ChatMessageEntity.builder()
                .content(request.getContent())
                .sender(sender)
                .project(project)
                .build();

        return chatMapper.toDTO(chatRepository.save(chatMessageEntity));
    }

    @Transactional
    @Override
    public ChatMessageDTO.Response saveAndProcessPrivateMessage(ChatMessageDTO.Request request) {
        log.debug("Save and process private message request");
        UserEntity sender = userRepository
                .findById(Objects.requireNonNull(AuthenticationUtils.getCurrentUserId()))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        UserEntity receiver = userRepository
                .findById(request.getReceiverId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ChatMessageEntity chatMessageEntity = ChatMessageEntity.builder()
                .content(request.getContent())
                .sender(sender)
                .receiver(receiver)
                .build();

        return chatMapper.toDTO(chatRepository.save(chatMessageEntity));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ChatMessageDTO.Response> getProjectChatHistory(Long projectId, int page, int size) {
        log.debug("Get project chat history");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ChatMessageEntity> chatMessageEntities =
                chatRepository.findByProjectIdOrderByCreatedAtAsc(projectId, pageable);
        return chatMessageEntities.stream().map(chatMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ChatMessageDTO.Response> getPrivateChatHistory(Long otherUserId, int page) {
        log.debug("Get private chat history");
        Long currentUserId = Objects.requireNonNull(AuthenticationUtils.getCurrentUserId());
        Pageable pageable = PageRequest.of(page, CHAT_HISTORY_PAGE_SIZE);
        List<ChatMessageEntity> chatMessageEntities =
                chatRepository.findPrivateChatHistory(currentUserId, otherUserId, pageable);
        return chatMessageEntities.stream().map(chatMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ChatMessageDTO.ConversationDTO> getConversations() {
        Long currentUserId = Objects.requireNonNull(AuthenticationUtils.getCurrentUserId());

        List<UserEntity> users = chatRepository.findConversationUsers(currentUserId);

        return users.stream()
                .filter(Objects::nonNull)
                .map(user -> {
                    ChatMessageEntity lastMessage =
                            chatRepository.findLastMessageBetweenUsers(currentUserId, user.getId());

                    String fullName = "";
                    if (user.getFirstName() != null || user.getLastName() != null) {
                        fullName = (user.getFirstName() != null ? user.getFirstName() : "")
                                + " "
                                + (user.getLastName() != null ? user.getLastName() : "");
                        fullName = fullName.trim();
                    }
                    if (fullName.isEmpty()) {
                        fullName = user.getUsername();
                    }

                    return ChatMessageDTO.ConversationDTO.builder()
                            .userId(user.getId())
                            .name(fullName)
                            .email(user.getEmail())
                            .lastMessage(lastMessage != null ? lastMessage.getContent() : null)
                            .lastMessageTime(lastMessage != null ? lastMessage.getCreatedAt() : null)
                            .online(false) // TODO: implement online status
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserResponse> getChatMembers(String search) {
        log.debug("Get chat members with search: {}", search);
        Long currentUserId = Objects.requireNonNull(AuthenticationUtils.getCurrentUserId());

        // Get all projects where current user is a member
        List<ProjectEntity> projects = projectRepository.findAllByMembersId(currentUserId);

        // Collect all unique members from these projects
        return projects.stream()
                .flatMap(project -> project.getMembers().stream())
                .filter(user -> !user.getId().equals(currentUserId)) // Exclude current user
                .distinct()
                .filter(user -> {
                    if (search == null || search.trim().isEmpty()) {
                        return true;
                    }
                    String searchLower = search.toLowerCase();
                    return user.getUsername().toLowerCase().contains(searchLower)
                            || user.getEmail().toLowerCase().contains(searchLower)
                            || (user.getFirstName() + " " + user.getLastName())
                                    .toLowerCase()
                                    .contains(searchLower);
                })
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ChatMessageDTO.ProjectConversationDTO> getProjectConversations() {
        log.debug("Get project conversations");
        Long currentUserId = Objects.requireNonNull(AuthenticationUtils.getCurrentUserId());

        // Get all projects where current user is a member
        List<ProjectEntity> projects = projectRepository.findAllByMembersId(currentUserId);

        return projects.stream()
                .map(project -> {
                    ChatMessageEntity lastMessage =
                            chatRepository.findTopByProjectIdOrderByCreatedAtDesc(project.getId());

                    return ChatMessageDTO.ProjectConversationDTO.builder()
                            .projectId(project.getId())
                            .projectName(project.getName())
                            .memberCount(project.getMembers().size())
                            .lastMessage(lastMessage != null ? lastMessage.getContent() : null)
                            .lastMessageTime(lastMessage != null ? lastMessage.getCreatedAt() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
