package com.orginsight.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orginsight.dto.common.ApiResponse;
import com.orginsight.dto.request.AssignEmployeeRequest;
import com.orginsight.dto.response.ProjectTeamMemberResponse;
import com.orginsight.service.ProjectAssignmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects/{projectId}/team")
public class ProjectAssignmentController {

    private final ProjectAssignmentService projectAssignmentService;

    public ProjectAssignmentController(ProjectAssignmentService projectAssignmentService) {
        this.projectAssignmentService = projectAssignmentService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectTeamMemberResponse>> getTeam(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectAssignmentService.getTeam(projectId));
    }

    @PostMapping
    public ResponseEntity<ProjectTeamMemberResponse> assignEmployee(@PathVariable Long projectId,
                                                                      @Valid @RequestBody AssignEmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectAssignmentService.assignEmployee(projectId, request));
    }

    @DeleteMapping("/{employeeId}")
    public ResponseEntity<ApiResponse<Void>> unassignEmployee(@PathVariable Long projectId, @PathVariable Long employeeId) {
        projectAssignmentService.unassignEmployee(projectId, employeeId);
        return ResponseEntity.ok(ApiResponse.ok("Employee removed from project", null));
    }
}
