package org.example.cv.services;

import org.example.cv.models.entities.TaskEntity;
import org.example.cv.repositories.TaskRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.sun.security.auth.UserPrincipal;

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal currentUser)) {
            return false;
        }

        // Lấy task và join fetch project + owner để tránh N+1
        TaskEntity task = taskRepository
                .findTaskWithDetailsById(taskId)
                .orElse(null); // Không throw 404 ở đây, để PreAuthorize xử lý

        if (task == null) {
            return false; // Hoặc true nếu muốn 404 thay vì 403
        }

        String currentUserName = currentUser.getName();

        // 1. User là người được gán (Assignee)
        if (task.getAssignee().getUsername() != null
                && task.getAssignee().getId().equals(currentUserName)) {
            return true;
        }

        // 2. User là chủ dự án (Project Owner)
        if (task.getProject().getOwner() != null
                && task.getProject().getOwner().getId().equals(currentUserName)) {
            return true;
        }

        return false;
    }
}
