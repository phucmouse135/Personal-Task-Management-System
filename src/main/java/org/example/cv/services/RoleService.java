package org.example.cv.services;

import java.util.List;

import org.example.cv.models.requests.RoleRequest;
import org.example.cv.models.responses.RoleResponse;
import org.springframework.stereotype.Service;

@Service
public interface RoleService {
    RoleResponse createRole(RoleRequest request);

    List<RoleResponse> getAllRoles();

    void softdeleteRole(String id);
}
