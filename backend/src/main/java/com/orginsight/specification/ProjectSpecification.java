package com.orginsight.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.orginsight.entity.Project;

import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class ProjectSpecification {

    private ProjectSpecification() {
    }

    public static Specification<Project> filterBy(String search, String status, String priority,
                                                    String manager, LocalDate startDateFrom, LocalDate endDateTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(search)) {
                String like = "%" + search.toLowerCase() + "%";
                Predicate byName = cb.like(cb.lower(root.get("name")), like);
                Predicate byProjectId = cb.like(cb.lower(root.get("projectId")), like);
                predicates.add(cb.or(byName, byProjectId));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
            }
            if (StringUtils.hasText(priority)) {
                predicates.add(cb.equal(cb.lower(root.get("priority")), priority.toLowerCase()));
            }
            if (StringUtils.hasText(manager)) {
                predicates.add(cb.equal(cb.lower(root.get("manager")), manager.toLowerCase()));
            }
            if (startDateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), startDateFrom));
            }
            if (endDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), endDateTo));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
