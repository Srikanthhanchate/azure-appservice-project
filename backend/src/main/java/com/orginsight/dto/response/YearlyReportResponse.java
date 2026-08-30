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
public class YearlyReportResponse {
    private int year;
    private long totalNewEmployees;
    private long totalNewProjects;
    private Map<Integer, Long> employeesByMonth;
    private Map<Integer, Long> projectsByMonth;
}
