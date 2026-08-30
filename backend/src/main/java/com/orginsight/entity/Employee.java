package com.orginsight.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employees",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_employee_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_employee_employee_id", columnNames = "employee_id")
        },
        indexes = {
                @Index(name = "idx_employee_department", columnList = "department"),
                @Index(name = "idx_employee_role", columnList = "role"),
                @Index(name = "idx_employee_status", columnList = "status"),
                @Index(name = "idx_employee_full_name", columnList = "full_name")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false, unique = true, length = 50)
    private String employeeId;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(length = 100)
    private String department;

    @Column(length = 100)
    private String designation;

    @Column(name = "reporting_manager", length = 150)
    private String reportingManager;

    @Column(name = "joining_date", length = 20)
    private String joiningDate;

    @Column(name = "employment_type", length = 50)
    private String employmentType;

    @Column(length = 50)
    private String role;

    @Builder.Default
    @Column(length = 20)
    private String status = "ACTIVE";
}
