package com.orginsight.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemStatisticsResponse {
    private long totalUsers;
    private long activeUsers;
    private long totalEmployees;
    private long totalProjects;
    private long totalKnowledgeItems;
    private long adminCount;
    private long managerCount;
    private long employeeCount;
}
