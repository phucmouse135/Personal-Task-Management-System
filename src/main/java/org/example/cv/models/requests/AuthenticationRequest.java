package org.example.cv.models.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
@Schema(name = "AuthenticationRequest", description = "Request object for user authentication")
public class AuthenticationRequest {

    @NotBlank(
            message = "Username is required",
            groups = {OnCreate.class})
    @Size(
            max = 50,
            message = "Username must not exceed 50 characters",
            groups = {OnCreate.class, OnUpdate.class})
    @Schema(description = "Username of the user", example = "john_doe", maxLength = 50)
    String username;

    @NotBlank(
            message = "Password is required",
            groups = {OnCreate.class, OnUpdate.class})
    @Size(
            max = 100,
            message = "Password must not exceed 100 characters",
            groups = {OnCreate.class})
    @Schema(description = "Password of the user", example = "securePassword123", maxLength = 100)
    String password;
}
