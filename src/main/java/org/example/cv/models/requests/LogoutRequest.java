package org.example.cv.models.requests;

import jakarta.validation.constraints.NotBlank;

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
@Schema(name = "LogoutRequest", description = "Request object for user logout")
public class LogoutRequest {

    @NotBlank(
            message = "Token is required",
            groups = {OnCreate.class, OnUpdate.class})
    @Schema(
            description = "The token to be invalidated during logout",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            required = true)
    String token;
}
