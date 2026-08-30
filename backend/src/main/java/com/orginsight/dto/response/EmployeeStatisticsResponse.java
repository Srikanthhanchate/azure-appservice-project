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
public class EmployeeStatisticsResponse {
    private long totalEmployees;
    private Map<String, Long> byDepartment;
    private Map<String, Long> byStatus;
    private Map<String, Long> byEmploymentType;
}
