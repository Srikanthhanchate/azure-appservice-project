package com.orginsight.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orginsight.dto.common.PageResponse;
import com.orginsight.dto.request.ProjectRequest;
import com.orginsight.dto.response.ProjectResponse;
import com.orginsight.dto.response.ProjectStatisticsResponse;
import com.orginsight.entity.Project;
import com.orginsight.exception.DuplicateResourceException;
import com.orginsight.exception.ProjectNotFoundException;
import com.orginsight.repository.ProjectRepository;
import com.orginsight.service.ProjectService;
import com.orginsight.specification.ProjectSpecification;

import java.time.LocalDate;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public ProjectResponse createProject(ProjectRequest request) {
        if (projectRepository.existsByProjectId(request.getProjectId())) {
            throw new DuplicateResourceException("A project with ID '" + request.getProjectId() + "' already exists.");
        }
        Project project = new Project();
        mapRequestToEntity(request, project);
        return mapToResponse(projectRepository.save(project));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id " + id));
        return mapToResponse(project);
    }

    @Override
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id " + id));

        if (projectRepository.existsByProjectIdAndIdNot(request.getProjectId(), id)) {
            throw new DuplicateResourceException("A project with ID '" + request.getProjectId() + "' already exists.");
        }

        mapRequestToEntity(request, project);
        return mapToResponse(projectRepository.save(project));
    }

    @Override
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id " + id));
        projectRepository.delete(project);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> searchProjects(String search, String status, String priority, String manager,
                                                          LocalDate startDateFrom, LocalDate endDateTo,
                                                          int page, int size, String sortBy, String sortDir) {
        String sortField = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(direction, sortField));

        Page<Project> result = projectRepository.findAll(
                ProjectSpecification.filterBy(search, status, priority, manager, startDateFrom, endDateTo), pageable);

        return PageResponse.from(result.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectStatisticsResponse getStatistics() {
        List<Project> all = projectRepository.findAll();

        Map<String, Long> byStatus = all.stream()
                .filter(p -> p.getStatus() != null && !p.getStatus().isBlank())
                .collect(Collectors.groupingBy(Project::getStatus, Collectors.counting()));

        Map<String, Long> byPriority = all.stream()
                .filter(p -> p.getPriority() != null && !p.getPriority().isBlank())
                .collect(Collectors.groupingBy(Project::getPriority, Collectors.counting()));

        long totalTeamMembers = projectRepository.sumTeamSize();
        Double avgProgress = projectRepository.averageProgress();

        return ProjectStatisticsResponse.builder()
                .totalProjects(all.size())
                .byStatus(byStatus)
                .byPriority(byPriority)
                .totalTeamMembersAllocated(totalTeamMembers)
                .averageProgress(avgProgress == null ? 0.0 : avgProgress)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getRecentProjects(int limit) {
        Pageable pageable = PageRequest.of(0, Math.max(limit, 1));
        return projectRepository.findAllByOrderByIdDesc(pageable).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void mapRequestToEntity(ProjectRequest request, Project project) {
        project.setProjectId(request.getProjectId());
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setManager(request.getManager());
        project.setPriority(request.getPriority());
        project.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? "PLANNED" : request.getStatus());
        project.setTeamSize(request.getTeamSize());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setProgressPercent(request.getProgressPercent() == null ? 0 : request.getProgressPercent());
        project.setDepartment(request.getDepartment());
    }

    private ProjectResponse mapToResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .projectId(project.getProjectId())
                .name(project.getName())
                .description(project.getDescription())
                .manager(project.getManager())
                .priority(project.getPriority())
                .status(project.getStatus())
                .teamSize(project.getTeamSize())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .progressPercent(project.getProgressPercent())
                .department(project.getDepartment())
                .build();
    }
}
