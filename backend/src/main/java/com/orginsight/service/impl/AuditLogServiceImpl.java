package com.orginsight.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orginsight.entity.AuditLog;
import com.orginsight.repository.AuditLogRepository;
import com.orginsight.service.AuditLogService;

@Service
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void log(String actorEmail, String action, String entityType, String entityId, String details) {
        AuditLog entry = AuditLog.builder()
                .actorEmail(actorEmail)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .build();
        auditLogRepository.save(entry);
    }
}
