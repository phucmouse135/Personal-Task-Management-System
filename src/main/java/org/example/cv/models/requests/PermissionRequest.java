package org.example.cv.models.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.cv.models.requests.validationgroups.OnCreate;
import org.example.cv.models.requests.validationgroups.OnUpdate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "PermissionRequest", description = "Request object for permissions")
public class PermissionRequest {

    @NotBlank(message = "Permission name is required", groups = {OnCreate.class, OnUpdate.class})
    @Size(max = 50, message = "Permission name must not exceed 50 characters", groups = {OnCreate.class, OnUpdate.class})
    @Schema(description = "Name of the permission", example = "READ_PRIVILEGES", maxLength = 50, required = true)
    String name;

    @Size(max = 255, message = "Description must not exceed 255 characters", groups = {OnCreate.class, OnUpdate.class})
    @Schema(description = "Description of the permission", example = "Allows reading privileges", maxLength = 255)
    String description;
}