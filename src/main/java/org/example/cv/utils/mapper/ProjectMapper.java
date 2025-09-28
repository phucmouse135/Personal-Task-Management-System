package org.example.cv.utils.mapper;

import org.example.cv.models.entities.ProjectEntity;
import org.example.cv.models.requests.ProjectRequest;
import org.example.cv.models.responses.ProjectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    ProjectEntity toEntity(ProjectRequest request);

    ProjectResponse toResponse(ProjectEntity entity);

    void updateEntityFromRequest(ProjectRequest request, @MappingTarget ProjectEntity entity);
}
