package org.example.cv.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Date;

import org.example.cv.controllers.AuthenticationController;
import org.example.cv.exceptions.GlobalExceptionHandler;
import org.example.cv.models.requests.AuthenticationRequest;
import org.example.cv.models.requests.IntrospectRequest;
import org.example.cv.models.requests.LogoutRequest;
import org.example.cv.models.requests.RefreshRequest;
import org.example.cv.models.responses.AuthenticationResponse;
import org.example.cv.models.responses.IntrospectResponse;
import org.example.cv.services.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;

@ExtendWith(MockitoExtension.class)
@TestPropertySource("./test.properties")
class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                .setControllerAdvice(new GlobalExceptionHandler()) // thêm dòng này
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testIntrospect_Success() throws Exception {
        IntrospectRequest request =
                IntrospectRequest.builder().token("valid-token").build();
        IntrospectResponse response = IntrospectResponse.builder().valid(true).build();

        when(authenticationService.introspect(any(IntrospectRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/introspect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.valid").value(true));

        verify(authenticationService, times(1)).introspect(any(IntrospectRequest.class));
    }

    @Test
    void testIntrospect_InvalidToken() throws Exception {
        IntrospectRequest request = IntrospectRequest.builder().token(null).build();

        mockMvc.perform(post("/auth/introspect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).introspect(any(IntrospectRequest.class));
    }

    @Test
    void testRefreshToken_Success() throws Exception {
        RefreshRequest request = RefreshRequest.builder().token("refresh-token").build();
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("new-access-token")
                .expiryTime(new Date())
                .build();

        when(authenticationService.refreshToken(any(RefreshRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.token").value("new-access-token"));

        verify(authenticationService, times(1)).refreshToken(any(RefreshRequest.class));
    }

    @Test
    void testRefreshToken_ThrowsJOSEException() throws Exception {
        RefreshRequest request = RefreshRequest.builder().token("invalid-token").build();

        when(authenticationService.refreshToken(any(RefreshRequest.class))).thenThrow(new JOSEException("JOSE error"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, times(1)).refreshToken(any(RefreshRequest.class));
    }

    @Test
    void testLogout_Success() throws Exception {
        LogoutRequest request = LogoutRequest.builder().token("valid-token").build();

        doNothing().when(authenticationService).logout(any(LogoutRequest.class));

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").doesNotExist());

        verify(authenticationService, times(1)).logout(any(LogoutRequest.class));
    }

    @Test
    void testLogout_ThrowsJOSEException() throws Exception {
        LogoutRequest request = LogoutRequest.builder().token("invalid-token").build();

        doThrow(new JOSEException("JOSE error")).when(authenticationService).logout(any(LogoutRequest.class));

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, times(1)).logout(any(LogoutRequest.class));
    }

    @Test
    void testAuthenticate_Success() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username("john_doe")
                .password("securePassword123")
                .build();
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("access-token")
                .expiryTime(new Date())
                .build();

        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.token").value("access-token"));

        verify(authenticationService, times(1)).authenticate(any(AuthenticationRequest.class));
    }

    @Test
    void testHandleGoogleCallback_Success() throws Exception {
        mockMvc.perform(get("/auth/login/oauth2/code/google")
                        .param("code", "test-code")
                        .param("state", "test-state"))
                .andExpect(status().isOk())
                .andExpect(content().string("Authorization Code: test-code"));
    }
}
