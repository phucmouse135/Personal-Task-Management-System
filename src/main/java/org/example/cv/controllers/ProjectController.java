package org.example.cv.controllers;

import jakarta.validation.Valid;

import org.example.cv.models.requests.ProjectRequest;
import org.example.cv.models.requests.validationgroups.OnCreate;
import org.example.cv.models.requests.validationgroups.OnUpdate;
import org.example.cv.models.responses.ApiResponse;
import org.example.cv.models.responses.ProjectResponse;
import org.example.cv.services.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatusCode;
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

    // Page<ProjectResponse> getAll(int page, int size, String sortBy, String sortDir, String filter);

    /**
     * Get all projects with pagination, sorting, and filtering
     * @param page
     * @param size
     * @param sortBy
     * @param sortDir
     * @param filter
     * @return
     */
    @Operation(summary = "Get all projects with pagination, sorting, and filtering")
    @GetMapping
    public ApiResponse<Page<ProjectResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "") String filter) {
        log.info(
                "Request to get all projects: page={}, size={}, sortBy={}, sortDir={}, filter={}",
                page,
                size,
                sortBy,
                sortDir,
                filter);
        Page<ProjectResponse> projects = projectService.getAll(page, size, sortBy, sortDir, filter);
        return ApiResponse.<Page<ProjectResponse>>builder()
                .code(200)
                .result(projects)
                .message("Projects retrieved successfully")
                .build();
    }

    // ProjectResponse getById(Long id);
    /**
     * Get project by id
     * @param id
     * @return
     */
    @Operation(summary = "Get project by id")
    @GetMapping("/{id}")
    public ApiResponse<ProjectResponse> getById(@PathVariable Long id) {
        log.info("Request to get project by id: {}", id);
        ProjectResponse project = projectService.getById(id);
        return ApiResponse.<ProjectResponse>builder()
                .code(200)
                .result(project)
                .message("Project retrieved successfully")
                .build();
    }

    // ProjectResponse create(ProjectRequest request);
    /**
     * Create a new project
     * @param request
     * @return
     */
    @Operation(summary = "Create a new project")
    @PostMapping("/create")
    public ApiResponse<ProjectResponse> create(@RequestBody @Valid @Validated(OnCreate.class) ProjectRequest request) {
        log.info("Request to create project: {}", request);
        ProjectResponse project = projectService.create(request);
        return ApiResponse.<ProjectResponse>builder()
                .code(HttpStatusCode.valueOf(201).value())
                .result(project)
                .message("Project created successfully")
                .build();
    }

    // ProjectResponse update(Long id, ProjectRequest request);
    /**
     * Update an existing project
     * @param id
     * @param request
     * @return
     */
    @Operation(summary = "Update an existing project")
    @PutMapping("/{id}")
    public ApiResponse<ProjectResponse> update(
            @PathVariable("id") Long id, @RequestBody @Valid @Validated(OnUpdate.class) ProjectRequest request) {
        log.info("Request to update project: id={}, {}", id, request);
        ProjectResponse project = projectService.update(id, request);
        return ApiResponse.<ProjectResponse>builder()
                .code(200)
                .result(project)
                .message("Project updated successfully")
                .build();
    }

    // void softdelete(Long id);
    /**
     * Soft delete a project by id
     * @param id
     * @return
     */
    @Operation(summary = "Soft delete a project by id")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> softdelete(@PathVariable("id") Long id) {
        log.info("Request to soft delete project: id={}", id);
        projectService.softdelete(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Project soft deleted successfully")
                .build();
    }

    // void restore(Long id);
    /**
     * Restore a soft deleted project by id
     * @param id
     * @return
     */
    @Operation(summary = "Restore a soft deleted project by id")
    @PutMapping("/restore/{id}")
    public ApiResponse<Void> restore(@PathVariable("id") Long id) {
        log.info("Request to restore project: id={}", id);
        projectService.restore(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Project restored successfully")
                .build();
    }

    // Page<ProjectResponse> getAllByOwnerId(Long ownerId, int page, int size, String sortBy, String sortDir, String
    // filter);
    /**
     * Get all projects by owner id with pagination, sorting, and filtering
     * @param ownerId
     * @param page
     * @param size
     * @param sortBy
     * @param sortDir
     * @param filter
     * @return
     */
    @Operation(summary = "Get all projects by owner id with pagination, sorting, and filtering")
    @GetMapping("/owner/{ownerId}")
    public ApiResponse<Page<ProjectResponse>> getAllByOwnerId(
            @PathVariable("ownerId") Long ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "") String filter) {
        log.info(
                "Request to get all projects by owner id: ownerId={}, page={}, size={}, sortBy={}, sortDir={}, filter={}",
                ownerId,
                page,
                size,
                sortBy,
                sortDir,
                filter);
        Page<ProjectResponse> projects = projectService.getAllByOwnerId(ownerId, page, size, sortBy, sortDir, filter);
        return ApiResponse.<Page<ProjectResponse>>builder()
                .code(200)
                .result(projects)
                .message("Projects by owner retrieved successfully")
                .build();
    }

    // ProjectResponse addMember(Long projectId, Long userId);
    @Operation(summary = "Add a member to a project")
    @PostMapping("/{projectId}/members/{userId}")
    public ApiResponse<ProjectResponse> addMember(
            @PathVariable("projectId") Long projectId, @PathVariable("userId") Long userId) {
        log.info("Request to add member {} to project {}", userId, projectId);
        ProjectResponse project = projectService.addMember(projectId, userId);
        return ApiResponse.<ProjectResponse>builder()
                .code(200)
                .result(project)
                .message("Member added to project successfully")
                .build();
    }

    // ProjectResponse removeMember(Long projectId, Long userId);
    @Operation(summary = "Remove a member from a project")
    @DeleteMapping("/{projectId}/members/{userId}")
    public ApiResponse<ProjectResponse> removeMember(
            @PathVariable("projectId") Long projectId, @PathVariable("userId") Long userId) {
        log.info("Request to remove member {} from project {}", userId, projectId);
        ProjectResponse project = projectService.removeMember(projectId, userId);
        return ApiResponse.<ProjectResponse>builder()
                .code(200)
                .result(project)
                .message("Member removed from project successfully")
                .build();
    }

    // ProjectResponse changeOwner(Long projectId, Long newOwnerId);
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
