package org.example.cv.controllers;

import jakarta.validation.Valid;

import org.example.cv.models.requests.UserRequest;
import org.example.cv.models.responses.ApiResponse;
import org.example.cv.models.responses.UserResponse;
import org.example.cv.services.UserService;
import org.springframework.data.domain.Page;
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

    /**
     * Create a new user
     * Endpoint to create a new user in the system.
     * @param userRequest
     * @return
     */
    @Operation(summary = "Create a new user", description = "Creates a new user with the provided details")
    @PostMapping("/create")
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserRequest userRequest) {
        log.info("Received request to create user: {}", userRequest.getUsername());
        UserResponse userResponse = userService.createUser(userRequest);
        log.info("User created successfully: {}", userResponse.getId());
        return ApiResponse.<UserResponse>builder().result(userResponse).build();
    }

    // UserResponse getUserById(Long id);
    /**
     * Get user by ID
     * Endpoint to retrieve user details by their ID.
     */
    @Operation(summary = "Get user by ID", description = "Retrieves user details by their ID")
    @Parameter(name = "id", description = "ID of the user to retrieve", required = true)
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable("id") Long id) {
        log.info("Received request to get user by ID: {}", id);
        UserResponse userResponse = userService.getUserById(id);
        log.info("User retrieved successfully: {}", userResponse.getUsername());
        return ApiResponse.<UserResponse>builder().result(userResponse).build();
    }

    // Page<UserResponse> getAllUsers(int page, int size, String search, String sort, String direction);
    /**
     * Get all users with pagination and filtering
     * Endpoint to retrieve a paginated list of users with optional search and sorting.
     */
    @Operation(
            summary = "Get all users",
            description = "Retrieves a paginated list of users with optional search and sorting")
    @GetMapping
    public ApiResponse<Page<UserResponse>> getAllUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "sort", defaultValue = "id") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        log.info(
                "Received request to get all users: page={}, size={}, search={}, sort={}, direction={}",
                page,
                size,
                search,
                sort,
                direction);
        Page<UserResponse> usersPage = userService.getAllUsers(page, size, search, sort, direction);
        log.info("Users retrieved successfully: totalElements={}", usersPage.getTotalElements());
        return ApiResponse.<Page<UserResponse>>builder().result(usersPage).build();
    }

    // UserResponse updateUser(Long id, UserRequest userRequest);
    /**
     * Update user details
     * Endpoint to update the details of an existing user.
     */
    @Operation(summary = "Update user details", description = "Updates the details of an existing user")
    @Parameter(name = "id", description = "ID of the user to update", required = true)
    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable("id") Long id, @RequestBody @Valid UserRequest userRequest) {
        log.info("Received request to update user: {}", id);
        UserResponse userResponse = userService.updateUser(id, userRequest);
        log.info("User updated successfully: {}", userResponse.getUsername());
        return ApiResponse.<UserResponse>builder().result(userResponse).build();
    }

    // void softdeleteUser(Long id);
    /**
     * Soft delete a user
     * Endpoint to soft delete a user by their ID.
     */
    @Operation(summary = "Soft delete a user", description = "Soft deletes a user by their ID")
    @Parameter(name = "id", description = "ID of the user to soft delete", required = true)
    @DeleteMapping("/{id}")
    public ApiResponse<String> softdeleteUser(@PathVariable("id") Long id) {
        log.info("Received request to soft delete user: {}", id);
        userService.softdeleteUser(id);
        log.info("User soft deleted successfully: {}", id);
        return ApiResponse.<String>builder()
                .result("User soft deleted successfully")
                .build();
    }

    // UserResponse assignRoleToUser(Long userId, Long roleId);
    /**
     * Assign role to user
     * Endpoint to assign a role to a user.
     */
    @Operation(summary = "Assign role to user", description = "Assigns a role to a user")
    @PostMapping("/{userId}/roles/{roleId}")
    public ApiResponse<UserResponse> assignRoleToUser(
            @PathVariable("userId") Long userId, @PathVariable("roleId") String roleId) {
        log.info("Received request to assign role: {} to user: {}", roleId, userId);
        UserResponse userResponse = userService.assignRoleToUser(userId, roleId);
        log.info("Role assigned successfully to user: {}", userResponse.getUsername());
        return ApiResponse.<UserResponse>builder().result(userResponse).build();
    }

    // UserResponse removeRoleFromUser(Long userId, Long roleId);
    /**
     * Remove role from user
     * Endpoint to remove a role from a user.
     */
    @Operation(summary = "Remove role from user", description = "Removes a role from a user")
    @DeleteMapping("/{userId}/roles/{roleId}")
    public ApiResponse<UserResponse> removeRoleFromUser(
            @PathVariable("userId") Long userId, @PathVariable("roleId") String roleId) {
        log.info("Received request to remove role: {} from user: {}", roleId, userId);
        UserResponse userResponse = userService.removeRoleFromUser(userId, roleId);
        log.info("Role removed successfully from user: {}", userResponse.getUsername());
        return ApiResponse.<UserResponse>builder().result(userResponse).build();
    }

    // void restoreUser(Long id);
    /**
     * Restore a soft-deleted user
     * Endpoint to restore a soft-deleted user by their ID.
     */
    @Operation(summary = "Restore a soft-deleted user", description = "Restores a soft-deleted user by their ID")
    @Parameter(name = "id", description = "ID of the user to restore", required = true)
    @PostMapping("/{id}/restore")
    public ApiResponse<String> restoreUser(@PathVariable("id") Long id) {
        log.info("Received request to restore user: {}", id);
        userService.restoreUser(id);
        log.info("User restored successfully: {}", id);
        return ApiResponse.<String>builder()
                .result("User restored successfully")
                .build();
    }
}
