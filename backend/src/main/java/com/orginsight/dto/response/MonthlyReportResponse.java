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
public class MonthlyReportResponse {
    private int year;
    private int month;
    private long newEmployees;
    private long newProjects;
    private long completedProjects;
}
