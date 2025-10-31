package org.example.cv.services.impl;

import java.util.Collections;
import java.util.HashSet;

import jakarta.transaction.Transactional;

import org.example.cv.exceptions.AppException;
import org.example.cv.exceptions.ErrorCode;
import org.example.cv.models.entities.ProjectEntity;
import org.example.cv.models.entities.UserEntity;
import org.example.cv.models.requests.ProjectRequest;
import org.example.cv.models.responses.ProjectResponse;
import org.example.cv.repositories.ProjectRepository;
import org.example.cv.repositories.UserRepository;
import org.example.cv.services.ProjectService;
import org.example.cv.utils.mapper.ProjectMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ProjectServiceImpl implements ProjectService {
    ProjectRepository projectRepository;
    ProjectMapper projectMapper;
    UserRepository userRepository;

    /**
     * Get all projects with pagination, sorting, and filtering.
     * @param page
     * @param size
     * @param sortBy
     * @param sortDir
     * @param filter
     * @return
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(
            value = "project-list",
            key = "'all_'+#page+'_'+#size+'_'+#sortBy+'_'+#sortDir+'_'+#filter",
            cacheManager = "redisCacheManager")
    @Override
    public Page<ProjectResponse> getAll(int page, int size, String sortBy, String sortDir, String filter) {
        log.info("Getting all projects");
        Pageable pageable = PageRequest.of(page, size).withSort(Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        return projectRepository.findAll(pageable, filter).map(projectMapper::toResponse);
    }

    /**
     * Get project by id.
     * @param id
     * @return
     */
    @Override
    @Cacheable(value = "project-detail", key = "'project_'+#id", cacheManager = "compositeCacheManager")
    @PostAuthorize("hasRole('ADMIN') or returnObject.owner.username == authentication.name")
    public ProjectResponse getById(Long id) {
        log.info("Getting project by id: {}", id);
        ProjectEntity project =
                projectRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));
        return projectMapper.toResponse(project);
    }

    /**
     * Create a new project.
     * @param request
     * @return
     */
    @Transactional
    @Override
    @CacheEvict(
            value = {"project-list"},
            allEntries = true,
            cacheManager = "redisCacheManager")
    public ProjectResponse create(ProjectRequest request) {
        log.info("Creating project: {}", request.getName());
        UserEntity owner = userRepository
                .findById(request.getOwnerId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        ProjectEntity project = projectMapper.toEntity(request);

        project.setOwner(owner);
        HashSet<UserEntity> members = new HashSet<>();
        project.setMembers(members);
        projectRepository.save(project);
        return projectMapper.toResponse(project);
    }

    /**
     * Update a project.
     * @param id
     * @param request
     * @return
     */
    @Transactional
    @Override
    @CachePut(value = "project-detail", key = "'project_' +#id", cacheManager = "compositeCacheManager")
    @CacheEvict(
            value = {"project-list"},
            allEntries = true,
            cacheManager = "redisCacheManager")
    @PostAuthorize("hasRole('ADMIN') or returnObject.owner.username == authentication.name")
    public ProjectResponse update(Long id, ProjectRequest request) {
        log.info("Updating project: {}", id);
        log.info(
                "Current user: {}",
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal()
                        .toString());
        UserEntity owner = userRepository
                .findById(request.getOwnerId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        var project = projectRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));
        projectMapper.updateEntityFromRequest(request, project);
        project.setOwner(owner);
        return projectMapper.toResponse(projectRepository.save(project));
    }

    /**
     * Soft delete a project.
     * @param id
     */
    @Override
    @Transactional
    @CacheEvict(
            value = "project-detail",
            key = "'project_' +#id",
            allEntries = true,
            cacheManager = "compositeCacheManager")
    @PreAuthorize(
            "hasRole('ADMIN') or @ownershipSecurity.isOwner(T(org.example.cv.models.entities.ProjectEntity), authentication, #id)")
    public void softdelete(Long id) {
        log.info("Deleting project: {}", id);
        var project = projectRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));
        projectRepository.softDeleteByIds(Collections.singletonList(id));
        log.info("Deleted project: {}", id);
    }

    /**
     * Restore a soft deleted project.
     * @param id
     */
    @Override
    @Transactional
    @CacheEvict(
            value = "project-detail",
            key = "'project_' +#id",
            allEntries = true,
            cacheManager = "compositeCacheManager")
    @PreAuthorize(
            "hasRole('ADMIN') or @ownershipSecurity.isOwner(T(org.example.cv.models.entities.ProjectEntity), authentication, #id)")
    public void restore(Long id) {
        log.info("Restoring project: {}", id);
        var project = projectRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));
        projectRepository.restoreById(id);
        log.info("Restored project: {}", id);
    }

    /**
     * Get all projects by owner id with pagination, sorting, and filtering.
     * @param ownerId
     * @param page
     * @param size
     * @param sortBy
     * @param sortDir
     * @param filter
     * @return
     */
    @Override
    @PreAuthorize("hasRole('ADMIN') or @ownershipSecurity.isOwner(#ownerId, authentication)")
    public Page<ProjectResponse> getAllByOwnerId(
            Long ownerId, int page, int size, String sortBy, String sortDir, String filter) {
        log.info("Getting all projects by owner id: {}", ownerId);
        Pageable pageable = PageRequest.of(page, size).withSort(Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        return projectRepository.findAllByOwnerId(pageable, filter, ownerId).map(projectMapper::toResponse);
    }

    @Override
    public ProjectResponse addMember(Long projectId, Long userId) {
        log.info("Adding member {} to project {}", userId, projectId);
        ProjectEntity project = projectRepository
                .findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));
        UserEntity user =
                userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        project.getMembers().add(user);
        projectRepository.save(project);
        return projectMapper.toResponse(project);
    }

    @Override
    public ProjectResponse removeMember(Long projectId, Long userId) {
        log.info("Removing member {} from project {}", userId, projectId);
        var project = projectRepository
                .findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));
        var user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        project.getMembers().remove(user);
        projectRepository.save(project);
        return projectMapper.toResponse(project);
    }

    @Override
    public ProjectResponse changeOwner(Long projectId, Long newOwnerId) {
        log.info("Changing owner of project {} to {}", projectId, newOwnerId);
        var project = projectRepository
                .findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));
        var newOwner =
                userRepository.findById(newOwnerId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        project.setOwner(newOwner);
        projectRepository.save(project);
        return projectMapper.toResponse(project);
    }
}
