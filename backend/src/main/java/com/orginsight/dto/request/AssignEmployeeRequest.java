package com.orginsight.dto.request;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignEmployeeRequest {

    @NotNull(message = "Employee id is required")
    private Long employeeId;

    private String roleOnProject;
}
