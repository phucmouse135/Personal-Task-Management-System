package org.example.cv.services.impl;

import java.util.List;
import java.util.Objects;

import org.example.cv.exceptions.AppException;
import org.example.cv.exceptions.ErrorCode;
import org.example.cv.models.entities.ChatMessageEntity;
import org.example.cv.models.entities.ProjectEntity;
import org.example.cv.models.entities.UserEntity;
import org.example.cv.models.responses.ChatMessageDTO;
import org.example.cv.repositories.ChatMessageRepository;
import org.example.cv.repositories.ProjectRepository;
import org.example.cv.repositories.UserRepository;
import org.example.cv.services.ChatService;
import org.example.cv.utils.AuthenticationUtils;
import org.example.cv.utils.mapper.ChatMessageMapper;
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
}
