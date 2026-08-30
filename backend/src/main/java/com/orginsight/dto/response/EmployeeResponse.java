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
public class EmployeeResponse {
    private Long id;
    private String employeeId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String department;
    private String designation;
    private String reportingManager;
    private String joiningDate;
    private String employmentType;
    private String role;
    private String status;
}
