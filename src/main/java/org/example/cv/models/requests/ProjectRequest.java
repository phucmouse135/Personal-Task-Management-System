package org.example.cv.models.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.cv.models.requests.validationgroups.OnCreate;
import org.example.cv.models.requests.validationgroups.OnUpdate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "ProjectRequest", description = "Request object for creating or updating a project")
public class ProjectRequest {

    @NotBlank(message = "Project name is required", groups = {OnCreate.class, OnUpdate.class})
    @Size(max = 100, message = "Project name must not exceed 100 characters", groups = {OnCreate.class, OnUpdate.class})
    @Schema(description = "Name of the project", example = "Project Alpha", maxLength = 100, required = true)
    String name;

    @Schema(description = "Description of the project", example = "This is a sample project.", maxLength = 1000)
    @Size(max = 1000, message = "Description must not exceed 1000 characters", groups = {OnCreate.class, OnUpdate.class})
    String description;

    @Schema(description = "Owner ID of the project", example = "1", required = true)
    @NotNull(message = "Owner ID is required", groups = {OnCreate.class, OnUpdate.class})
    Long ownerId;
}