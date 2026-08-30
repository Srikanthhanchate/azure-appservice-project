package com.orginsight.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orginsight.dto.request.AssignEmployeeRequest;
import com.orginsight.dto.response.ProjectTeamMemberResponse;
import com.orginsight.entity.Employee;
import com.orginsight.entity.Project;
import com.orginsight.entity.ProjectAssignment;
import com.orginsight.exception.DuplicateResourceException;
import com.orginsight.exception.EmployeeNotFoundException;
import com.orginsight.exception.ProjectNotFoundException;
import com.orginsight.repository.EmployeeRepository;
import com.orginsight.repository.ProjectAssignmentRepository;
import com.orginsight.repository.ProjectRepository;
import com.orginsight.service.ProjectAssignmentService;

@Service
@Transactional
public class ProjectAssignmentServiceImpl implements ProjectAssignmentService {

    private final ProjectAssignmentRepository assignmentRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;

    public ProjectAssignmentServiceImpl(ProjectAssignmentRepository assignmentRepository,
                                         ProjectRepository projectRepository,
                                         EmployeeRepository employeeRepository) {
        this.assignmentRepository = assignmentRepository;
        this.projectRepository = projectRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public ProjectTeamMemberResponse assignEmployee(Long projectId, AssignEmployeeRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id " + projectId));
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id " + request.getEmployeeId()));

        if (assignmentRepository.existsByProjectIdAndEmployeeId(projectId, request.getEmployeeId())) {
            throw new DuplicateResourceException("This employee is already assigned to this project.");
        }

        ProjectAssignment assignment = ProjectAssignment.builder()
                .project(project)
                .employee(employee)
                .roleOnProject(request.getRoleOnProject())
                .build();
        assignmentRepository.save(assignment);

        return mapToResponse(assignment);
    }

    @Override
    public void unassignEmployee(Long projectId, Long employeeId) {
        assignmentRepository.deleteByProjectIdAndEmployeeId(projectId, employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectTeamMemberResponse> getTeam(Long projectId) {
        return assignmentRepository.findByProjectId(projectId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ProjectTeamMemberResponse mapToResponse(ProjectAssignment assignment) {
        Employee employee = assignment.getEmployee();
        return ProjectTeamMemberResponse.builder()
                .employeeId(employee.getId())
                .fullName(employee.getFullName())
                .designation(employee.getDesignation())
                .department(employee.getDepartment())
                .roleOnProject(assignment.getRoleOnProject())
                .build();
    }
}
