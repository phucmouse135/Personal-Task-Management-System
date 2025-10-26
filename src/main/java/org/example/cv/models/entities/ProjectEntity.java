package org.example.cv.models.entities;

import jakarta.persistence.*;

import org.example.cv.models.entities.base.BaseEntity;
import org.example.cv.utils.userSecurity.Ownable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "ProjectEntity", description = "Entity representing a project")
@NamedEntityGraph(name = "ProjectEntity.owner",
        attributeNodes = {
                @NamedAttributeNode("owner"),
                @NamedAttributeNode("members")
        }
)
public class ProjectEntity extends BaseEntity implements Ownable {

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
    @JoinColumn(name = "owner_id")
    @Schema(description = "Owner of the project")
    UserEntity owner;

    // project members
    @ManyToMany
    @JoinTable(
        name = "project_members",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Schema(description = "Members of the project")
    java.util.Set<UserEntity> members;
}
