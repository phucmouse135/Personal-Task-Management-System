package org.example.cv.models.responses;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "UserResponse", description = "Response object for user details")
public class UserResponse {

    @Schema(description = "Unique identifier of the user", example = "1")
    Long id;

    @Schema(description = "Unique username of the user", example = "john_doe")
    String username;

    @Schema(description = "First name of the user", example = "John")
    String firstName;

    @Schema(description = "Last name of the user", example = "Doe")
    String lastName;

    @Schema(description = "Email address of the user", example = "john.doe@example.com")
    String email;

    @Schema(description = "Set of roles associated with the user", example = "[\"ADMIN\", \"USER\"]")
    Set<RoleResponse> roles;
}
