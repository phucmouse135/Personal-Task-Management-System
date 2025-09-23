package org.example.cv.services;

import org.example.cv.models.requests.RoleRequest;
import org.example.cv.models.responses.RoleResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RoleService {
    RoleResponse createRole(RoleRequest request);

    List<RoleResponse> getAllRoles();

    void softdeleteRole(Long id);
}
