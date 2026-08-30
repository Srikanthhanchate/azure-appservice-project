package com.orginsight.service;

import java.util.List;

import com.orginsight.dto.common.PageResponse;
import com.orginsight.dto.request.EmployeeRequest;
import com.orginsight.dto.response.EmployeeResponse;
import com.orginsight.dto.response.EmployeeStatisticsResponse;

public interface EmployeeService {
    EmployeeResponse createEmployee(EmployeeRequest request);
    List<EmployeeResponse> getAllEmployees();
    EmployeeResponse getEmployeeById(Long id);
    EmployeeResponse updateEmployee(Long id, EmployeeRequest request);
    void deleteEmployee(Long id);

    PageResponse<EmployeeResponse> searchEmployees(String search, String department, String role, String status,
                                                    int page, int size, String sortBy, String sortDir);

    EmployeeStatisticsResponse getStatistics();
    List<EmployeeResponse> getRecentEmployees(int limit);
    List<String> getDepartments();
}
