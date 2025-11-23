package org.example.cv.controllers;

import jakarta.validation.Valid;

import org.example.cv.models.requests.ProjectRequest;
import org.example.cv.models.requests.validationgroups.OnCreate;
import org.example.cv.models.requests.validationgroups.OnUpdate;
import org.example.cv.models.responses.ApiResponse;
import org.example.cv.models.responses.PageResponse; // Nên dùng cái này thống nhất
import org.example.cv.models.responses.ProjectResponse;
import org.example.cv.services.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus; // Sửa import
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/projects")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Project Controller", description = "Controller for managing projects")
public class ProjectController {
    ProjectService projectService;

    @Operation(summary = "Get all projects with pagination, sorting, and filtering")
    @GetMapping
    public ApiResponse<Page<ProjectResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "") String filter) {
        log.info("GET all projects: page={}, size={}, filter={}", page, size, filter);
        return ApiResponse.<Page<ProjectResponse>>builder()
                .code(200)
                .result(projectService.getAll(page, size, sortBy, sortDir, filter))
                .message("Projects retrieved successfully")
                .build();
    }

    @Operation(summary = "Get my projects")
    @GetMapping("/my-projects")
    public ApiResponse<Page<ProjectResponse>> getMyProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<ProjectResponse>>builder()
                .code(200)
                .result(projectService.getMyProjects(page, size))
                .build();
    }

    @Operation(summary = "Get project by id")
    @GetMapping("/{id}")
    public ApiResponse<ProjectResponse> getById(@PathVariable Long id) {
        return ApiResponse.<ProjectResponse>builder()
                .code(200)
                .result(projectService.getById(id))
                .build();
    }

    @Operation(summary = "Create a new project")
    @PostMapping
    public ApiResponse<ProjectResponse> create(@RequestBody @Valid @Validated(OnCreate.class) ProjectRequest request) {
        log.info("Create project: {}", request.getName());
        return ApiResponse.<ProjectResponse>builder()
                .code(HttpStatus.CREATED.value()) // Dùng Enum chuẩn
                .result(projectService.create(request))
                .message("Project created successfully")
                .build();
    }

    @Operation(summary = "Update an existing project")
    @PutMapping("/{id}")
    public ApiResponse<ProjectResponse> update(
            @PathVariable("id") Long id,
            @RequestBody @Valid @Validated(OnUpdate.class) ProjectRequest request) {
        return ApiResponse.<ProjectResponse>builder()
                .code(200)
                .result(projectService.update(id, request))
                .message("Project updated successfully")
                .build();
    }

    @Operation(summary = "Soft delete a project by id")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> softdelete(@PathVariable("id") Long id) {
        projectService.softdelete(id);
        return ApiResponse.<Void>builder()
                .code(200) // Hoặc 204 No Content nếu không trả về body
                .message("Project soft deleted successfully")
                .build();
    }

    @Operation(summary = "Restore a soft deleted project by id")
    @PutMapping("/{id}/restore")
    public ApiResponse<Void> restore(@PathVariable("id") Long id) {
        projectService.restore(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Project restored successfully")
                .build();
    }

    @Operation(summary = "Get soft deleted projects (Scope: 'my' or 'all')")
    @GetMapping("/soft-deleted")
    public ApiResponse<PageResponse<ProjectResponse>> getSoftDeletedProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "all") String scope) {

        // Logic if-else này NÊN chuyển xuống Service để Controller gọn nhẹ
        // Controller chỉ nên điều hướng request
        PageResponse<ProjectResponse> projects = "my".equalsIgnoreCase(scope)
                ? projectService.getAllMySoftDeletedProjects(page, size)
                : projectService.getAllSoftDeletedProjects(page, size);

        return ApiResponse.<PageResponse<ProjectResponse>>builder()
                .code(200)
                .result(projects)
                .build();
    }

    @Operation(summary = "Get projects by owner id")
    @GetMapping("/owner/{ownerId}")
    public ApiResponse<Page<ProjectResponse>> getAllByOwnerId(
            @PathVariable("ownerId") Long ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "") String filter) {
        return ApiResponse.<Page<ProjectResponse>>builder()
                .code(200)
                .result(projectService.getAllByOwnerId(ownerId, page, size, sortBy, sortDir, filter))
                .build();
    }

    // Sub-resources: Rất tốt, giữ nguyên
    @Operation(summary = "Add a member to a project")
    @PostMapping("/{projectId}/members/{userId}")
    public ApiResponse<ProjectResponse> addMember(
            @PathVariable("projectId") Long projectId,
            @PathVariable("userId") Long userId) {
        return ApiResponse.<ProjectResponse>builder()
                .code(200)
                .result(projectService.addMember(projectId, userId))
                .message("Member added successfully")
                .build();
    }

    @Operation(summary = "Remove a member from a project")
    @DeleteMapping("/{projectId}/members/{userId}")
    public ApiResponse<ProjectResponse> removeMember(
            @PathVariable("projectId") Long projectId,
            @PathVariable("userId") Long userId) {
        return ApiResponse.<ProjectResponse>builder()
                .code(200)
                .result(projectService.removeMember(projectId, userId))
                .message("Member removed successfully")
                .build();
    }


    @Operation(summary = "Change the owner of a project")
    @PutMapping("/{projectId}/owner/{newOwnerId}")
    public ApiResponse<ProjectResponse> changeOwner(
            @PathVariable("projectId") Long projectId, @PathVariable("newOwnerId") Long newOwnerId) {
        log.info("Request to change owner of project {} to {}", projectId, newOwnerId);
        ProjectResponse project = projectService.changeOwner(projectId, newOwnerId);
        return ApiResponse.<ProjectResponse>builder()
                .code(200)
                .result(project)
                .message("Project owner changed successfully")
                .build();
    }
}
