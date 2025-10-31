package org.example.cv.configuration;

import java.io.IOException;
import java.text.ParseException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.example.cv.exceptions.AppException;
import org.example.cv.exceptions.ErrorCode;
import org.example.cv.models.requests.IntrospectRequest;
import org.example.cv.services.AuthenticationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nimbusds.jwt.SignedJWT;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private CustomUserDetailService customUserDetailService;
    private AuthenticationService authenticationService;

    JwtAuthenticationFilter(@Lazy AuthenticationService authenticationService,
            CustomUserDetailService customUserDetailService ) {
        this.authenticationService = authenticationService;
        this.customUserDetailService = customUserDetailService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String username = null;
        try {
            username = SignedJWT.parse(token).getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            CustomUserDetail userDetails = (CustomUserDetail) customUserDetailService.loadUserByUsername(username);
            if (authenticationService.introspect(new IntrospectRequest(token)).isValid()) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                // ✅ Đưa thông tin user vào SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
