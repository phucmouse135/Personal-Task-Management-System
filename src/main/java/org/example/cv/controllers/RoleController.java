package org.example.cv.controllers;

import java.util.List;

import jakarta.validation.Valid;
import org.example.cv.models.requests.RoleRequest;
import org.example.cv.models.responses.ApiResponse;
import org.example.cv.models.responses.RoleResponse;
import org.example.cv.services.RoleService;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Role Management", description = "APIs for managing roles")
public class RoleController {
    RoleService roleService;

    // RoleResponse createRole(RoleRequest request);

    /**
     * Create a new role
     * @param request
     * @return
     */
    @Operation(summary = "Create a new role", description = "Creates a new role with the provided details.")
    @PostMapping
    public ApiResponse<RoleResponse> createRole(@RequestBody @Valid RoleRequest request) {
        log.info("Received request to create role: {}", request);
        RoleResponse roleResponse = roleService.createRole(request);
        log.info("Role created successfully: {}", roleResponse);
        return ApiResponse.<RoleResponse>builder().result(roleResponse).build();
    }

    // List<RoleResponse> getAllRoles();
    /**
     * Retrieve all roles
     * @return
     */
    @Operation(summary = "Get all roles", description = "Retrieves a list of all roles.")
    @GetMapping("/all")
    public ApiResponse<List<RoleResponse>> getAllRoles() {
        log.info("Received request to get all roles");
        java.util.List<RoleResponse> roles = roleService.getAllRoles();
        log.info("Retrieved {} roles", roles.size());
        return ApiResponse.<List<RoleResponse>>builder().result(roles).build();
    }

    //  void softdeleteRole(Long id);
    /**
     * Soft delete a role by ID
     * @param id
     */
    @Operation(summary = "Soft delete a role", description = "Soft deletes a role by its ID.")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> softdeleteRole(@PathVariable Long id) {
        log.info("Received request to soft delete role with ID: {}", id);
        roleService.softdeleteRole(id);
        log.info("Role with ID: {} has been soft deleted", id);
        return ApiResponse.<Void>builder().build();
    }
}
