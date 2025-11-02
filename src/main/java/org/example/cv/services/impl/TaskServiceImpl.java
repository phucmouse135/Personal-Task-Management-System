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
import org.springframework.data.domain.PageRequest;
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

    // Business Logic: Định nghĩa các bước chuyển trạng thái hợp lệ
    private static final Map<TaskStatus, Set<TaskStatus>> VALID_TRANSITIONS = Map.of(
            TaskStatus.TODO, EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED),
            TaskStatus.IN_PROGRESS, EnumSet.of(TaskStatus.DONE, TaskStatus.CANCELLED, TaskStatus.TODO),
            TaskStatus.DONE, EnumSet.of(TaskStatus.IN_PROGRESS), // Cho phép reopen
            TaskStatus.CANCELLED, EnumSet.noneOf(TaskStatus.class) // Không thể chuyển từ CANCELLED
            );

    // Tạm thời tắt cache để tránh lỗi LinkedHashMap cast to Page
    // @Cacheable(
    //         value = "cache-task-lists",
    //         key = "'all_'+#pageable.pageNumber+'_'+#pageable.pageSize+'_'+#pageable.sort+'_'+#filter",
    //         cacheManager = "redisCacheManager")
    @Override
    public PageResponse<TaskResponse> getAllTasks(TaskFilterRequest filter, Pageable pageable) {
        // Sử dụng Specification để xây dựng query động
        log.info("Getting all tasks for filter {}", filter);
        Specification<TaskEntity> spec = TaskSpecification.fromFilter(filter);
        Page<TaskEntity> taskPage = taskRepository.findAll(spec, pageable);

        Page<TaskResponse> dtoPage = taskPage.map(taskMapper::toTaskResponse);

        return new PageResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages(),
                dtoPage.isLast());
    }

    @Override
    public PageResponse<TaskResponse> getMyTasks(Pageable pageable) {
        log.info("Getting tasks for current user");
        Long currentUserId = AuthenticationUtils.getCurrentUserId();

        // Tìm tất cả tasks mà user hiện tại là assignee HOẶC là người tạo (owner của project)
        Page<TaskEntity> taskPage = taskRepository.findByAssigneesIdOrProjectOwnerId(currentUserId, pageable);

        Page<TaskResponse> dtoPage = taskPage.map(taskMapper::toTaskResponse);

        return new PageResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages(),
                dtoPage.isLast());
    }

    @Override
    @Cacheable(value = "cache-task-details", key = "#id", cacheManager = "redisCacheManager")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurityService.canAccessTask(#id)")
    public TaskResponse getTaskById(Long id) {
        log.info("Getting task with id {}", id);
        TaskEntity task = findTaskById(id);
        return taskMapper.toTaskResponse(task);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cache-task-lists", allEntries = true, cacheManager = "redisCacheManager")
    public TaskResponse createTask(CreateTaskRequest request) {
        // TODO: Cần kiểm tra xem user hiện tại có quyền tạo task trong project này không
        log.info("Creating task {}", request);
        ProjectEntity project = projectRepository
                .findById(request.projectId())
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_EXISTED));
        if (!project.getOwner().getId().equals(AuthenticationUtils.getCurrentUserId())) {
            throw new AppException(ErrorCode.USER_NOT_PROJECT_OWNER);
        }

        Set<UserEntity> assignees = new HashSet<>();
        request.assignees().forEach(u -> {
            UserEntity user =
                    userRepository.findById(u).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            assignees.add(user);
        });

        TaskEntity task = taskMapper.toEntity(request);
        task.setProject(project);
        task.setAssignees(assignees);
        task.setStatus(TaskStatus.TODO); // Luôn bắt đầu là TODO

        TaskEntity savedTask = taskRepository.save(task);
        log.info("Created task {}", savedTask);

        assignees.forEach(u -> {
            if (u.getId().equals(project.getOwner().getId())) return; // Không gửi thông báo cho người tạo
            eventPublisher.publishEvent(
                    new TaskEvent(savedTask, project.getOwner(), u, NotificationType.TASK_ASSIGNED));
        });

        return taskMapper.toTaskResponse(savedTask);
    }

    @Override
    @Transactional
    @CachePut(value = "cache-task-details", key = "#id", cacheManager = "redisCacheManager")
    @CacheEvict(value = "cache-task-lists", allEntries = true, cacheManager = "redisCacheManager")
    @PreAuthorize("hasRole('ADMIN') or @taskSecurityService.canAccessTask(#id)")
    public TaskResponse updateTask(Long id, UpdateTaskRequest request) {
        log.info("Updating task id {} with {}", id, request);
        TaskEntity existingTask = findTaskById(id);

        // Business Logic: Kiểm tra chuyển đổi trạng thái
        validateStatusTransition(existingTask.getStatus(), request.status());

        Set<UserEntity> assignees = new HashSet<>();
        request.assignees().forEach(u -> {
            UserEntity user =
                    userRepository.findById(u).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            assignees.add(user);
        });
        existingTask.setAssignees(assignees);
        // Map các trường còn lại
        taskMapper.updateEntityFromRequest(request, existingTask);

        TaskEntity updatedTask = taskRepository.save(existingTask);

        UserEntity currentUser = userRepository
                .findById(Objects.requireNonNull(AuthenticationUtils.getCurrentUserId()))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        log.info("Updated task {}", updatedTask);
        assignees.forEach(u -> {
            if (u.getId().equals(currentUser.getId())) return; // Không gửi thông báo cho người cập nhật
            eventPublisher.publishEvent(new TaskEvent(updatedTask, currentUser, u, NotificationType.TASK_UPDATED));
        });

        return taskMapper.toTaskResponse(updatedTask);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @taskSecurityService.isAssigneeOrCreator(#id)")
    @CacheEvict(
            value = {"cache-task-details", "cache-task-lists"},
            key = "#id",
            allEntries = true,
            cacheManager = "redisCacheManager")
    public TaskResponse updateTaskStatus(Long id, UpdateTaskStatusRequest request) {
        log.info("Updating task status for id {} to {}", id, request.status());
        TaskEntity existingTask = findTaskById(id);

        // Validate status transition
        validateStatusTransition(existingTask.getStatus(), request.status());

        // Only update the status
        existingTask.setStatus(request.status());

        TaskEntity updatedTask = taskRepository.save(existingTask);

        UserEntity currentUser = userRepository
                .findById(Objects.requireNonNull(AuthenticationUtils.getCurrentUserId()))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        log.info("Updated task status {}", updatedTask);

        // Notify assignees about status change
        existingTask.getAssignees().forEach(u -> {
            if (u.getId().equals(currentUser.getId())) return;
            eventPublisher.publishEvent(new TaskEvent(updatedTask, currentUser, u, NotificationType.TASK_UPDATED));
        });

        return taskMapper.toTaskResponse(updatedTask);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @taskSecurityService.canAccessTask(#id)")
    @CacheEvict(
            value = {"cache-task-details", "cache-task-lists"},
            key = "#id",
            allEntries = true,
            cacheManager = "redisCacheManager")
    public void deleteTask(Long id) {
        log.info("Soft deleting task with id {}", id);
        // Verify task exists before deleting
        findTaskById(id);
        taskRepository.softDeleteByIds(List.of(id));
    }

    @Override
    public void restoreTask(Long id) {
        log.info("Restoring task with id {}", id);
        // Verify task exists before restoring
        findTaskById(id);
        taskRepository.restoreById(id);
    }

    @Override
    public PageResponse<TaskResponse> getAllMySoftDeletedTasks(int page, int size) {
        log.info("Getting all my soft deleted tasks - page: {}, size: {}", page, size);

        Long currentUserId = AuthenticationUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);

        Page<TaskEntity> taskPage = taskRepository.findAllSoftDeletedByOwnerId(currentUserId, pageable);

        List<TaskResponse> taskResponses =
                taskPage.getContent().stream().map(taskMapper::toTaskResponse).collect(Collectors.toList());

        return new PageResponse<>(
                taskResponses,
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.isLast());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<TaskResponse> getAllSoftDeletedTasks(int page, int size) {
        log.info("Getting all soft deleted tasks - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<TaskEntity> taskPage = taskRepository.findAllSoftDeleted(pageable);

        List<TaskResponse> taskResponses =
                taskPage.getContent().stream().map(taskMapper::toTaskResponse).collect(Collectors.toList());

        return new PageResponse<>(
                taskResponses,
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.isLast());
    }

    // --- Helper Methods ---

    private TaskEntity findTaskById(Long id) {
        log.info("Finding task with id {}", id);
        return taskRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_EXISTED));
    }

    private void validateStatusTransition(TaskStatus oldStatus, TaskStatus newStatus) {
        if (oldStatus == newStatus) return; // Không thay đổi

        Set<TaskStatus> allowedTransitions =
                VALID_TRANSITIONS.getOrDefault(oldStatus, EnumSet.noneOf(TaskStatus.class));

        if (!allowedTransitions.contains(newStatus)) {
            throw new AppException(ErrorCode.INVALID_TASK_STATUS_TRANSITION);
        }
    }
}
