package org.example.cv.utils.mapper;

import org.example.cv.models.entities.RoleEntity;
import org.example.cv.models.requests.RoleRequest;
import org.example.cv.models.responses.RoleResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleEntity toEntity(RoleRequest request);

    RoleResponse toResponse(RoleEntity role);
}
