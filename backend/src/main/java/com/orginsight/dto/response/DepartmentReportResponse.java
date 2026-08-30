package com.orginsight.dto.response;

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
public class DepartmentReportResponse {
    private String department;
    private long employeeCount;
    private long activeEmployeeCount;
    private long projectCount;
}
