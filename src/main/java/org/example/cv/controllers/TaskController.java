package org.example.cv.controllers;

import jakarta.validation.Valid;

import org.example.cv.models.requests.CreateTaskRequest;
import org.example.cv.models.requests.TaskFilterRequest;
import org.example.cv.models.requests.UpdateTaskRequest;
import org.example.cv.models.requests.UpdateTaskStatusRequest;
import org.example.cv.models.responses.PageResponse;
import org.example.cv.models.responses.TaskResponse;
import org.example.cv.services.TaskService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Controller", description = "API cho quản lý Công việc")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Lấy danh sách task (có lọc, sắp xếp, phân trang)")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping
    public ResponseEntity<PageResponse<TaskResponse>> getAllTasks(
            @ParameterObject @Valid TaskFilterRequest filter,
            @ParameterObject @PageableDefault(size = 20, sort = "deadline") Pageable pageable) {
        PageResponse<TaskResponse> response = taskService.getAllTasks(filter, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lấy danh sách task của người dùng hiện tại (có phân trang)")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/my-tasks")
    public ResponseEntity<PageResponse<TaskResponse>> getMyTasks(
            @ParameterObject @PageableDefault(size = 20, sort = "deadline") Pageable pageable) {
        PageResponse<TaskResponse> response = taskService.getMyTasks(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Tạo một task mới")
    @ApiResponse(responseCode = "201", description = "Tạo thành công")
    @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Lấy chi tiết một task theo ID")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @ApiResponse(responseCode = "404", description = "Không tìm thấy Task")
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cập nhật một task (PUT)")
    @ApiResponse(responseCode = "200", description = "Cập nhật thành công")
    @ApiResponse(responseCode = "404", description = "Không tìm thấy Task")
    @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id, @Valid @RequestBody UpdateTaskRequest request) {
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cập nhật trạng thái task (PATCH) - cho assignee")
    @ApiResponse(responseCode = "200", description = "Cập nhật trạng thái thành công")
    @ApiResponse(responseCode = "404", description = "Không tìm thấy Task")
    @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateTaskStatusRequest request) {
        TaskResponse response = taskService.updateTaskStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Xóa một task")
    @ApiResponse(responseCode = "204", description = "Xóa thành công")
    @ApiResponse(responseCode = "404", description = "Không tìm thấy Task")
    @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Khôi phục một task đã xóa")
    @ApiResponse(responseCode = "204", description = "Khôi phục thành công")
    @ApiResponse(responseCode = "404", description = "Không tìm thấy Task")
    @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    @PatchMapping("/{id}/restore")
    public ResponseEntity<Void> restoreTask(@PathVariable Long id) {
        taskService.restoreTask(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lấy danh sách Task đã xóa mềm")
    @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    @GetMapping("/soft-deleted")
    public ResponseEntity<PageResponse<TaskResponse>> getSoftDeletedTasks(
            @RequestParam(defaultValue = "my") String scope,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<TaskResponse> response;
        if ("all".equals(scope)) {
            response = taskService.getAllSoftDeletedTasks(page, size);
        } else {
            response = taskService.getAllMySoftDeletedTasks(page, size);
        }
        return ResponseEntity.ok(response);
    }
}
