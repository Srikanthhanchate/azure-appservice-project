package com.orginsight.service;

import java.util.List;

import com.orginsight.dto.response.DashboardSummaryResponse;
import com.orginsight.dto.response.DepartmentReportResponse;
import com.orginsight.dto.response.EmployeeStatisticsResponse;
import com.orginsight.dto.response.MonthlyReportResponse;
import com.orginsight.dto.response.ProjectStatisticsResponse;
import com.orginsight.dto.response.YearlyReportResponse;

public interface ReportService {
    EmployeeStatisticsResponse getEmployeeReport();
    ProjectStatisticsResponse getProjectReport();
    List<DepartmentReportResponse> getDepartmentReport();
    DashboardSummaryResponse getDashboardReport();
    MonthlyReportResponse getMonthlyReport(int year, int month);
    YearlyReportResponse getYearlyReport(int year);

    byte[] exportEmployeesCsv();
    byte[] exportProjectsCsv();
    byte[] exportEmployeesPdf();
    byte[] exportProjectsPdf();
}
