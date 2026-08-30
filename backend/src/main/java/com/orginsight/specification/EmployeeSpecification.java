package com.orginsight.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.orginsight.entity.Employee;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public final class EmployeeSpecification {

    private EmployeeSpecification() {
    }

    public static Specification<Employee> filterBy(String search, String department, String role, String status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(search)) {
                String like = "%" + search.toLowerCase() + "%";
                Predicate byName = cb.like(cb.lower(root.get("fullName")), like);
                Predicate byEmail = cb.like(cb.lower(root.get("email")), like);
                Predicate byEmployeeId = cb.like(cb.lower(root.get("employeeId")), like);
                predicates.add(cb.or(byName, byEmail, byEmployeeId));
            }
            if (StringUtils.hasText(department)) {
                predicates.add(cb.equal(cb.lower(root.get("department")), department.toLowerCase()));
            }
            if (StringUtils.hasText(role)) {
                predicates.add(cb.equal(cb.lower(root.get("role")), role.toLowerCase()));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
