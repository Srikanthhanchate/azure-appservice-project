package com.orginsight.dto.response;

import java.util.List;
import java.util.Map;

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
public class DashboardSummaryResponse {
    private long totalEmployees;
    private long activeEmployees;
    private long totalProjects;
    private long departmentsCount;
    private long completedProjects;
    private long ongoingProjects;
    private long knowledgeItemsCount;
    private double averageProjectProgress;
    private List<EmployeeResponse> recentEmployees;
    private List<ProjectResponse> recentProjects;
    private List<ActivityItem> activityFeed;
    private Map<String, Long> projectStatusChart;
    private Map<String, Long> employeeDepartmentChart;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActivityItem {
        private String type;
        private String message;
        private String timestamp;
    }
}
