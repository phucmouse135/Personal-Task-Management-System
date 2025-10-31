package org.example.cv.services;

import org.example.cv.models.requests.CreateTaskRequest;
import org.example.cv.models.requests.TaskFilterRequest;
import org.example.cv.models.requests.UpdateTaskRequest;
import org.example.cv.models.responses.PageResponse;
import org.example.cv.models.responses.TaskResponse;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    PageResponse<TaskResponse> getAllTasks(TaskFilterRequest filter, Pageable pageable);

    TaskResponse getTaskById(Long id);

    TaskResponse createTask(CreateTaskRequest request);

    TaskResponse updateTask(Long id, UpdateTaskRequest request);

    void deleteTask(Long id);
}
