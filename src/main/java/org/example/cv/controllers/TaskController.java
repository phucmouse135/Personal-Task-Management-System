package org.example.cv.controllers;

import jakarta.validation.Valid;

import org.example.cv.models.requests.CreateTaskRequest;
import org.example.cv.models.requests.TaskFilterRequest;
import org.example.cv.models.requests.UpdateTaskRequest;
import org.example.cv.models.requests.UpdateTaskStatusRequest;
import org.example.cv.models.responses.ApiResponse; // Import class wrapper của bạn
import org.example.cv.models.responses.PageResponse;
import org.example.cv.models.responses.TaskResponse;
import org.example.cv.services.TaskService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Controller", description = "API quản lý công việc")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Lấy danh sách task (Search & Filter)")
    @GetMapping
    public ApiResponse<PageResponse<TaskResponse>> getAllTasks(
            @ParameterObject @Valid TaskFilterRequest filter,
            @ParameterObject @PageableDefault(size = 10, sort = "deadline", direction = Sort.Direction.ASC) Pageable pageable) {
        return ApiResponse.<PageResponse<TaskResponse>>builder()
                .code(200)
                .result(taskService.getAllTasks(filter, pageable))
                .message("Lấy danh sách task thành công")
                .build();
    }

    @Operation(summary = "Lấy task của tôi")
    @GetMapping("/my-tasks")
    public ApiResponse<PageResponse<TaskResponse>> getMyTasks(
            @ParameterObject @PageableDefault(size = 10, sort = "deadline", direction = Sort.Direction.ASC) Pageable pageable) {
        return ApiResponse.<PageResponse<TaskResponse>>builder()
                .code(200)
                .result(taskService.getMyTasks(pageable))
                .build();
    }

    @Operation(summary = "Tạo task mới")
    @PostMapping
    public ApiResponse<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        return ApiResponse.<TaskResponse>builder()
                .code(HttpStatus.CREATED.value()) // 201
                .result(taskService.createTask(request))
                .message("Tạo task thành công")
                .build();
    }

    @Operation(summary = "Xem chi tiết task")
    @GetMapping("/{id}")
    public ApiResponse<TaskResponse> getTaskById(@PathVariable Long id) {
        return ApiResponse.<TaskResponse>builder()
                .code(200)
                .result(taskService.getTaskById(id))
                .build();
    }

    @Operation(summary = "Cập nhật thông tin task")
    @PutMapping("/{id}")
    public ApiResponse<TaskResponse> updateTask(
            @PathVariable Long id, @Valid @RequestBody UpdateTaskRequest request) {
        return ApiResponse.<TaskResponse>builder()
                .code(200)
                .result(taskService.updateTask(id, request))
                .message("Cập nhật task thành công")
                .build();
    }

    @Operation(summary = "Cập nhật trạng thái task (Assignee)")
    @PatchMapping("/{id}/status")
    public ApiResponse<TaskResponse> updateTaskStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateTaskStatusRequest request) {
        return ApiResponse.<TaskResponse>builder()
                .code(200)
                .result(taskService.updateTaskStatus(id, request))
                .message("Cập nhật trạng thái thành công")
                .build();
    }

    @Operation(summary = "Xóa task (Soft Delete)")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ApiResponse.<Void>builder()
                .code(204) // No Content
                .message("Xóa task thành công")
                .build();
    }

    @Operation(summary = "Khôi phục task")
    @PatchMapping("/{id}/restore")
    public ApiResponse<Void> restoreTask(@PathVariable Long id) {
        taskService.restoreTask(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Khôi phục task thành công")
                .build();
    }

    @Operation(summary = "Lấy danh sách đã xóa (Admin/User)")
    @GetMapping("/soft-deleted")
    public ApiResponse<PageResponse<TaskResponse>> getSoftDeletedTasks(
            @RequestParam(defaultValue = "my") String scope,
            @ParameterObject @PageableDefault(sort = "deletedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponse<TaskResponse> result;
        if ("all".equalsIgnoreCase(scope)) {
            result = taskService.getAllSoftDeletedTasks(pageable); // Service cần nhận Pageable
        } else {
            result = taskService.getAllMySoftDeletedTasks(pageable); // Service cần nhận Pageable
        }

        return ApiResponse.<PageResponse<TaskResponse>>builder()
                .code(200)
                .result(result)
                .message("Lấy danh sách đã xóa thành công")
                .build();
    }
}