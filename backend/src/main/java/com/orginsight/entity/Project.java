package com.orginsight.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "projects",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_project_project_id", columnNames = "project_id")
        },
        indexes = {
                @Index(name = "idx_project_status", columnList = "status"),
                @Index(name = "idx_project_priority", columnList = "priority"),
                @Index(name = "idx_project_manager", columnList = "manager")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false, unique = true, length = 50)
    private String projectId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(length = 100)
    private String manager;

    @Column(length = 30)
    private String priority;

    @Builder.Default
    @Column(length = 30)
    private String status = "PLANNED";

    @Column(name = "team_size")
    private Integer teamSize;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Builder.Default
    @Column(name = "progress_percent")
    private Integer progressPercent = 0;

    @Column(length = 100)
    private String department;
}
