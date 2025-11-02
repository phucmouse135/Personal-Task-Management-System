package org.example.cv.event;

import org.example.cv.constants.NotificationType;
import org.example.cv.models.entities.TaskEntity;
import org.example.cv.models.entities.UserEntity;
import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class TaskEvent extends ApplicationEvent {

    private final TaskEntity task;
    private final UserEntity actor; // Người thực hiện
    private final UserEntity assignee; // Người bị tác động
    private final NotificationType type;

    /**
     * @param task Task bị tác động
     * @param actor Người thực hiện hành động
     * @param type Loại hành động
     * @param assignee Người bị tác động
     */
    public TaskEvent(TaskEntity task, UserEntity actor, UserEntity assignee, NotificationType type) {
        super(task);
        this.task = task;
        this.actor = actor;
        this.type = type;
        this.assignee = assignee;
    }
}
