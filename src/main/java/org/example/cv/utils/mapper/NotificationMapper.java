package org.example.cv.utils.mapper;

import org.example.cv.models.entities.NotificationEntity;
import org.example.cv.models.responses.NotificationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface NotificationMapper {
    @Mapping(source = "task.id", target = "taskId")
    @Mapping(source = "actor", target = "actor") // Sẽ dùng UserMapper.toUserSummaryResponse
    NotificationResponse toResponse(NotificationEntity entity);
}
