package org.example.cv.controllers;

import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.cv.models.requests.AuthenticationRequest;
import org.example.cv.models.requests.IntrospectRequest;
import org.example.cv.models.requests.LogoutRequest;
import org.example.cv.models.requests.RefreshRequest;
import org.example.cv.models.responses.ApiResponse;
import org.example.cv.models.responses.AuthenticationResponse;
import org.example.cv.models.responses.IntrospectResponse;
import org.example.cv.services.AuthenticationService;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

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
    ApiResponse<IntrospectResponse> introspectResponseApiResponse(@RequestBody IntrospectRequest request) {
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
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody RefreshRequest request)
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
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
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
    @PostMapping(value = "/token", consumes = "application/json", produces = "application/json")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    /**
     * Endpoint to authenticate a user via an external provider using an authorization code.
     * @param code the authorization code received from the external provider
     */
    @Operation(summary = "Outbound Authenticate", description = "Authenticate a user via an external provider using an authorization code")
    @PostMapping(value = "/outbound/authentication", consumes = "application/json", produces = "application/json")
    ApiResponse<AuthenticationResponse> outboundAuthenticate(@RequestParam("code") String code) {
        var result = authenticationService.outboundAuthenticate(code);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

}
