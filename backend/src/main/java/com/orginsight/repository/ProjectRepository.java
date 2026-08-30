package com.orginsight.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.orginsight.entity.Project;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    boolean existsByProjectId(String projectId);
    boolean existsByProjectIdAndIdNot(String projectId, Long id);
    long countByStatus(String status);
    List<Project> findAllByOrderByIdDesc(org.springframework.data.domain.Pageable pageable);

    @Query("select coalesce(sum(p.teamSize), 0) from Project p")
    long sumTeamSize();

    @Query("select coalesce(avg(p.progressPercent), 0) from Project p")
    Double averageProgress();
}
