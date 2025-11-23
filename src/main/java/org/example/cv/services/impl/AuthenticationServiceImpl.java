package org.example.cv.services.impl;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import jakarta.mail.MessagingException;

import org.example.cv.exceptions.AppException;
import org.example.cv.exceptions.ErrorCode;
import org.example.cv.models.entities.InvalidatedToken;
import org.example.cv.models.entities.RoleEntity;
import org.example.cv.models.entities.UserEntity;
import org.example.cv.models.requests.*;
import org.example.cv.models.responses.AuthenticationResponse;
import org.example.cv.models.responses.IntrospectResponse;
import org.example.cv.repositories.InvalidedTokenRepository;
import org.example.cv.repositories.UserRepository;
import org.example.cv.services.AuthenticationService;
import org.example.cv.services.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {
    UserRepository userRepository;
    InvalidedTokenRepository invalidedTokenRepository;
    EmailService emailService;
    static String EMAIL = "email";

    @NonFinal
    @Value("${outbound.google.client-id}")
    protected String CLIENT_ID;

    @NonFinal
    @Value("${outbound.google.client-secret}")
    protected String CLIENT_SECRET;

    @NonFinal
    @Value("${outbound.google.redirect-uri}")
    protected String REDIRECT_URI;

    @NonFinal
    protected final String GRANT_TYPE = "authorization_code";

    @NonFinal
    @Value("${jwt.secret}")
    protected String secret;

    @NonFinal
    @Value("${jwt.expiration}")
    protected Long jwtExpiration; // in minutes

    /**
     * Authenticate user with OAuth2
     * If user not exist, create new user
     * @param oAuth2User
     * @return
     */
    @Override
    public AuthenticationResponse outboundAuthenticate(OAuth2User oAuth2User) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var userInfo = oAuth2User.getAttributes();
        log.info("User info: {}", userInfo);

        var user = userRepository.findByUsername((String) userInfo.get(EMAIL)).orElseGet(() -> {
            log.info("User not found, creating new user");
            HashSet<RoleEntity> roles = new HashSet<>();
            roles.add(RoleEntity.builder().name("USER").build());
            String password = UUID.randomUUID().toString();
            var newUser = UserEntity.builder()
                    .username((String) userInfo.get(EMAIL))
                    .password(passwordEncoder.encode(password)) // No password for OAuth2 users
                    .roles(roles)
                    .email((String) userInfo.get(EMAIL))
                    .build();
            var savedUser = userRepository.save(newUser);
            try {
                emailService.sendHtmlMessage(
                        userInfo.get("email").toString(),
                        "Welcome to Our Service",
                        "<h1>Welcome to Our Service</h1>" + "<p>Your account has been created successfully.</p>"
                                + "<p>Your temporary password is: <strong>"
                                + password + "</strong></p>" + "<p>Please change your password after logging in.</p>");
            } catch (MessagingException e) {
                throw new AppException(ErrorCode.EMAIL_SENDING_FAILED);
            }
            log.info("Created new user: {}", savedUser.getId());
            return savedUser;
        });
        var tokenInfo = generateToken(user);
        return AuthenticationResponse.builder()
                .token(tokenInfo.token)
                .expiryTime(tokenInfo.expiryDate)
                .build();
    }

    /**
     * Introspect token
     * @param request
     * @return
     */
    @Override
    public IntrospectResponse introspect(IntrospectRequest request) {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token);
        } catch (Exception e) {
            isValid = false;
        }
        return new IntrospectResponse(isValid);
    }

    /**
     * Authenticate user with username and password
     * @param request
     * @return
     */
    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        log.info("Authenticating user: {}", request.getUsername());
        UserEntity user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);
        TokenInfo tokenInfo = generateToken(user);
        return new AuthenticationResponse(tokenInfo.token, tokenInfo.expiryDate);
    }

    /**
     * Logout user by invalidating token
     * @param request
     */
    @Override
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        var signToken = verifyToken(request.getToken());

        String jit = signToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

        invalidedTokenRepository.save(invalidatedToken);
    }

    /**
     * Refresh token
     * @param request
     * @return
     */
    @Override
    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        log.info("Refreshing token");
        var signedJWT = verifyToken(request.getToken());

        String jit = signedJWT.getJWTClaimsSet().getJWTID();
        UserEntity user = userRepository
                .findByUsername(signedJWT.getJWTClaimsSet().getSubject())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();
        invalidedTokenRepository.save(invalidatedToken);

        log.info("Generating new token for user: {}", user.getUsername());
        TokenInfo tokenInfo = generateToken(user);
        return new AuthenticationResponse(tokenInfo.token, tokenInfo.expiryDate);
    }

    /**
     * Generate JWT token
     * @param user
     * @return
     */
    private TokenInfo generateToken(UserEntity user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);

        Date issueTime = new Date();
        Date expiryTime = new Date(Instant.ofEpochMilli(issueTime.getTime())
                .plus(1, ChronoUnit.HOURS)
                .toEpochMilli());

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("example.com")
                .issueTime(issueTime)
                .expirationTime(expiryTime)
                .jwtID(UUID.randomUUID().toString())
                .claim("userId", user.getId())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(claimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try {
            jwsObject.sign(new MACSigner(secret.getBytes()));
            return new TokenInfo(jwsObject.serialize(), expiryTime);
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    /**
     * Authenticate user with Google ID Token
     * @param request containing Google ID token from frontend
     * @return AuthenticationResponse with JWT token
     */
    @Override
    public AuthenticationResponse authenticateWithGoogle(GoogleLoginRequest request) {
        try {
            log.info("Authenticating with Google ID Token");

            // Verify Google ID token
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                            new NetHttpTransport(), new GsonFactory())
                    .setAudience(java.util.Collections.singletonList(CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getCredential());

            if (idToken == null) {
                log.error("Invalid Google ID token");
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            // Extract user info from Google token
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            log.info("Google user authenticated: email={}, name={}", email, name);

            // Find or create user
            UserEntity user = userRepository.findByEmail(email).orElseGet(() -> {
                try {
                    return createGoogleUser(email, name);
                } catch (MessagingException e) {
                    log.error("Failed to create user from Google account", e);
                    throw new AppException(ErrorCode.EMAIL_SENDING_FAILED);
                }
            });

            // Generate JWT token
            TokenInfo tokenInfo = generateToken(user);
            return new AuthenticationResponse(tokenInfo.token, tokenInfo.expiryDate);

        } catch (Exception e) {
            log.error("Google authentication failed", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    /**
     * Create new user from Google account
     * @param email User email from Google
     * @param name User full name from Google
     * @return Created user entity
     */
    private UserEntity createGoogleUser(String email, String name) throws MessagingException {
        log.info("Creating new user from Google account: {}", email);

        UserEntity newUser = new UserEntity();
        newUser.setEmail(email);
        newUser.setFirstName(name);
        newUser.setUsername(email); // Use email as username
        String password = UUID.randomUUID().toString();
        newUser.setPassword(
                new BCryptPasswordEncoder(10).encode(UUID.randomUUID().toString())); // Random password
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(RoleEntity.builder().name("USER").build());
        newUser.setRoles(roles);

        emailService.sendHtmlMessage(
                email,
                "Welcome to Our Service",
                "<h1>Welcome to Our Service</h1>" + "<p>Your account has been created successfully.</p>"
                        + "<p>Your temporary password is: <strong>"
                        + password + "</strong></p>" + "<p>Please change your password after logging in.</p>");

        return userRepository.save(newUser);
    }
    /**
     * Verify token
     * @param token
     * @return
     * @throws JOSEException
     * @throws java.text.ParseException
     */
    private SignedJWT verifyToken(String token) throws JOSEException, java.text.ParseException {
        JWSVerifier verifier = new MACVerifier(secret.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);
        if (!(verified && expirationTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);
        if (invalidedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    /**
     * Build scope string from user roles
     * @param user
     * @return
     */
    private String buildScope(UserEntity user) {
        StringJoiner scope = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> scope.add("ROLE_" + role.getName()));
        }
        return scope.toString();
    }

    // Record to hold token and expiry date
    private record TokenInfo(String token, Date expiryDate) {}
}
