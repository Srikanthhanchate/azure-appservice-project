package com.orginsight.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.orginsight.dto.common.PageResponse;
import com.orginsight.dto.request.UpdateUserRoleRequest;
import com.orginsight.dto.request.UpdateUserStatusRequest;
import com.orginsight.dto.response.AdminUserResponse;
import com.orginsight.dto.response.AuditLogResponse;
import com.orginsight.dto.response.SystemStatisticsResponse;
import com.orginsight.service.AdminService;

import jakarta.validation.Valid;

/**
 * All endpoints here are restricted to ROLE_ADMIN via SecurityConfig
 * ("/api/admin/**" -> hasRole("ADMIN")).
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<String> dashboard() {
        return ResponseEntity.ok("Admin dashboard is available");
    }

    @GetMapping("/users")
    public ResponseEntity<PageResponse<AdminUserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getUsers(page, size));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<AdminUserResponse> updateUserRole(@PathVariable Long id,
                                                              @Valid @RequestBody UpdateUserRoleRequest request,
                                                              Authentication authentication) {
        return ResponseEntity.ok(adminService.updateUserRole(id, request, authentication.getName()));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<AdminUserResponse> updateUserStatus(@PathVariable Long id,
                                                                @Valid @RequestBody UpdateUserStatusRequest request,
                                                                Authentication authentication) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, request, authentication.getName()));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<PageResponse<AuditLogResponse>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAuditLogs(page, size));
    }

    @GetMapping("/system-statistics")
    public ResponseEntity<SystemStatisticsResponse> getSystemStatistics() {
        return ResponseEntity.ok(adminService.getSystemStatistics());
    }
}
