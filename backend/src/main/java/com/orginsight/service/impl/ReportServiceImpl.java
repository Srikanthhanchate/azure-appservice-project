package com.orginsight.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orginsight.dto.response.DashboardSummaryResponse;
import com.orginsight.dto.response.DepartmentReportResponse;
import com.orginsight.dto.response.EmployeeStatisticsResponse;
import com.orginsight.dto.response.MonthlyReportResponse;
import com.orginsight.dto.response.ProjectStatisticsResponse;
import com.orginsight.dto.response.YearlyReportResponse;
import com.orginsight.entity.Employee;
import com.orginsight.entity.Project;
import com.orginsight.repository.EmployeeRepository;
import com.orginsight.repository.ProjectRepository;
import com.orginsight.service.DashboardService;
import com.orginsight.service.EmployeeService;
import com.orginsight.service.ProjectService;
import com.orginsight.service.ReportService;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private static final DateTimeFormatter[] DATE_FORMATS = new DateTimeFormatter[]{
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
    };

    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeService employeeService;
    private final ProjectService projectService;
    private final DashboardService dashboardService;

    public ReportServiceImpl(EmployeeRepository employeeRepository,
                              ProjectRepository projectRepository,
                              EmployeeService employeeService,
                              ProjectService projectService,
                              DashboardService dashboardService) {
        this.employeeRepository = employeeRepository;
        this.projectRepository = projectRepository;
        this.employeeService = employeeService;
        this.projectService = projectService;
        this.dashboardService = dashboardService;
    }

    @Override
    public EmployeeStatisticsResponse getEmployeeReport() {
        return employeeService.getStatistics();
    }

    @Override
    public ProjectStatisticsResponse getProjectReport() {
        return projectService.getStatistics();
    }

    @Override
    public List<DepartmentReportResponse> getDepartmentReport() {
        List<Employee> employees = employeeRepository.findAll();
        List<Project> projects = projectRepository.findAll();

        Map<String, List<Employee>> byDept = employees.stream()
                .filter(e -> e.getDepartment() != null && !e.getDepartment().isBlank())
                .collect(Collectors.groupingBy(Employee::getDepartment));

        return byDept.entrySet().stream()
                .map(entry -> {
                    String dept = entry.getKey();
                    long count = entry.getValue().size();
                    long active = entry.getValue().stream()
                            .filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus()))
                            .count();
                    long projectCount = projects.stream()
                            .filter(p -> dept.equalsIgnoreCase(p.getDepartment()))
                            .count();
                    return DepartmentReportResponse.builder()
                            .department(dept)
                            .employeeCount(count)
                            .activeEmployeeCount(active)
                            .projectCount(projectCount)
                            .build();
                })
                .sorted((a, b) -> a.getDepartment().compareToIgnoreCase(b.getDepartment()))
                .collect(Collectors.toList());
    }

    @Override
    public DashboardSummaryResponse getDashboardReport() {
        return dashboardService.getSummary();
    }

    @Override
    public MonthlyReportResponse getMonthlyReport(int year, int month) {
        List<Employee> employees = employeeRepository.findAll();
        List<Project> projects = projectRepository.findAll();

        long newEmployees = employees.stream()
                .map(e -> parseDate(e.getJoiningDate()))
                .filter(d -> d != null && d.getYear() == year && d.getMonthValue() == month)
                .count();

        long newProjects = projects.stream()
                .filter(p -> p.getStartDate() != null && p.getStartDate().getYear() == year
                        && p.getStartDate().getMonthValue() == month)
                .count();

        long completedProjects = projects.stream()
                .filter(p -> "COMPLETED".equalsIgnoreCase(p.getStatus())
                        && p.getEndDate() != null && p.getEndDate().getYear() == year
                        && p.getEndDate().getMonthValue() == month)
                .count();

        return MonthlyReportResponse.builder()
                .year(year)
                .month(month)
                .newEmployees(newEmployees)
                .newProjects(newProjects)
                .completedProjects(completedProjects)
                .build();
    }

    @Override
    public YearlyReportResponse getYearlyReport(int year) {
        List<Employee> employees = employeeRepository.findAll();
        List<Project> projects = projectRepository.findAll();

        Map<Integer, Long> employeesByMonth = new HashMap<>();
        Map<Integer, Long> projectsByMonth = new HashMap<>();
        for (int m = 1; m <= 12; m++) {
            final int month = m;
            long empCount = employees.stream()
                    .map(e -> parseDate(e.getJoiningDate()))
                    .filter(d -> d != null && d.getYear() == year && d.getMonthValue() == month)
                    .count();
            long projCount = projects.stream()
                    .filter(p -> p.getStartDate() != null && p.getStartDate().getYear() == year
                            && p.getStartDate().getMonthValue() == month)
                    .count();
            employeesByMonth.put(month, empCount);
            projectsByMonth.put(month, projCount);
        }

        return YearlyReportResponse.builder()
                .year(year)
                .totalNewEmployees(employeesByMonth.values().stream().mapToLong(Long::longValue).sum())
                .totalNewProjects(projectsByMonth.values().stream().mapToLong(Long::longValue).sum())
                .employeesByMonth(employeesByMonth)
                .projectsByMonth(projectsByMonth)
                .build();
    }

    @Override
    public byte[] exportEmployeesCsv() {
        List<Employee> employees = employeeRepository.findAll();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader("Employee ID", "Full Name", "Email", "Phone", "Department", "Designation",
                             "Manager", "Joining Date", "Employment Type", "Role", "Status")
                     .build())) {
            for (Employee e : employees) {
                printer.printRecord(e.getEmployeeId(), e.getFullName(), e.getEmail(), e.getPhoneNumber(),
                        e.getDepartment(), e.getDesignation(), e.getReportingManager(), e.getJoiningDate(),
                        e.getEmploymentType(), e.getRole(), e.getStatus());
            }
            printer.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate employee CSV report", e);
        }
    }

    @Override
    public byte[] exportProjectsCsv() {
        List<Project> projects = projectRepository.findAll();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader("Project ID", "Name", "Manager", "Priority", "Status", "Team Size",
                             "Start Date", "End Date", "Progress %", "Department")
                     .build())) {
            for (Project p : projects) {
                printer.printRecord(p.getProjectId(), p.getName(), p.getManager(), p.getPriority(), p.getStatus(),
                        p.getTeamSize(), p.getStartDate(), p.getEndDate(), p.getProgressPercent(), p.getDepartment());
            }
            printer.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate project CSV report", e);
        }
    }

    @Override
    public byte[] exportEmployeesPdf() {
        List<Employee> employees = employeeRepository.findAll();
        List<String> lines = employees.stream()
                .map(e -> String.format("%s | %s | %s | %s | %s | %s",
                        nullSafe(e.getEmployeeId()), nullSafe(e.getFullName()), nullSafe(e.getEmail()),
                        nullSafe(e.getDepartment()), nullSafe(e.getDesignation()), nullSafe(e.getStatus())))
                .collect(Collectors.toList());
        return renderSimplePdf("Employee Report", "ID | Name | Email | Department | Designation | Status", lines);
    }

    @Override
    public byte[] exportProjectsPdf() {
        List<Project> projects = projectRepository.findAll();
        List<String> lines = projects.stream()
                .map(p -> String.format("%s | %s | %s | %s | %s%%",
                        nullSafe(p.getProjectId()), nullSafe(p.getName()), nullSafe(p.getStatus()),
                        nullSafe(p.getPriority()), p.getProgressPercent() == null ? 0 : p.getProgressPercent()))
                .collect(Collectors.toList());
        return renderSimplePdf("Project Report", "ID | Name | Status | Priority | Progress", lines);
    }

    private byte[] renderSimplePdf(String title, String header, List<String> lines) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream content = new PDPageContentStream(document, page);

            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float y = yStart;
            float leading = 16f;

            content.beginText();
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
            content.newLineAtOffset(margin, y);
            content.showText(title);
            content.endText();
            y -= 2 * leading;

            content.beginText();
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
            content.newLineAtOffset(margin, y);
            content.showText(header);
            content.endText();
            y -= leading;

            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
            for (String line : lines) {
                if (y < margin) {
                    content.close();
                    PDPage newPage = new PDPage(PDRectangle.A4);
                    document.addPage(newPage);
                    content = new PDPageContentStream(document, newPage);
                    y = yStart;
                }
                content.beginText();
                content.newLineAtOffset(margin, y);
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                content.showText(sanitize(line));
                content.endText();
                y -= leading;
            }
            content.close();

            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private String sanitize(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[^\\x20-\\x7E]", "?");
    }

    private String nullSafe(String value) {
        return value == null ? "-" : value;
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                return LocalDate.parse(raw.trim(), fmt);
            } catch (DateTimeParseException ignored) {
                // try next format
            }
        }
        return null;
    }
}
