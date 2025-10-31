package org.example.cv.utils.mapper;

import org.example.cv.models.entities.UserEntity;
import org.example.cv.models.requests.UserRequest;
import org.example.cv.models.responses.UserResponse;
import org.example.cv.models.responses.UserSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    UserEntity toEntity(UserRequest request);

    UserResponse toResponse(UserEntity user);

    void updateEntityFromRequest(UserRequest request, @MappingTarget UserEntity entity);

    @Mapping(target = "fullName", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    UserSummaryResponse toUserSummaryResponse(UserEntity user);
}
