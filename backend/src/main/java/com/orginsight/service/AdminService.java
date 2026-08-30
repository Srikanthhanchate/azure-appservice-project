package com.orginsight.service;

import com.orginsight.dto.common.PageResponse;
import com.orginsight.dto.request.UpdateUserRoleRequest;
import com.orginsight.dto.request.UpdateUserStatusRequest;
import com.orginsight.dto.response.AdminUserResponse;
import com.orginsight.dto.response.AuditLogResponse;
import com.orginsight.dto.response.SystemStatisticsResponse;

public interface AdminService {
    PageResponse<AdminUserResponse> getUsers(int page, int size);
    AdminUserResponse updateUserRole(Long userId, UpdateUserRoleRequest request, String actorEmail);
    AdminUserResponse updateUserStatus(Long userId, UpdateUserStatusRequest request, String actorEmail);
    PageResponse<AuditLogResponse> getAuditLogs(int page, int size);
    SystemStatisticsResponse getSystemStatistics();
}
