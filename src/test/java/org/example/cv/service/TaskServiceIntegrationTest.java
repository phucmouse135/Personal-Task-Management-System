package org.example.cv.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import org.example.cv.constants.TaskPriority;
import org.example.cv.constants.TaskStatus;
import org.example.cv.models.entities.ProjectEntity;
import org.example.cv.models.entities.TaskEntity;
import org.example.cv.models.entities.UserEntity;
import org.example.cv.models.requests.TaskFilterRequest;
import org.example.cv.models.responses.PageResponse;
import org.example.cv.models.responses.TaskResponse;
import org.example.cv.repositories.ProjectRepository;
import org.example.cv.repositories.TaskRepository;
import org.example.cv.repositories.UserRepository;
import org.example.cv.services.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test") // Sử dụng profile test (application-test.yml, thường dùng H2)
@Transactional // Rollback transaction sau mỗi test
class TaskServiceIntegrationTest {

    @Autowired
    private TaskService taskService; // Service (logic) không đổi

    // === CẬP NHẬT ===
    // Autowire các Repository tương ứng với Entity mới
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    // === CẬP NHẬT ===
    // Sử dụng đúng kiểu Entity
    private ProjectEntity project1, project2;
    private UserEntity user1, user2;

    @BeforeEach
    void setUpDatabase() {
        // Xóa dữ liệu cũ theo đúng thứ tự (Task -> Project -> User)
        // để tránh lỗi khóa ngoại
        taskRepository.deleteAllInBatch();
        projectRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        // === CẬP NHẬT ===
        // Tạo dữ liệu giả bằng @Builder của Entity

        // 1. Tạo Users
        user1 = UserEntity.builder()
                .username("user1")
                .password("hashed_password") // Pass sẽ được hash trong service thực tế
                .email("user1@example.com")
                .firstName("Test")
                .lastName("User 1")
                .build();
        user1 = userRepository.save(user1);

        user2 = UserEntity.builder()
                .username("user2")
                .password("hashed_password")
                .email("user2@example.com")
                .firstName("Test")
                .lastName("User 2")
                .build();
        user2 = userRepository.save(user2);

        // 2. Tạo Projects
        project1 = ProjectEntity.builder()
                .name("Project A")
                .owner(user1) // Gán owner
                .build();
        project1 = projectRepository.save(project1);

        project2 = ProjectEntity.builder().name("Project B").owner(user2).build();
        project2 = projectRepository.save(project2);

        // 3. Tạo Tasks
        Instant now = Instant.now();
        taskRepository.save(TaskEntity.builder()
                .title("Task 1")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .project(project1)
                .assignees((Set<UserEntity>) user1)
                .deadline(now.plus(Duration.ofDays(5))) // Cung cấp deadline (NOT NULL)
                .build());

        taskRepository.save(TaskEntity.builder()
                .title("Task 2")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .project(project1)
                .assignees((Set<UserEntity>) user2)
                .deadline(now.plus(Duration.ofDays(2)))
                .build());

        taskRepository.save(TaskEntity.builder()
                .title("Task 3")
                .status(TaskStatus.DONE)
                .priority(TaskPriority.LOW)
                .project(project2)
                .assignees((Set<UserEntity>) user1)
                .deadline(now.minus(Duration.ofDays(1))) // Task đã trễ hạn
                .build());
    }

    @Test
    @DisplayName("Integration Test: Lọc Task theo Project ID")
    void getAllTasks_FilterByProjectId() {
        // Arrange
        // DTO request không đổi, nó là lớp API
        TaskFilterRequest filter = new TaskFilterRequest(project1.getId(), null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        PageResponse<TaskResponse> response = taskService.getAllTasks(filter, pageable);

        // Assert
        // Logic test không đổi, vì chúng ta assert trên DTO Response
        assertThat(response.totalElements()).isEqualTo(2);
        assertThat(response.content()).extracting("title").containsExactlyInAnyOrder("Task 1", "Task 2");
    }

    @Test
    @DisplayName("Integration Test: Lọc Task theo Assignee và Status")
    void getAllTasks_FilterByAssigneeAndStatus() {
        // Arrange
        TaskFilterRequest filter = new TaskFilterRequest(
                null,
                user1.getId(),
                Set.of(TaskStatus.TODO, TaskStatus.DONE), // Lấy task TODO và DONE của user1
                null,
                null,
                null);
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        PageResponse<TaskResponse> response = taskService.getAllTasks(filter, pageable);

        // Assert
        assertThat(response.totalElements()).isEqualTo(2);
        assertThat(response.content()).extracting("title").containsExactlyInAnyOrder("Task 1", "Task 3");
    }

    @Test
    @DisplayName("Integration Test: Lọc Task không có kết quả")
    void getAllTasks_FilterReturnsNoResults() {
        // Arrange
        TaskFilterRequest filter = new TaskFilterRequest(
                project2.getId(), // Project B (chỉ có Task 3)
                user2.getId(), // Assignee là user2 (không có task nào trong Project B)
                null,
                null,
                null,
                null);
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        PageResponse<TaskResponse> response = taskService.getAllTasks(filter, pageable);

        // Assert
        assertThat(response.totalElements()).isEqualTo(0);
        assertThat(response.content()).isEmpty();
    }
}
