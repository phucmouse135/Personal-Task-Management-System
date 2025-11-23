package org.example.cv.models.entities;

import java.time.Instant;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.example.cv.constants.TaskPriority;
import org.example.cv.constants.TaskStatus;
import org.example.cv.event.AuditLogListener;
import org.example.cv.event.Auditable;
import org.example.cv.models.entities.base.BaseEntity;
import org.hibernate.annotations.BatchSize;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tasks")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@NamedEntityGraph(
        name = "TaskEntity.projectAssignee",
        attributeNodes = {@NamedAttributeNode("project"), @NamedAttributeNode("assignees")})
@EntityListeners(AuditLogListener.class)
public class TaskEntity extends BaseEntity implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, name = "deadline")
    private Instant deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.TODO; // Mặc định

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority = TaskPriority.MEDIUM; // Mặc định

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @ManyToMany
    @JoinTable(
            name = "task_assignees",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @BatchSize(size = 50)
    private Set<UserEntity> assignees;

    @Override
    public String getEntityType() {
        return "TASK";
    }
}
