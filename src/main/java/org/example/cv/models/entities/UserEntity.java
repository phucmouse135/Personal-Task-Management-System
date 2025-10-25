package org.example.cv.models.entities;

import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import org.example.cv.models.entities.base.BaseEntity;
import org.example.cv.utils.userSecurity.Ownable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
@Schema(name = "UserEntity", description = "Entity representing a user")
@NamedEntityGraph(
        name = "UserEntity.roles",
        attributeNodes = {@NamedAttributeNode("roles")})
public class UserEntity extends BaseEntity implements Ownable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the user", example = "1")
    Long id;

    @Column(nullable = false, unique = true, name = "username", length = 50)
    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Username must not exceed 50 characters")
    @Schema(description = "Unique username of the user", example = "john_doe", maxLength = 50)
    String username;

    @Column(nullable = false, name = "password", length = 100)
    @NotBlank(message = "Password is required")
    @Size(max = 100, message = "Password must not exceed 100 characters")
    @Schema(description = "Password of the user", example = "securePassword123", maxLength = 100)
    String password;

    @Column(name = "first_name", length = 100)
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Schema(description = "First name of the user", example = "John", maxLength = 100)
    String firstName;

    @Column(name = "last_name", length = 100)
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Schema(description = "Last name of the user", example = "Doe", maxLength = 100)
    String lastName;

    @Column(name = "email", nullable = false, unique = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "Email address of the user", example = "john.doe@example.com")
    String email;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_name"))
    Set<RoleEntity> roles;

    @Override
    public UserEntity getOwner() {
        return this;
    }
}
