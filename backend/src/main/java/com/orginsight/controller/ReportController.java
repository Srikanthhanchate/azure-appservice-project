package com.orginsight.controller;

import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orginsight.dto.response.DashboardSummaryResponse;
import com.orginsight.dto.response.DepartmentReportResponse;
import com.orginsight.dto.response.EmployeeStatisticsResponse;
import com.orginsight.dto.response.MonthlyReportResponse;
import com.orginsight.dto.response.ProjectStatisticsResponse;
import com.orginsight.dto.response.YearlyReportResponse;
import com.orginsight.service.ReportService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/employees/summary")
    public ResponseEntity<EmployeeStatisticsResponse> employeeReport() {
        return ResponseEntity.ok(reportService.getEmployeeReport());
    }

    @GetMapping("/projects/summary")
    public ResponseEntity<ProjectStatisticsResponse> projectReport() {
        return ResponseEntity.ok(reportService.getProjectReport());
    }

    @GetMapping("/departments/summary")
    public ResponseEntity<List<DepartmentReportResponse>> departmentReport() {
        return ResponseEntity.ok(reportService.getDepartmentReport());
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummaryResponse> dashboardReport() {
        return ResponseEntity.ok(reportService.getDashboardReport());
    }

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> monthlyReport(@PathVariable int year, @PathVariable int month) {
        return ResponseEntity.ok(reportService.getMonthlyReport(year, month));
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> yearlyReport(@PathVariable int year) {
        return ResponseEntity.ok(reportService.getYearlyReport(year));
    }

    @GetMapping("/employees/export/csv")
    public ResponseEntity<byte[]> exportEmployeesCsv() {
        byte[] data = reportService.exportEmployeesCsv();
        return fileResponse(data, "employees_report.csv", "text/csv");
    }

    @GetMapping("/projects/export/csv")
    public ResponseEntity<byte[]> exportProjectsCsv() {
        byte[] data = reportService.exportProjectsCsv();
        return fileResponse(data, "projects_report.csv", "text/csv");
    }

    @GetMapping("/employees/export/pdf")
    public ResponseEntity<byte[]> exportEmployeesPdf() {
        byte[] data = reportService.exportEmployeesPdf();
        return fileResponse(data, "employees_report.pdf", "application/pdf");
    }

    @GetMapping("/projects/export/pdf")
    public ResponseEntity<byte[]> exportProjectsPdf() {
        byte[] data = reportService.exportProjectsPdf();
        return fileResponse(data, "projects_report.pdf", "application/pdf");
    }

    private ResponseEntity<byte[]> fileResponse(byte[] data, String filename, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }
}
