package org.example.cv.utils.mapper;

import org.example.cv.models.entities.ProjectEntity;
import org.example.cv.models.entities.TaskEntity;
import org.example.cv.models.entities.UserEntity;
import org.example.cv.models.requests.CreateTaskRequest;
import org.example.cv.models.requests.UpdateTaskRequest;
import org.example.cv.models.responses.ProjectSummaryResponse;
import org.example.cv.models.responses.TaskResponse;
import org.example.cv.models.responses.UserSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(source = "project", target = "project")
    @Mapping(source = "assignee", target = "assignee")
    TaskResponse toTaskResponse(TaskEntity task);

    ProjectSummaryResponse projectToProjectSummaryResponse(ProjectEntity project);

    UserSummaryResponse userToUserSummaryResponse(UserEntity user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true) // Sẽ được set default trong Service
    @Mapping(target = "project", ignore = true) // Service sẽ set
    @Mapping(target = "assignee", ignore = true) // Service sẽ set
    TaskEntity toEntity(CreateTaskRequest request);

    @Mapping(target = "id", ignore = true)
    //    @Mapping(target = "createdAt", ignore = true)
    //    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "project", ignore = true) // Không cho phép đổi project
    @Mapping(target = "assignee", ignore = true) // Service sẽ xử lý logic đổi assignee
    void updateEntityFromRequest(UpdateTaskRequest request, @MappingTarget TaskEntity task);
}
