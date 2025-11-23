package org.example.cv.services.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.example.cv.constants.NotificationType;
import org.example.cv.constants.TaskStatus;
import org.example.cv.event.TaskEvent;
import org.example.cv.exceptions.AppException;
import org.example.cv.exceptions.ErrorCode;
import org.example.cv.models.entities.ProjectEntity;
import org.example.cv.models.entities.TaskEntity;
import org.example.cv.models.entities.UserEntity;
import org.example.cv.models.requests.CreateTaskRequest;
import org.example.cv.models.requests.TaskFilterRequest;
import org.example.cv.models.requests.UpdateTaskRequest;
import org.example.cv.models.requests.UpdateTaskStatusRequest;
import org.example.cv.models.responses.PageResponse;
import org.example.cv.models.responses.TaskResponse;
import org.example.cv.repositories.ProjectRepository;
import org.example.cv.repositories.TaskRepository;
import org.example.cv.repositories.UserRepository;
import org.example.cv.services.TaskService;
import org.example.cv.utils.AuthenticationUtils;
import org.example.cv.utils.TaskSpecification;
import org.example.cv.utils.mapper.TaskMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final ApplicationEventPublisher eventPublisher;

    private static final Map<TaskStatus, Set<TaskStatus>> VALID_TRANSITIONS = Map.of(
            TaskStatus.TODO, EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED),
            TaskStatus.IN_PROGRESS, EnumSet.of(TaskStatus.DONE, TaskStatus.CANCELLED, TaskStatus.TODO),
            TaskStatus.DONE, EnumSet.of(TaskStatus.IN_PROGRESS),
            TaskStatus.CANCELLED, EnumSet.noneOf(TaskStatus.class)
    );

    @Override
    public PageResponse<TaskResponse> getAllTasks(TaskFilterRequest filter, Pageable pageable) {
        Specification<TaskEntity> spec = TaskSpecification.fromFilter(filter);
        // Lưu ý: Đảm bảo Repository dùng @EntityGraph hoặc JOIN FETCH trong findAllWithFilter để tránh N+1
        Page<TaskEntity> taskPage = taskRepository.findAllWithFilter(spec, pageable);
        return mapToPageResponse(taskPage);
    }

    @Override
    public PageResponse<TaskResponse> getMyTasks(Pageable pageable) {
        Long currentUserId = AuthenticationUtils.getCurrentUserId();
        Page<TaskEntity> taskPage = taskRepository.findByAssigneesIdOrProjectOwnerId(currentUserId, pageable);
        return mapToPageResponse(taskPage);
    }

    @Override
    @Cacheable(value = "cache-task-details", key = "#id", cacheManager = "redisCacheManager")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurityService.canAccessTask(#id)")
    public TaskResponse getTaskById(Long id) {
        TaskEntity task = findTaskById(id);
        return taskMapper.toTaskResponse(task);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cache-task-lists", allEntries = true, cacheManager = "redisCacheManager")
    public TaskResponse createTask(CreateTaskRequest request) {
        Long currentUserId = AuthenticationUtils.getCurrentUserId();

        ProjectEntity project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));

        // Validate: Người tạo task phải là Owner hoặc Member của project (Tùy logic, ở đây giữ logic Owner)
        if (!project.getOwner().getId().equals(currentUserId)) {
            // Mở rộng: && !project.getMembers().contains(currentUser)
            throw new AppException(ErrorCode.USER_NOT_PROJECT_OWNER);
        }
        Set<UserEntity> assignees = fetchAndValidateAssignees(request.assignees(), project);

        TaskEntity task = taskMapper.toEntity(request);
        task.setProject(project);
        task.setAssignees(assignees);
        task.setStatus(TaskStatus.TODO);

        TaskEntity savedTask = taskRepository.save(task);

        // Notify
        notifyAssignees(savedTask, project.getOwner(), assignees, NotificationType.TASK_ASSIGNED);

        return taskMapper.toTaskResponse(savedTask);
    }

    @Override
    @Transactional
    @CachePut(value = "cache-task-details", key = "#id", cacheManager = "redisCacheManager")
    @CacheEvict(value = "cache-task-lists", allEntries = true, cacheManager = "redisCacheManager")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurityService.canAccessTask(#id)")
    public TaskResponse updateTask(Long id, UpdateTaskRequest request) {
        TaskEntity existingTask = findTaskById(id);

        validateStatusTransition(existingTask.getStatus(), request.status());

        Set<UserEntity> newAssignees = fetchAndValidateAssignees(request.assignees(), existingTask.getProject());
        existingTask.setAssignees(newAssignees);

        taskMapper.updateEntityFromRequest(request, existingTask);
        TaskEntity updatedTask = taskRepository.save(existingTask);

        // Notify (Lấy ID từ Security Context để tránh query DB thừa)
        Long currentUserId = AuthenticationUtils.getCurrentUserId();
        // Cần tạo proxy user entity chỉ chứa ID để pass vào event (nếu NotificationService xử lý được)
        // Hoặc query nhẹ user hiện tại
        UserEntity currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        notifyAssignees(updatedTask, currentUser, newAssignees, NotificationType.TASK_UPDATED);

        return taskMapper.toTaskResponse(updatedTask);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @taskSecurityService.isAssigneeOrCreator(#id)")
    @CacheEvict(
            value = {"cache-task-details", "cache-task-lists"},
            key = "#id",
            allEntries = true, // Clear list cache vì status thay đổi ảnh hưởng đến filter/sort
            cacheManager = "redisCacheManager")
    public TaskResponse updateTaskStatus(Long id, UpdateTaskStatusRequest request) {
        TaskEntity existingTask = findTaskById(id);
        validateStatusTransition(existingTask.getStatus(), request.status());

        existingTask.setStatus(request.status());
        TaskEntity updatedTask = taskRepository.save(existingTask);

        // Notify
        Long currentUserId = AuthenticationUtils.getCurrentUserId();
        UserEntity currentUser = userRepository.findById(currentUserId).orElseThrow();
        notifyAssignees(updatedTask, currentUser, existingTask.getAssignees(), NotificationType.TASK_UPDATED);

        return taskMapper.toTaskResponse(updatedTask);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @taskSecurityService.canAccessTask(#id)")
    @CacheEvict(value = {"cache-task-details", "cache-task-lists"}, key = "#id", allEntries = true, cacheManager = "redisCacheManager")
    public void deleteTask(Long id) {
        // FIX 3: Dùng existsById thay vì findById để nhẹ DB
        if (!taskRepository.existsById(id)) {
            throw new AppException(ErrorCode.TASK_NOT_EXISTED);
        }
        taskRepository.softDeleteByIds(List.of(id));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')") // Thường chỉ admin/owner mới restore
    public void restoreTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new AppException(ErrorCode.TASK_NOT_EXISTED);
        }
        taskRepository.restoreById(id);
    }

    @Override
    public PageResponse<TaskResponse> getAllMySoftDeletedTasks(Pageable pageable) {
        Long currentUserId = AuthenticationUtils.getCurrentUserId();
        return mapToPageResponse(taskRepository.findAllSoftDeletedByOwnerId(currentUserId, pageable));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<TaskResponse> getAllSoftDeletedTasks(Pageable pageable) {
        return mapToPageResponse(taskRepository.findAllSoftDeleted(pageable));
    }

    // --- Helper Methods ---

    private TaskEntity findTaskById(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_EXISTED));
    }

    /**
     * Helper: Fetch users by IDs in ONE query and validate project membership
     */
    private Set<UserEntity> fetchAndValidateAssignees(Set<Long> assigneeIds, ProjectEntity project) {
        if (assigneeIds == null || assigneeIds.isEmpty()) {
            return new HashSet<>();
        }

        // 1. Fetch all users in ONE query
        List<UserEntity> users = userRepository.findAllById(assigneeIds);
        if (users.size() != assigneeIds.size()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED); // Có ID không tồn tại
        }
        for (UserEntity user : users) {
            // Check Owner hoặc Member
            boolean isOwner = project.getOwner().getId().equals(user.getId());
            boolean isMember = project.getMembers().stream().anyMatch(m -> m.getId().equals(user.getId()));

            if (!isOwner && !isMember) {
                throw new AppException(ErrorCode.USER_NOT_PROJECT_MEMBER);
            }
        }
        return new HashSet<>(users);
    }

    private void validateStatusTransition(TaskStatus oldStatus, TaskStatus newStatus) {
        if (oldStatus == newStatus) return;
        Set<TaskStatus> allowed = VALID_TRANSITIONS.getOrDefault(oldStatus, EnumSet.noneOf(TaskStatus.class));
        if (!allowed.contains(newStatus)) {
            throw new AppException(ErrorCode.INVALID_TASK_STATUS_TRANSITION);
        }
    }

    private void notifyAssignees(TaskEntity task, UserEntity actor, Set<UserEntity> recipients, NotificationType type) {
        recipients.forEach(u -> {
            if (!u.getId().equals(actor.getId())) {
                eventPublisher.publishEvent(new TaskEvent(task, actor, u, type));
            }
        });
    }

    private PageResponse<TaskResponse> mapToPageResponse(Page<TaskEntity> page) {
        List<TaskResponse> responses = page.getContent().stream().map(taskMapper::toTaskResponse).toList();
        return new PageResponse<>(
                responses,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }
}