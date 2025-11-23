package org.example.cv.controllers;

import jakarta.validation.Valid;

import org.example.cv.models.requests.UserRequest;
import org.example.cv.models.responses.ApiResponse;
import org.example.cv.models.responses.UserResponse;
import org.example.cv.services.UserService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "User Management", description = "Endpoints for managing users")
public class UserController {

    UserService userService;

    @Operation(summary = "Create a new user")
    @PostMapping // Bỏ "/create" -> chuẩn RESTful
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserRequest userRequest) {
        log.info("Create user request: {}", userRequest.getUsername());
        return ApiResponse.<UserResponse>builder()
                .code(HttpStatus.CREATED.value()) // Trả về 201 Created
                .result(userService.createUser(userRequest))
                .message("User created successfully")
                .build();
    }

    @Operation(summary = "Get current user info")
    @GetMapping("/my-info") // Sửa thành kebab-case
    public ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .result(userService.getMyInfo())
                .build();
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable("id") Long id) {
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .result(userService.getUserById(id))
                .build();
    }

    @Operation(summary = "Get user by Username")
    @GetMapping("/username/{username}")
    public ApiResponse<UserResponse> getUserByUsername(@PathVariable("username") String username) {
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .result(userService.getUserByUsername(username))
                .build();
    }

    @Operation(summary = "Get all users (Pagination & Sorting)")
    @GetMapping
    public ApiResponse<Page<UserResponse>> getAllUsers(
            @RequestParam(value = "search", required = false) String search,
            @ParameterObject @PageableDefault(size = 10 , sort = "id" , direction = Sort.Direction.DESC) Pageable pageable) {

        // Lưu ý: Bạn cần cập nhật Service để nhận Pageable và String search
        log.info("Get users list: search={}, pageable={}", search, pageable);
        return ApiResponse.<Page<UserResponse>>builder()
                .code(200)
                .result(userService.getAllUsers(search, pageable))
                .build();
    }

    @Operation(summary = "Update user details")
    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable("id") Long id, @RequestBody @Valid UserRequest userRequest) {
        log.info("Update user request: id={}", id);
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .result(userService.updateUser(id, userRequest))
                .message("User updated successfully")
                .build();
    }

    @Operation(summary = "Soft delete a user")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> softdeleteUser(@PathVariable("id") Long id) {
        userService.softdeleteUser(id);
        return ApiResponse.<Void>builder()
                .code(200) // Hoặc 204 No Content
                .message("User soft deleted successfully")
                .build();
    }

    @Operation(summary = "Restore a soft-deleted user")
    @PatchMapping("/{id}/restore") // Restore là update trạng thái -> Dùng PATCH hoặc PUT
    public ApiResponse<Void> restoreUser(@PathVariable("id") Long id) {
        userService.restoreUser(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("User restored successfully")
                .build();
    }


    @Operation(summary = "Assign role to user")
    @PostMapping("/{userId}/roles/{roleId}")
    public ApiResponse<UserResponse> assignRoleToUser(
            @PathVariable("userId") Long userId,
            @PathVariable("roleId") String roleId) { // Kiểm tra lại: roleId là String hay Long?
        log.info("Assign role {} to user {}", roleId, userId);
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .result(userService.assignRoleToUser(userId, roleId))
                .message("Role assigned successfully")
                .build();
    }

    @Operation(summary = "Remove role from user")
    @DeleteMapping("/{userId}/roles/{roleId}")
    public ApiResponse<UserResponse> removeRoleFromUser(
            @PathVariable("userId") Long userId,
            @PathVariable("roleId") String roleId) {
        log.info("Remove role {} from user {}", roleId, userId);
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .result(userService.removeRoleFromUser(userId, roleId))
                .message("Role removed successfully")
                .build();
    }
}