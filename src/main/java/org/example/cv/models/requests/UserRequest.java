package org.example.cv.models.requests;

import java.util.Set;

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
@Schema(name = "UserRequest", description = "Request object for user operations")
public class UserRequest {

    @NotBlank(
            message = "Username is required",
            groups = {OnCreate.class, OnUpdate.class})
    @Size(
            max = 50,
            message = "Username must not exceed 50 characters",
            groups = {OnCreate.class, OnUpdate.class})
    @Schema(description = "Unique username of the user", example = "john_doe", maxLength = 50, required = true)
    String username;

    @NotBlank(
            message = "Password is required",
            groups = {OnCreate.class})
    @Size(
            max = 100,
            message = "Password must not exceed 100 characters",
            groups = {OnCreate.class})
    @Schema(description = "Password of the user", example = "securePassword123", maxLength = 100, required = true)
    String password;

    @Size(
            max = 100,
            message = "First name must not exceed 100 characters",
            groups = {OnCreate.class, OnUpdate.class})
    @Schema(description = "First name of the user", example = "John", maxLength = 100)
    String firstName;

    @Size(
            max = 100,
            message = "Last name must not exceed 100 characters",
            groups = {OnCreate.class, OnUpdate.class})
    @Schema(description = "Last name of the user", example = "Doe", maxLength = 100)
    String lastName;

    @NotBlank(
            message = "Email is required",
            groups = {OnCreate.class, OnUpdate.class})
    @Email(
            message = "Email should be valid",
            groups = {OnCreate.class, OnUpdate.class})
    @Schema(description = "Email address of the user", example = "john.doe@example.com", required = true)
    String email;

    @Schema(description = "Set of role IDs associated with the user", example = "[1, 2, 3]")
    Set<String> roleIds;
}
