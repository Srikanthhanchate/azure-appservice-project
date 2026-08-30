package com.orginsight.service;

import java.time.LocalDate;
import java.util.List;

import com.orginsight.dto.common.PageResponse;
import com.orginsight.dto.request.ProjectRequest;
import com.orginsight.dto.response.ProjectResponse;
import com.orginsight.dto.response.ProjectStatisticsResponse;

public interface ProjectService {
    ProjectResponse createProject(ProjectRequest request);
    List<ProjectResponse> getAllProjects();
    ProjectResponse getProjectById(Long id);
    ProjectResponse updateProject(Long id, ProjectRequest request);
    void deleteProject(Long id);

    PageResponse<ProjectResponse> searchProjects(String search, String status, String priority, String manager,
                                                  LocalDate startDateFrom, LocalDate endDateTo,
                                                  int page, int size, String sortBy, String sortDir);

    ProjectStatisticsResponse getStatistics();
    List<ProjectResponse> getRecentProjects(int limit);
}
