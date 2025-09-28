package org.example.cv.services.impl;

import java.util.List;

import jakarta.transaction.Transactional;

import org.example.cv.models.entities.RoleEntity;
import org.example.cv.models.requests.RoleRequest;
import org.example.cv.models.responses.RoleResponse;
import org.example.cv.repositories.RoleRepository;
import org.example.cv.services.RoleService;
import org.example.cv.utils.mapper.RoleMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleServiceImpl implements RoleService {
    RoleMapper roleMapper;
    RoleRepository roleRepository;

    /**
     * Create role
     * @param request
     * @return
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse createRole(RoleRequest request) {
        log.info("Creating role: {}", request.getName());
        RoleEntity role = roleMapper.toEntity(request);
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    /**
     * Get all roles
     * @return
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<RoleResponse> getAllRoles() {
        log.info("Getting all roles");
        return roleRepository.findAll().stream().map(roleMapper::toResponse).toList();
    }

    /**
     * Soft delete role by id
     * @param id
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void softdeleteRole(String id) {
        log.info("Soft deleting role by id: {}", id);
        roleRepository.softDeleteByIds(List.of(id));
    }
}
