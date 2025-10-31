package org.example.cv.services;

import org.example.cv.event.AuditLogEvent;
import org.springframework.stereotype.Service;

@Service
public interface AuditLogService {
    void handleAuditLogEvent(AuditLogEvent event);
}
