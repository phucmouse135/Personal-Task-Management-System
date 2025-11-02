package org.example.cv.models.responses;

import java.time.Instant;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

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

    @Schema(description = "Owner of the project")
    UserResponse owner;

    @Schema(description = "Project end date", example = "2024-12-31T23:59:59Z")
    Instant endDate;

    @Schema(description = "Members of the project")
    Set<UserResponse> members;

    @Schema(description = "Timestamp when the project was created", example = "2023-10-01T12:00:00Z")
    Instant createdAt;

    @Schema(description = "Timestamp when the project was last updated", example = "2023-10-02T12:00:00Z")
    Instant updatedAt;

    @Schema(
            description = "Timestamp when the project was soft deleted",
            example = "2023-10-03T10:20:00Z",
            nullable = true)
    Instant deletedAt;
}
