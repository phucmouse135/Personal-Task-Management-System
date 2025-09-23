package org.example.cv.utils.mapper;

import org.example.cv.models.entities.UserEntity;
import org.example.cv.models.requests.UserRequest;
import org.example.cv.models.responses.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role.id", source = "request.roleIds")
    UserEntity toEntity(UserRequest request);

    UserResponse toResponse(UserEntity user);

    @Mapping(target = "roles" , ignore = true)
    void updateEntityFromRequest(UserRequest request, @MappingTarget UserEntity entity);




}
