package org.example.cv.utils.userSecurity;

import java.util.Map;

import org.example.cv.exceptions.AppException;
import org.example.cv.exceptions.ErrorCode;
import org.example.cv.repositories.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component("ownershipSecurity")
@RequiredArgsConstructor
public class OwnershipSecurity {
    private final Map<Class<?>, JpaRepository<?, Long>> repositories;
    private final UserRepository userRepository;

    public boolean isOwner(Class<?> entityClass, Authentication authentication, Long entityId) {
        JpaRepository<?, Long> repository = repositories.get(entityClass);
        if (repository == null) {
            throw new AppException(ErrorCode.REPOSITORY_NOT_FOUND);
        }
        return repository
                .findById(entityId)
                .map(entity -> ((Ownable) entity).getOwner().getUsername())
                .map(username -> username.equals(authentication.getName()))
                .orElse(false);
    }

    public boolean isOwner(Long ownerId, Authentication authentication) {
        return userRepository
                .findById(ownerId)
                .map(user -> user.getUsername().equals(authentication.getName()))
                .orElse(false);
    }
}
