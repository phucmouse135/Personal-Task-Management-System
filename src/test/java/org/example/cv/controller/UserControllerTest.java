package org.example.cv.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.example.cv.controllers.UserController;
import org.example.cv.models.requests.UserRequest;
import org.example.cv.models.responses.UserResponse;
import org.example.cv.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateUser() throws Exception {
        UserRequest request = UserRequest.builder()
                .username("john_doe")
                .password("securePassword123")
                .email("john.doe@example.com")
                .build();

        UserResponse response = UserResponse.builder()
                .id(1L)
                .username("john_doe")
                .email("john.doe@example.com")
                .roles(Collections.emptySet())
                .build();

        Mockito.when(userService.createUser(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.username").value("john_doe"))
                .andExpect(jsonPath("$.result.id").value(1));
    }

    @Test
    void testGetUserById() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(1L)
                .username("john_doe")
                .email("john.doe@example.com")
                .build();

        Mockito.when(userService.getUserById(1L)).thenReturn(response);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.username").value("john_doe"))
                .andExpect(jsonPath("$.result.email").value("john.doe@example.com"));
    }

    @Test
    void testUpdateUser() throws Exception {
        UserRequest request = UserRequest.builder()
                .username("updated_user")
                .email("updated@example.com")
                .build();

        UserResponse response = UserResponse.builder()
                .id(1L)
                .username("updated_user")
                .email("updated@example.com")
                .build();

        Mockito.when(userService.updateUser(eq(1L), any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.username").value("updated_user"))
                .andExpect(jsonPath("$.result.email").value("updated@example.com"));
    }

    @Test
    void testSoftDeleteUser() throws Exception {
        Mockito.doNothing().when(userService).softdeleteUser(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("User soft deleted successfully"));
    }

    @Test
    void testRestoreUser() throws Exception {
        Mockito.doNothing().when(userService).restoreUser(1L);

        mockMvc.perform(post("/users/1/restore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("User restored successfully"));
    }
}
