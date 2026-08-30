package com.orginsight.service.impl;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orginsight.dto.response.DashboardSummaryResponse;
import com.orginsight.dto.response.EmployeeResponse;
import com.orginsight.dto.response.ProjectResponse;
import com.orginsight.entity.Employee;
import com.orginsight.entity.Project;
import com.orginsight.repository.EmployeeRepository;
import com.orginsight.repository.KnowledgeItemRepository;
import com.orginsight.repository.ProjectRepository;
import com.orginsight.service.DashboardService;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;

    public DashboardServiceImpl(EmployeeRepository employeeRepository,
                                 ProjectRepository projectRepository,
                                 KnowledgeItemRepository knowledgeItemRepository) {
        this.employeeRepository = employeeRepository;
        this.projectRepository = projectRepository;
        this.knowledgeItemRepository = knowledgeItemRepository;
    }

    @Override
    public DashboardSummaryResponse getSummary() {
        List<Employee> employees = employeeRepository.findAll();
        List<Project> projects = projectRepository.findAll();

        long totalEmployees = employees.size();
        long activeEmployees = employees.stream()
                .filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus()))
                .count();
        long departmentsCount = employeeRepository.countDistinctDepartments();

        long totalProjects = projects.size();
        long completedProjects = projects.stream()
                .filter(p -> "COMPLETED".equalsIgnoreCase(p.getStatus()))
                .count();
        long ongoingProjects = projects.stream()
                .filter(p -> "IN_PROGRESS".equalsIgnoreCase(p.getStatus()) || "ONGOING".equalsIgnoreCase(p.getStatus()))
                .count();

        Double avgProgress = projectRepository.averageProgress();

        Map<String, Long> projectStatusChart = projects.stream()
                .filter(p -> p.getStatus() != null && !p.getStatus().isBlank())
                .collect(Collectors.groupingBy(Project::getStatus, Collectors.counting()));

        Map<String, Long> employeeDepartmentChart = employees.stream()
                .filter(e -> e.getDepartment() != null && !e.getDepartment().isBlank())
                .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));

        Pageable top5 = PageRequest.of(0, 5);
        List<EmployeeResponse> recentEmployees = employeeRepository.findAllByOrderByIdDesc(top5).stream()
                .map(this::mapEmployee)
                .collect(Collectors.toList());

        List<ProjectResponse> recentProjects = projectRepository.findAllByOrderByIdDesc(top5).stream()
                .map(this::mapProject)
                .collect(Collectors.toList());

        List<DashboardSummaryResponse.ActivityItem> activityFeed = buildActivityFeed(recentEmployees, recentProjects);

        return DashboardSummaryResponse.builder()
                .totalEmployees(totalEmployees)
                .activeEmployees(activeEmployees)
                .totalProjects(totalProjects)
                .departmentsCount(departmentsCount)
                .completedProjects(completedProjects)
                .ongoingProjects(ongoingProjects)
                .knowledgeItemsCount(knowledgeItemRepository.count())
                .averageProjectProgress(avgProgress == null ? 0.0 : avgProgress)
                .recentEmployees(recentEmployees)
                .recentProjects(recentProjects)
                .activityFeed(activityFeed)
                .projectStatusChart(projectStatusChart)
                .employeeDepartmentChart(employeeDepartmentChart)
                .build();
    }

    private List<DashboardSummaryResponse.ActivityItem> buildActivityFeed(List<EmployeeResponse> recentEmployees,
                                                                            List<ProjectResponse> recentProjects) {
        java.util.List<DashboardSummaryResponse.ActivityItem> feed = new java.util.ArrayList<>();
        for (EmployeeResponse e : recentEmployees) {
            feed.add(DashboardSummaryResponse.ActivityItem.builder()
                    .type("EMPLOYEE_ADDED")
                    .message(e.getFullName() + " joined as " + (e.getDesignation() == null ? "a team member" : e.getDesignation()))
                    .timestamp(java.time.LocalDateTime.now().format(TS_FORMAT))
                    .build());
        }
        for (ProjectResponse p : recentProjects) {
            feed.add(DashboardSummaryResponse.ActivityItem.builder()
                    .type("PROJECT_CREATED")
                    .message("Project '" + p.getName() + "' was created")
                    .timestamp(java.time.LocalDateTime.now().format(TS_FORMAT))
                    .build());
        }
        return feed;
    }

    private EmployeeResponse mapEmployee(Employee e) {
        return EmployeeResponse.builder()
                .id(e.getId())
                .employeeId(e.getEmployeeId())
                .fullName(e.getFullName())
                .email(e.getEmail())
                .phoneNumber(e.getPhoneNumber())
                .department(e.getDepartment())
                .designation(e.getDesignation())
                .reportingManager(e.getReportingManager())
                .joiningDate(e.getJoiningDate())
                .employmentType(e.getEmploymentType())
                .role(e.getRole())
                .status(e.getStatus())
                .build();
    }

    private ProjectResponse mapProject(Project p) {
        return ProjectResponse.builder()
                .id(p.getId())
                .projectId(p.getProjectId())
                .name(p.getName())
                .description(p.getDescription())
                .manager(p.getManager())
                .priority(p.getPriority())
                .status(p.getStatus())
                .teamSize(p.getTeamSize())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .progressPercent(p.getProgressPercent())
                .department(p.getDepartment())
                .build();
    }
}
