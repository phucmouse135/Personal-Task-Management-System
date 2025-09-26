package org.example.cv.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.cv.models.entities.base.BaseEntity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "roles")
@Schema(name = "RoleEntity", description = "Entity representing a user role")
public class RoleEntity extends BaseEntity {
    @Id
    @NotBlank(message = "Role name is required")
    @Size(max = 50, message = "Role name must not exceed 50 characters")
    @Schema(description = "Unique name of the role", example = "ADMIN", maxLength = 50)
    private String name;

    @Column(nullable = true)
    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Schema(description = "Description of the role", example = "Administrator role with full access", maxLength = 255)
    private String description;
}
