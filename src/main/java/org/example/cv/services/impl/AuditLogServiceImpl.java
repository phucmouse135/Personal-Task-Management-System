package org.example.cv.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cv.event.AuditLogEvent;
import org.example.cv.models.entities.AuditLogEntity;
import org.example.cv.repositories.AuditLogRepository;
import org.example.cv.services.AuditLogService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogRepository auditLogRepository;

    @Override
    @TransactionalEventListener
    @Async
    @EventListener(AuditLogEvent.class)
    public void handleAuditLogEvent(AuditLogEvent event) {
        AuditLogEntity entity = AuditLogEntity.builder()
                .actorId(event.getActorId())
                .actionType(event.getAction())
                .entityType(event.getEntityType())
                .entityId(event.getEntityId())
                .details(event.getDetails())
                .timestamp(Instant.now())
                .build();

        auditLogRepository.save(entity);
        log.info("✅ [Async] Saved audit log: {} {} id={}", event.getAction(), event.getEntityType(), event.getEntityId());
    }
}
