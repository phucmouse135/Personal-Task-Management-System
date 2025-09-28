package org.example.cv.models.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "ProjectResponse", description = "Response object for project details")
public class ProjectResponse {

    @Schema(description = "Unique identifier of the project", example = "1")
    Long id;

    @Schema(description = "Name of the project", example = "Project Alpha")
    String name;

    @Schema(description = "Description of the project", example = "This is a sample project.")
    String description;

    @Schema(description = "Owner ID of the project", example = "1")
    Long ownerId;

    @Schema(description = "Timestamp when the project was created", example = "2023-10-01T12:00:00Z")
    Instant createdAt;

    @Schema(description = "Timestamp when the project was last updated", example = "2023-10-02T12:00:00Z")
    Instant updatedAt;
}