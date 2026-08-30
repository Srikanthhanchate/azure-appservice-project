package com.orginsight.dto.response;

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
public class ProjectStatisticsResponse {
    private long totalProjects;
    private Map<String, Long> byStatus;
    private Map<String, Long> byPriority;
    private long totalTeamMembersAllocated;
    private double averageProgress;
}
