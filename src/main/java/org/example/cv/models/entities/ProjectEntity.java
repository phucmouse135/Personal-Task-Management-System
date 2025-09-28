package org.example.cv.models.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.cv.models.entities.base.BaseEntity;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "ProjectEntity", description = "Entity representing a project")
public class ProjectEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the project", example = "1")
    Long id;

    @Column(nullable = false, length = 100)
    @Schema(description = "Name of the project", example = "Project Alpha", maxLength = 100)
    String name;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Description of the project", example = "This is a sample project.")
    String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", insertable = false, updatable = false)
    @Schema(description = "Owner of the project")
    UserEntity owner;
}