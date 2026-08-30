package com.orginsight.ai;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.orginsight.entity.Employee;
import com.orginsight.entity.KnowledgeItem;
import com.orginsight.entity.Project;
import com.orginsight.entity.ProjectAssignment;
import com.orginsight.repository.EmployeeRepository;
import com.orginsight.repository.KnowledgeItemRepository;
import com.orginsight.repository.ProjectAssignmentRepository;
import com.orginsight.repository.ProjectRepository;

/**
 * Builds a compact, aggregated, non-identifying summary of the organization's
 * current state to use as AI prompt context.
 *
 * Deliberately excludes PII (names, emails, phone numbers) - only counts and
 * category breakdowns are sent to the external AI provider. Uses the real
 * Employee<->Project relational data (ProjectAssignment) rather than
 * free-text fields, so team-composition and bench-capacity insights reflect
 * actual assignments.
 */
@Component
public class AiContextBuilder {

    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final ProjectAssignmentRepository assignmentRepository;

    public AiContextBuilder(EmployeeRepository employeeRepository,
                             ProjectRepository projectRepository,
                             KnowledgeItemRepository knowledgeItemRepository,
                             ProjectAssignmentRepository assignmentRepository) {
        this.employeeRepository = employeeRepository;
        this.projectRepository = projectRepository;
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public String buildOrganizationSummary() {
        List<Employee> employees = employeeRepository.findAll();
        List<Project> projects = projectRepository.findAll();
        List<KnowledgeItem> knowledgeItems = knowledgeItemRepository.findAll();

        long totalEmployees = employees.size();
        long activeEmployees = employees.stream().filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus())).count();

        Map<String, Long> byDepartment = employees.stream()
                .filter(e -> e.getDepartment() != null && !e.getDepartment().isBlank())
                .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));

        Map<String, Long> byProjectStatus = projects.stream()
                .filter(p -> p.getStatus() != null && !p.getStatus().isBlank())
                .collect(Collectors.groupingBy(Project::getStatus, Collectors.counting()));

        Map<String, Long> byPriority = projects.stream()
                .filter(p -> p.getPriority() != null && !p.getPriority().isBlank())
                .collect(Collectors.groupingBy(Project::getPriority, Collectors.counting()));

        long lowProgressProjects = projects.stream()
                .filter(p -> !"COMPLETED".equalsIgnoreCase(p.getStatus()))
                .filter(p -> p.getProgressPercent() != null && p.getProgressPercent() < 40)
                .count();

        // Real relational team-composition data (ProjectAssignment), not free text.
        List<Long> assignedEmployeeIds = assignmentRepository.findAllAssignedEmployeeIds();
        long benchEmployees = employees.stream()
                .filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus()))
                .filter(e -> !assignedEmployeeIds.contains(e.getId()))
                .count();

        long understaffedProjects = projects.stream()
                .filter(p -> !"COMPLETED".equalsIgnoreCase(p.getStatus()))
                .filter(p -> p.getTeamSize() != null && p.getTeamSize() > 0)
                .filter(p -> assignmentRepository.findByProjectId(p.getId()).size() < p.getTeamSize())
                .count();

        Map<String, Long> knowledgeByDepartment = knowledgeItems.stream()
                .filter(k -> k.getDepartment() != null && !k.getDepartment().isBlank())
                .collect(Collectors.groupingBy(KnowledgeItem::getDepartment, Collectors.counting()));

        long departmentsWithNoDocs = byDepartment.keySet().stream()
                .filter(dept -> !knowledgeByDepartment.containsKey(dept))
                .count();

        StringBuilder sb = new StringBuilder();
        sb.append("ORGANIZATION SNAPSHOT (aggregated, no personal data)\n");
        sb.append("Total employees: ").append(totalEmployees).append("\n");
        sb.append("Active employees: ").append(activeEmployees).append("\n");
        sb.append("Employees by department: ").append(byDepartment).append("\n");
        sb.append("Employees not currently assigned to any project (bench): ").append(benchEmployees).append("\n");
        sb.append("Total projects: ").append(projects.size()).append("\n");
        sb.append("Projects by status: ").append(byProjectStatus).append("\n");
        sb.append("Projects by priority: ").append(byPriority).append("\n");
        sb.append("Projects under 40% progress (excluding completed): ").append(lowProgressProjects).append("\n");
        sb.append("Projects with fewer assigned employees than their target team size: ").append(understaffedProjects).append("\n");
        sb.append("Knowledge base articles: ").append(knowledgeItems.size()).append("\n");
        sb.append("Knowledge base articles by department: ").append(knowledgeByDepartment).append("\n");
        sb.append("Departments with employees but zero knowledge base articles: ").append(departmentsWithNoDocs).append("\n");

        return sb.toString();
    }
}
