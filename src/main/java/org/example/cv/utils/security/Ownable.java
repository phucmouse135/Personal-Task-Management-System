package org.example.cv.utils.security;

import org.example.cv.models.entities.UserEntity;

public interface Ownable {
    UserEntity getOwner();
}
