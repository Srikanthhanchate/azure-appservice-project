package com.orginsight.service;

public interface AuditLogService {
    void log(String actorEmail, String action, String entityType, String entityId, String details);
}
