package org.example.cv.services;

import org.example.cv.models.entities.TaskEntity;
import org.example.cv.models.entities.UserEntity;
import org.example.cv.repositories.TaskRepository;
import org.example.cv.utils.AuthenticationUtils;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service("taskSecurityService")
@RequiredArgsConstructor
public class TaskSecurityService {

    private final TaskRepository taskRepository;

    /**
     * Kiểm tra xem user hiện tại có phải là Assignee, Project Owner,
     * hay không (Admin đã được kiểm tra bằng hasRole('ADMIN')).
     */
    public boolean canAccessTask(Long taskId) {
        // Lấy task và join fetch project + owner để tránh N+1
        TaskEntity task = taskRepository
                .findTaskWithDetailsById(taskId)
                .orElse(null); // Không throw 404 ở đây, để PreAuthorize xử lý

        if (task == null) {
            return false; // Hoặc true nếu muốn 404 thay vì 403
        }

        Long currentId = AuthenticationUtils.getCurrentUserId();

        for (UserEntity assignee : task.getAssignees()) {
            if (assignee.getId().equals(currentId)) {
                return true;
            }
        }

        // 2. User là chủ dự án (Project Owner)
        if (task.getProject().getOwner() != null
                && task.getProject().getOwner().getId().equals(currentId)) {
            return true;
        }

        return false;
    }
}
