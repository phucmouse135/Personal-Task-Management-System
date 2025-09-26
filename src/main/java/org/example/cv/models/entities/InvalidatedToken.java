package org.example.cv.models.entities;

import java.util.Date;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import org.example.cv.models.entities.base.BaseEntity;
import org.example.cv.models.requests.validationgroups.OnCreate;
import org.example.cv.models.requests.validationgroups.OnUpdate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "invalidated_tokens")
@Schema(name = "InvalidatedToken", description = "Entity representing an invalidated token")
public class InvalidatedToken extends BaseEntity {

    @Id
    @Schema(description = "Unique identifier of the invalidated token", example = "1")
    String id;

    @NotNull(
            message = "Expiry time is required",
            groups = {OnCreate.class, OnUpdate.class})
    @Future(
            message = "Expiry time must be in the future",
            groups = {OnCreate.class, OnUpdate.class})
    @Column(nullable = false, unique = true)
    @Schema(description = "Expiry time of the invalidated token", example = "2023-12-31T23:59:59Z", required = true)
    Date expiryTime;
}
