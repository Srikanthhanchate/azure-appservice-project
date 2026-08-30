package com.orginsight.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {

    @NotBlank(message = "Employee ID is required")
    @Size(max = 50, message = "Employee ID must be at most 50 characters")
    private String employeeId;

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must be at most 150 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9\\-() ]{7,20}$", message = "Phone number is invalid")
    private String phoneNumber;

    private String department;
    private String designation;
    private String reportingManager;
    private String joiningDate;
    private String employmentType;
    private String role;
    private String status;
}
