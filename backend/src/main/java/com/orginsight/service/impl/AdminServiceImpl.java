package com.orginsight.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orginsight.dto.common.PageResponse;
import com.orginsight.dto.request.UpdateUserRoleRequest;
import com.orginsight.dto.request.UpdateUserStatusRequest;
import com.orginsight.dto.response.AdminUserResponse;
import com.orginsight.dto.response.AuditLogResponse;
import com.orginsight.dto.response.SystemStatisticsResponse;
import com.orginsight.entity.AuditLog;
import com.orginsight.entity.Role;
import com.orginsight.entity.User;
import com.orginsight.repository.AuditLogRepository;
import com.orginsight.repository.EmployeeRepository;
import com.orginsight.repository.KnowledgeItemRepository;
import com.orginsight.repository.ProjectRepository;
import com.orginsight.repository.RefreshTokenRepository;
import com.orginsight.repository.UserRepository;
import com.orginsight.service.AdminService;
import com.orginsight.service.AuditLogService;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final AuditLogRepository auditLogRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuditLogService auditLogService;

    public AdminServiceImpl(UserRepository userRepository,
                             EmployeeRepository employeeRepository,
                             ProjectRepository projectRepository,
                             KnowledgeItemRepository knowledgeItemRepository,
                             AuditLogRepository auditLogRepository,
                             RefreshTokenRepository refreshTokenRepository,
                             AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.projectRepository = projectRepository;
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.auditLogRepository = auditLogRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(Sort.Direction.ASC, "id"));
        Page<User> users = userRepository.findAll(pageable);
        return PageResponse.from(users.map(this::mapToResponse));
    }

    @Override
    public AdminUserResponse updateUserRole(Long userId, UpdateUserRoleRequest request, String actorEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id " + userId));
        Role previousRole = user.getRole();
        user.setRole(request.getRole());
        userRepository.save(user);

        auditLogService.log(actorEmail, "UPDATE_USER_ROLE", "User", String.valueOf(userId),
                "Role changed from " + previousRole + " to " + request.getRole());

        return mapToResponse(user);
    }

    @Override
    public AdminUserResponse updateUserStatus(Long userId, UpdateUserStatusRequest request, String actorEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id " + userId));
        user.setActive(request.getActive());
        userRepository.save(user);

        if (Boolean.FALSE.equals(request.getActive())) {
            refreshTokenRepository.revokeAllForUser(user);
        }

        auditLogService.log(actorEmail, request.getActive() ? "ACTIVATE_USER" : "DEACTIVATE_USER",
                "User", String.valueOf(userId), null);

        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        Page<AuditLog> logs = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        return PageResponse.from(logs.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public SystemStatisticsResponse getSystemStatistics() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream()
                .filter(u -> u.getActive() != null && u.getActive())
                .count();
        long adminCount = userRepository.findAll().stream().filter(u -> u.getRole() == Role.ADMIN).count();
        long managerCount = userRepository.findAll().stream().filter(u -> u.getRole() == Role.MANAGER).count();
        long employeeCount = userRepository.findAll().stream().filter(u -> u.getRole() == Role.EMPLOYEE).count();

        return SystemStatisticsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalEmployees(employeeRepository.count())
                .totalProjects(projectRepository.count())
                .totalKnowledgeItems(knowledgeItemRepository.count())
                .adminCount(adminCount)
                .managerCount(managerCount)
                .employeeCount(employeeCount)
                .build();
    }

    private AdminUserResponse mapToResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .active(user.getActive())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private AuditLogResponse mapToResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .actorEmail(log.getActorEmail())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
