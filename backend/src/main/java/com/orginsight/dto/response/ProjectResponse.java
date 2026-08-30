package com.orginsight.dto.response;

import java.time.LocalDate;

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
public class ProjectResponse {
    private Long id;
    private String projectId;
    private String name;
    private String description;
    private String manager;
    private String priority;
    private String status;
    private Integer teamSize;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer progressPercent;
    private String department;
}
