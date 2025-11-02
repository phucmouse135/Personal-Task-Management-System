package org.example.cv.event;

import org.springframework.context.ApplicationEvent;

public class AuditLogEvent extends ApplicationEvent {
    private final String entityType;
    private final Long entityId;
    private final String action;
    private final String details;
    private final Long actorId;

    public AuditLogEvent(Object source, String entityType, Long entityId, String action, String details, Long actorId) {
        super(source);
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.details = details;
        this.actorId = actorId;
    }

    public String getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    public Long getActorId() {
        return actorId;
    }
}
