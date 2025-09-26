package org.example.cv.utils.mapper;

import org.example.cv.models.entities.UserEntity;
import org.example.cv.models.requests.UserRequest;
import org.example.cv.models.responses.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserEntity toEntity(UserRequest request);

    UserResponse toResponse(UserEntity user);

    void updateEntityFromRequest(UserRequest request, @MappingTarget UserEntity entity);
}
