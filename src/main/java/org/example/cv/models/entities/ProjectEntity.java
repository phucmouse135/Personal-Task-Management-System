package org.example.cv.models.entities;

import java.time.Instant;

import jakarta.persistence.*;

import org.example.cv.event.AuditLogListener;
import org.example.cv.event.Auditable;
import org.example.cv.models.entities.base.BaseEntity;
import org.example.cv.utils.security.Ownable;
import org.hibernate.annotations.BatchSize;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "ProjectEntity", description = "Entity representing a project")
@NamedEntityGraph(
        name = "ProjectEntity.owner",
        attributeNodes = {@NamedAttributeNode("owner"), @NamedAttributeNode("members")})
@EntityListeners(AuditLogListener.class)
public class ProjectEntity extends BaseEntity implements Ownable, Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the project", example = "1")
    private Long id;

    @Column(nullable = false, length = 100)
    @Schema(description = "Name of the project", example = "Project Alpha", maxLength = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Description of the project", example = "This is a sample project.")
    private String description;

    // project end date
    @Column(name = "end_date")
    @Schema(description = "End date of the project", example = "2024-12-31T23:59:59")
    private Instant endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @Schema(description = "Owner of the project")
    private UserEntity owner;

    // project members
    @ManyToMany
    @JoinTable(
            name = "project_members",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @Schema(description = "Members of the project")
    @BatchSize(size = 50)
    private java.util.Set<UserEntity> members;

    @Override
    public String getEntityType() {
        return "PROJECT";
    }
}
