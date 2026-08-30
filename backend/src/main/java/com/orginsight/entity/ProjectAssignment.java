package com.orginsight.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Many-to-many link between Employee and Project (a project's team roster).
 * This is the real relational backbone the AI Insights module uses to reason
 * about team composition, bench (unassigned) capacity, and understaffed
 * projects - rather than relying on Project.manager as a free-text string.
 */
@Entity
@Table(name = "project_assignments",
        uniqueConstraints = @UniqueConstraint(name = "uk_project_employee", columnNames = {"project_id", "employee_id"}),
        indexes = {
                @Index(name = "idx_assignment_project", columnList = "project_id"),
                @Index(name = "idx_assignment_employee", columnList = "employee_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "role_on_project", length = 100)
    private String roleOnProject;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @jakarta.persistence.PrePersist
    protected void onCreate() {
        this.assignedAt = LocalDateTime.now();
    }
}
