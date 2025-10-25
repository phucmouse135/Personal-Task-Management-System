package org.example.cv.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Optional;

import org.example.cv.exceptions.AppException;
import org.example.cv.exceptions.ErrorCode;
import org.example.cv.models.entities.ProjectEntity;
import org.example.cv.models.entities.UserEntity;
import org.example.cv.models.requests.ProjectRequest;
import org.example.cv.models.responses.ProjectResponse;
import org.example.cv.repositories.ProjectRepository;
import org.example.cv.repositories.UserRepository;
import org.example.cv.services.impl.ProjectServiceImpl;
import org.example.cv.utils.mapper.ProjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private UserEntity user;
    private ProjectEntity project;
    private ProjectRequest projectRequest;
    private ProjectResponse projectResponse;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .id(1L)
                .username("testuser")
                .email("testuser@example.com")
                .build();

        project = ProjectEntity.builder()
                .id(1L)
                .name("Test Project")
                .description("Test Description")
                .owner(user)
                .build();

        projectRequest = ProjectRequest.builder()
                .name("Test Project")
                .description("Test Description")
                .ownerId(1L)
                .build();

        projectResponse = ProjectResponse.builder()
                .id(1L)
                .name("Test Project")
                .description("Test Description")
                .build();

        // Set up SecurityContextHolder
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("testuser");
        lenient().when(authentication.getPrincipal()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);

        // Use lenient stubbing for common mocks to avoid UnnecessaryStubbingException
        lenient().when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        lenient().when(projectMapper.toResponse(project)).thenReturn(projectResponse);
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    }

    @Test
    void getAll_ValidParameters_ReturnsPagedProjects() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        Page<ProjectEntity> projectPage = new PageImpl<>(Collections.singletonList(project));
        when(projectRepository.findAll(any(Pageable.class), anyString())).thenReturn(projectPage);
        when(projectMapper.toResponse(any(ProjectEntity.class))).thenReturn(projectResponse);

        // Act
        Page<ProjectResponse> result = projectService.getAll(0, 10, "name", "ASC", "");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(projectResponse, result.getContent().get(0));
        verify(projectRepository).findAll(pageable, "");
        verify(projectMapper).toResponse(project);
    }

    @Test
    void getById_ExistingId_ReturnsProjectResponse() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMapper.toResponse(project)).thenReturn(projectResponse);

        // Act
        ProjectResponse result = projectService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(projectResponse, result);
        verify(projectRepository).findById(1L);
        verify(projectMapper).toResponse(project);
    }

    @Test
    void getById_NonExistingId_ThrowsProjectNotExisted() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> projectService.getById(1L));
        assertEquals(ErrorCode.PROJECT_NOT_EXISTED, exception.getErrorCode());
        verify(projectRepository).findById(1L);
    }

    @Test
    void create_ValidRequest_CreatesAndReturnsProject() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(projectMapper.toEntity(projectRequest)).thenReturn(project);
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toResponse(project)).thenReturn(projectResponse);

        // Act
        ProjectResponse result = projectService.create(projectRequest);

        // Assert
        assertNotNull(result);
        assertEquals(projectResponse, result);
        verify(userRepository).findById(1L);
        verify(projectMapper).toEntity(projectRequest);
        verify(projectRepository).save(project);
        verify(projectMapper).toResponse(project);
    }

    @Test
    void create_NonExistingUser_ThrowsUserNotExisted() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> projectService.create(projectRequest));
        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
        verify(userRepository).findById(1L);
    }

    @Test
    void update_ValidRequest_UpdatesAndReturnsProject() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toResponse(project)).thenReturn(projectResponse);

        // Act
        ProjectResponse result = projectService.update(1L, projectRequest);

        // Assert
        assertNotNull(result);
        assertEquals(projectResponse, result);
        verify(userRepository).findById(1L);
        verify(projectRepository).findById(1L);
        verify(projectMapper).updateEntityFromRequest(projectRequest, project);
        verify(projectRepository).save(project);
        verify(projectMapper).toResponse(project);
    }

    @Test
    void update_NonExistingProject_ThrowsProjectNotExisted() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> projectService.update(1L, projectRequest));
        assertEquals(ErrorCode.PROJECT_NOT_EXISTED, exception.getErrorCode());
        verify(userRepository).findById(1L);
        verify(projectRepository).findById(1L);
    }

    @Test
    void softdelete_ExistingProject_DeletesSuccessfully() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        // Act
        projectService.softdelete(1L);

        // Assert
        verify(projectRepository).findById(1L);
        verify(projectRepository).softDeleteByIds(Collections.singletonList(1L));
    }

    @Test
    void softdelete_NonExistingProject_ThrowsProjectNotExisted() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> projectService.softdelete(1L));
        assertEquals(ErrorCode.PROJECT_NOT_EXISTED, exception.getErrorCode());
        verify(projectRepository).findById(1L);
    }

    @Test
    void restore_ExistingProject_RestoresSuccessfully() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        // Act
        projectService.restore(1L);

        // Assert
        verify(projectRepository).findById(1L);
        verify(projectRepository).restoreById(1L);
    }

    @Test
    void restore_NonExistingProject_ThrowsProjectNotExisted() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> projectService.restore(1L));
        assertEquals(ErrorCode.PROJECT_NOT_EXISTED, exception.getErrorCode());
        verify(projectRepository).findById(1L);
    }

    @Test
    void getAllByOwnerId_ValidParameters_ReturnsPagedProjects() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        Page<ProjectEntity> projectPage = new PageImpl<>(Collections.singletonList(project));
        when(projectRepository.findAllByOwnerId(any(Pageable.class), anyString(), eq(1L)))
                .thenReturn(projectPage);
        when(projectMapper.toResponse(any(ProjectEntity.class))).thenReturn(projectResponse);

        // Act
        Page<ProjectResponse> result = projectService.getAllByOwnerId(1L, 0, 10, "name", "ASC", "");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(projectResponse, result.getContent().get(0));
        verify(projectRepository).findAllByOwnerId(pageable, "", 1L);
        verify(projectMapper).toResponse(project);
    }
}
