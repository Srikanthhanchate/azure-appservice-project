package com.orginsight.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {

    @NotBlank(message = "Project ID is required")
    @Size(max = 50, message = "Project ID must be at most 50 characters")
    private String projectId;

    @NotBlank(message = "Project name is required")
    @Size(max = 200, message = "Project name must be at most 200 characters")
    private String name;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    private String manager;

    @NotBlank(message = "Priority is required")
    private String priority;

    @NotBlank(message = "Status is required")
    private String status;

    @Min(value = 0, message = "Team size cannot be negative")
    private Integer teamSize;

    private LocalDate startDate;

    private LocalDate endDate;

    @Min(value = 0, message = "Progress cannot be negative")
    private Integer progressPercent;

    private String department;
}
