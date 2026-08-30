package com.orginsight.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.orginsight.entity.ProjectAssignment;

@Repository
public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignment, Long> {
    List<ProjectAssignment> findByProjectId(Long projectId);
    List<ProjectAssignment> findByEmployeeId(Long employeeId);
    boolean existsByProjectIdAndEmployeeId(Long projectId, Long employeeId);
    void deleteByProjectIdAndEmployeeId(Long projectId, Long employeeId);

    @org.springframework.data.jpa.repository.Query(
            "select distinct a.employee.id from ProjectAssignment a")
    List<Long> findAllAssignedEmployeeIds();
}
