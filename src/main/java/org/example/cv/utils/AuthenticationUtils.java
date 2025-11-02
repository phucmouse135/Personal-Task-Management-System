package org.example.cv.utils;

import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationUtils {
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        log.debug("Current user principal is {}", principal);
        if (principal instanceof Jwt jwt) {
            return Long.parseLong(jwt.getClaimAsString("userId"));
        }

        if (principal instanceof OAuth2User) {
            return Long.parseLong(Objects.requireNonNull(((OAuth2User) principal).getAttribute("userId")));
        }
        log.warn("No authenticated user found in security context");
        return null;
    }
}
