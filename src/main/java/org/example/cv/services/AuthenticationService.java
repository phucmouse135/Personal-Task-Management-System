package org.example.cv.services;

import com.nimbusds.jose.JOSEException;
import org.example.cv.models.requests.AuthenticationRequest;
import org.example.cv.models.requests.IntrospectRequest;
import org.example.cv.models.requests.LogoutRequest;
import org.example.cv.models.requests.RefreshRequest;
import org.example.cv.models.responses.AuthenticationResponse;
import org.example.cv.models.responses.IntrospectResponse;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Service
public interface AuthenticationService {
    IntrospectResponse introspect(IntrospectRequest request);

    AuthenticationResponse authenticate(AuthenticationRequest request);

    void logout(LogoutRequest request) throws ParseException, JOSEException;

    AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException;
}
