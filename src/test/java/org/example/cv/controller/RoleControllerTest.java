package org.example.cv.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.example.cv.controllers.RoleController;
import org.example.cv.models.requests.RoleRequest;
import org.example.cv.models.responses.RoleResponse;
import org.example.cv.services.RoleService;
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

@ExtendWith(MockitoExtension.class)
@TestPropertySource("./test.properties")
class RoleControllerTest {

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roleController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateRole() throws Exception {
        // Arrange
        RoleRequest request = new RoleRequest();
        RoleResponse response = new RoleResponse();
        when(roleService.createRole(any(RoleRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());

        verify(roleService, times(1)).createRole(any(RoleRequest.class));
    }

    @Test
    void testGetAllRoles() throws Exception {
        // Arrange
        List<RoleResponse> roles = List.of(new RoleResponse());
        when(roleService.getAllRoles()).thenReturn(roles);

        // Act & Assert
        mockMvc.perform(get("/roles/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(roles.size()));

        verify(roleService, times(1)).getAllRoles();
    }

    @Test
    void testSoftDeleteRole() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/roles/1")).andExpect(status().isOk());

        verify(roleService, times(1)).softdeleteRole("USER");
    }
}
