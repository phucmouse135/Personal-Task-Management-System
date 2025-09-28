package org.example.cv.utils.userSecurity;

import org.example.cv.models.entities.UserEntity;

public interface Ownable {
    UserEntity getOwner();
}
