package org.example.cv.controllers;

import java.text.ParseException;

import jakarta.validation.Valid;

import org.example.cv.models.requests.AuthenticationRequest;
import org.example.cv.models.requests.GoogleLoginRequest;
import org.example.cv.models.requests.IntrospectRequest;
import org.example.cv.models.requests.LogoutRequest;
import org.example.cv.models.requests.RefreshRequest;
import org.example.cv.models.responses.ApiResponse;
import org.example.cv.models.responses.AuthenticationResponse;
import org.example.cv.models.responses.IntrospectResponse;
import org.example.cv.services.AuthenticationService;
import org.springframework.web.bind.annotation.*;

import com.nimbusds.jose.JOSEException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Authentication", description = "Endpoints for authentication and token management")
public class AuthenticationController {
    AuthenticationService authenticationService;

    /**
     * Endpoint to introspect a token and check its validity.
     *
     * @param request the introspection request containing the token to be checked
     * @return ApiResponse containing the introspection result
     */
    @Operation(summary = "Introspect Token", description = "Check the validity of a given token")
    @PostMapping(value = "/introspect")
    ApiResponse<IntrospectResponse> introspectResponseApiResponse(@RequestBody @Valid IntrospectRequest request) {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    /**
     * Endpoint to refresh an existing authentication token.
     *
     * @param request the refresh request containing the refresh token
     * @return ApiResponse containing the new authentication token
     * @throws ParseException if there is an error parsing the token
     * @throws JOSEException  if there is an error with JOSE processing
     */
    @Operation(summary = "Refresh Token", description = "Refresh an existing authentication token")
    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody @Valid RefreshRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    /**
     * Endpoint to log out a user by invalidating their token.
     *
     * @param request the logout request containing the token to be invalidated
     * @return ApiResponse indicating the success of the logout operation
     * @throws ParseException if there is an error parsing the token
     * @throws JOSEException  if there is an error with JOSE processing
     */
    @Operation(summary = "Logout", description = "Log out a user by invalidating their token")
    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody @Valid LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

    /**
     * Endpoint to authenticate a user and generate an authentication token.
     *
     * @param request the authentication request containing user credentials
     * @return ApiResponse containing the authentication token
     */
    @Operation(summary = "Authenticate", description = "Authenticate a user and generate an authentication token")
    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody @Valid AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }
    /**
     * Endpoint to authenticate a user via Google ID Token (for React frontend).
     *
     * @param request the Google login request containing the ID token
     * @return ApiResponse containing the authentication token
     */
    @Operation(summary = "Google Login", description = "Authenticate a user using Google ID Token")
    @PostMapping("/google")
    ApiResponse<AuthenticationResponse> googleLogin(@RequestBody @Valid GoogleLoginRequest request) {
        var result = authenticationService.authenticateWithGoogle(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }
}
