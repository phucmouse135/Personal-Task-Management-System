package org.example.cv.models.entities.base;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
@MappedSuperclass
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "BaseEntity", description = "Base entity with common fields")
public abstract class BaseEntity {
    @CreationTimestamp
    @Column(name = "created_at")
    @Schema(description = "Creation timestamp", example = "2023-10-01T12:00:00Z")
    private Instant createdAt;

    @UpdateTimestamp // Tự động gán giá trị ngày cập nhật
    @Column(name = "updated_at")
    @Schema(description = "Last update timestamp", example = "2023-10-02T15:30:00Z")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    @Schema(description = "Deletion timestamp", example = "2023-10-03T10:20:00Z", nullable = true)
    private Instant deletedAt;
}
