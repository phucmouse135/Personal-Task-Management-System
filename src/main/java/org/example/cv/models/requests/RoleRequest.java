package org.example.cv.models.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.example.cv.models.requests.validationgroups.OnCreate;
import org.example.cv.models.requests.validationgroups.OnUpdate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "RoleRequest", description = "Request object for roles")
public class RoleRequest {

    @NotBlank(
            message = "Role name is required",
            groups = {OnCreate.class, OnUpdate.class})
    @Size(
            max = 50,
            message = "Role name must not exceed 50 characters",
            groups = {OnCreate.class, OnUpdate.class})
    @Schema(description = "Name of the role", example = "ADMIN", maxLength = 50)
    String name;

    @Size(
            max = 255,
            message = "Description must not exceed 255 characters",
            groups = {OnCreate.class, OnUpdate.class})
    @Schema(description = "Description of the role", example = "Administrator role with full access", maxLength = 255)
    String description;
}
