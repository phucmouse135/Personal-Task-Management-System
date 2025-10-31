package org.example.cv.configuration;

import org.example.cv.models.entities.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetail implements UserDetails {
    private final UserEntity userEntity;

    public CustomUserDetail(UserEntity userEntity) {
        this.userEntity = userEntity;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userEntity.getRoles().stream()
                .map(role -> (GrantedAuthority) ("ROLE_" + role.getName())::toString)
                .toList();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return userEntity.getUsername();
    }

    public Long getId() {
        return userEntity.getId();
    }
}
