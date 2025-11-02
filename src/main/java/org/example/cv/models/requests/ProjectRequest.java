package org.example.cv.models.requests;

import jakarta.validation.constraints.*;

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
@Schema(name = "ProjectRequest", description = "Request object for creating or updating a project")
public class ProjectRequest {

    @NotBlank(
            message = "Project name is required",
            groups = {OnCreate.class, OnUpdate.class})
    @Size(
            max = 100,
            message = "Project name must not exceed 100 characters",
            groups = {OnCreate.class, OnUpdate.class})
    @Schema(description = "Name of the project", example = "Project Alpha", maxLength = 100, required = true)
    String name;

    @Schema(description = "Description of the project", example = "This is a sample project.", maxLength = 1000)
    @Size(
            max = 1000,
            message = "Description must not exceed 1000 characters",
            groups = {OnCreate.class, OnUpdate.class})
    String description;

    @Schema(description = "End date of the project in ISO 8601 format", example = "2024-12-31T23:59:59Z")
    String endDate;

    //    @Schema(description = "Owner ID of the project", example = "1")
    //    @NotNull(
    //            message = "Owner ID is required",
    //            groups = {OnUpdate.class})
    //    Long ownerId;
}
