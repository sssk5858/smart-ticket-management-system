package com.smartticket.system.service;

import com.smartticket.system.model.AuditLog;
import com.smartticket.system.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(Long ticketId, String action, String updatedBy) {
        auditLogRepository.save(AuditLog.builder()
                .ticketId(ticketId)
                .action(action)
                .updatedBy(updatedBy)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
