package org.example.cv.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.example.cv.constants.TaskStatus;
import org.example.cv.exceptions.AppException;
import org.example.cv.models.entities.TaskEntity;
import org.example.cv.models.entities.UserEntity;
import org.example.cv.models.requests.UpdateTaskRequest;
import org.example.cv.models.responses.TaskResponse;
import org.example.cv.repositories.TaskRepository;
import org.example.cv.repositories.UserRepository;
import org.example.cv.services.impl.TaskServiceImpl;
import org.example.cv.utils.mapper.TaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskMapper taskMapper; // Giả định TaskMapper là một interface

    // Không mock ProjectRepository vì không dùng trong các test case này

    @InjectMocks
    private TaskServiceImpl taskService;

    private TaskEntity existingTask;
    private UserEntity existingAssignee;
    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        existingAssignee = new UserEntity();
        existingAssignee.setId(1L);

        Set<UserEntity> userEntities = new HashSet<>();
        userEntities.add(existingAssignee);
        existingTask = new TaskEntity();
        existingTask.setId(1L);
        existingTask.setTitle("Old Title");
        existingTask.setStatus(TaskStatus.IN_PROGRESS);
        existingTask.setAssignees(userEntities);

        taskResponse = new TaskResponse(1L, "New Title", null, TaskStatus.DONE, null, null, null, null, null, false);
    }

    @Test
    @DisplayName("Unit Test: Cập nhật Task thành công")
    void updateTask_Success() {
        // Arrange
        UpdateTaskRequest request = new UpdateTaskRequest(
                "New Title",
                null,
                TaskStatus.DONE,
                null,
                Instant.now().plus(5, ChronoUnit.DAYS),
                List.of(1L) // Thêm một assignee mới với ID 10
                );

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(existingTask);
        when(taskMapper.toTaskResponse(any(TaskEntity.class))).thenReturn(taskResponse);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(existingAssignee));

        // Act
        TaskResponse result = taskService.updateTask(1L, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("New Title");
        assertThat(result.status()).isEqualTo(TaskStatus.DONE);

        // Verify mapper được gọi để cập nhật entity
        verify(taskMapper).updateEntityFromRequest(request, existingTask);
        verify(taskRepository).save(existingTask);
    }

    @Test
    @DisplayName("Unit Test: Cập nhật Task thất bại - Chuyển trạng thái không hợp lệ")
    void updateTask_Fail_InvalidStatusTransition() {
        // Arrange
        existingTask.setStatus(TaskStatus.CANCELLED);
        UpdateTaskRequest request = new UpdateTaskRequest(
                "New Title",
                null,
                TaskStatus.IN_PROGRESS, // Không thể từ CANCELLED -> IN_PROGRESS
                null,
                null,
                List.of(10L));

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));

        // Act & Assert
        assertThatThrownBy(() -> taskService.updateTask(1L, request)).isInstanceOf(AppException.class);

        // Đảm bảo không lưu gì vào DB
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Unit Test: Cập nhật Task thất bại - Không tìm thấy Task")
    void updateTask_Fail_TaskNotFound() {
        // Arrange
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());
        UpdateTaskRequest request = mock(UpdateTaskRequest.class); // Request không quan trọng

        // Act & Assert
        assertThatThrownBy(() -> taskService.updateTask(99L, request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Task not existed");
    }
}
