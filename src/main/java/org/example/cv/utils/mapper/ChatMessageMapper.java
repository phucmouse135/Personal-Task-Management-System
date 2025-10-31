package org.example.cv.utils.mapper;

import org.example.cv.models.entities.ChatMessageEntity;
import org.example.cv.models.responses.ChatMessageDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    @Mapping(target = "project", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "receiver", ignore = true)
    @Mapping(target = "id", ignore = true)
    ChatMessageEntity toEntity(ChatMessageDTO.Request request);

    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "receiver.id", target = "receiverId")
    @Mapping(source = "sender.username", target = "senderUsername")
    ChatMessageDTO.Response toDTO(ChatMessageEntity entity);
}
