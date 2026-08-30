package com.orginsight.service;

import java.util.List;

import com.orginsight.dto.request.AssignEmployeeRequest;
import com.orginsight.dto.response.ProjectTeamMemberResponse;

public interface ProjectAssignmentService {
    ProjectTeamMemberResponse assignEmployee(Long projectId, AssignEmployeeRequest request);
    void unassignEmployee(Long projectId, Long employeeId);
    List<ProjectTeamMemberResponse> getTeam(Long projectId);
}
