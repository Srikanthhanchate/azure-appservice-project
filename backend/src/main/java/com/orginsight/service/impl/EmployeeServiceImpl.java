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
import com.orginsight.dto.request.EmployeeRequest;
import com.orginsight.dto.response.EmployeeResponse;
import com.orginsight.dto.response.EmployeeStatisticsResponse;
import com.orginsight.entity.Employee;
import com.orginsight.exception.DuplicateResourceException;
import com.orginsight.exception.EmployeeNotFoundException;
import com.orginsight.repository.EmployeeRepository;
import com.orginsight.service.EmployeeService;
import com.orginsight.specification.EmployeeSpecification;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An employee with email '" + request.getEmail() + "' already exists.");
        }
        if (employeeRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new DuplicateResourceException("An employee with ID '" + request.getEmployeeId() + "' already exists.");
        }
        Employee employee = new Employee();
        mapRequestToEntity(request, employee);
        return mapToResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id " + id));
        return mapToResponse(employee);
    }

    @Override
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id " + id));

        if (employeeRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateResourceException("An employee with email '" + request.getEmail() + "' already exists.");
        }
        if (employeeRepository.existsByEmployeeIdAndIdNot(request.getEmployeeId(), id)) {
            throw new DuplicateResourceException("An employee with ID '" + request.getEmployeeId() + "' already exists.");
        }

        mapRequestToEntity(request, employee);
        return mapToResponse(employeeRepository.save(employee));
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id " + id));
        employeeRepository.delete(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> searchEmployees(String search, String department, String role, String status,
                                                            int page, int size, String sortBy, String sortDir) {
        String sortField = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(direction, sortField));

        Page<Employee> result = employeeRepository.findAll(
                EmployeeSpecification.filterBy(search, department, role, status), pageable);

        Page<EmployeeResponse> mapped = result.map(this::mapToResponse);
        return PageResponse.from(mapped);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeStatisticsResponse getStatistics() {
        List<Employee> all = employeeRepository.findAll();

        Map<String, Long> byDepartment = all.stream()
                .filter(e -> e.getDepartment() != null && !e.getDepartment().isBlank())
                .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));

        Map<String, Long> byStatus = all.stream()
                .filter(e -> e.getStatus() != null && !e.getStatus().isBlank())
                .collect(Collectors.groupingBy(Employee::getStatus, Collectors.counting()));

        Map<String, Long> byEmploymentType = all.stream()
                .filter(e -> e.getEmploymentType() != null && !e.getEmploymentType().isBlank())
                .collect(Collectors.groupingBy(Employee::getEmploymentType, Collectors.counting()));

        return EmployeeStatisticsResponse.builder()
                .totalEmployees(all.size())
                .byDepartment(byDepartment)
                .byStatus(byStatus)
                .byEmploymentType(byEmploymentType)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getRecentEmployees(int limit) {
        Pageable pageable = PageRequest.of(0, Math.max(limit, 1));
        return employeeRepository.findAllByOrderByIdDesc(pageable).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDepartments() {
        return employeeRepository.findDistinctDepartments();
    }

    private void mapRequestToEntity(EmployeeRequest request, Employee employee) {
        employee.setEmployeeId(request.getEmployeeId());
        employee.setFullName(request.getFullName());
        employee.setEmail(request.getEmail());
        employee.setPhoneNumber(request.getPhoneNumber());
        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());
        employee.setReportingManager(request.getReportingManager());
        employee.setJoiningDate(request.getJoiningDate());
        employee.setEmploymentType(request.getEmploymentType());
        employee.setRole(request.getRole());
        employee.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? "ACTIVE" : request.getStatus());
    }

    private EmployeeResponse mapToResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .employeeId(employee.getEmployeeId())
                .fullName(employee.getFullName())
                .email(employee.getEmail())
                .phoneNumber(employee.getPhoneNumber())
                .department(employee.getDepartment())
                .designation(employee.getDesignation())
                .reportingManager(employee.getReportingManager())
                .joiningDate(employee.getJoiningDate())
                .employmentType(employee.getEmploymentType())
                .role(employee.getRole())
                .status(employee.getStatus())
                .build();
    }
}
