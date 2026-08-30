package com.orginsight.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.orginsight.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    boolean existsByEmail(String email);
    boolean existsByEmployeeId(String employeeId);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByEmployeeIdAndIdNot(String employeeId, Long id);
    long countByStatus(String status);
    long countByDepartmentIsNotNull();

    @org.springframework.data.jpa.repository.Query("select count(distinct e.department) from Employee e where e.department is not null and e.department <> ''")
    long countDistinctDepartments();

    @org.springframework.data.jpa.repository.Query("select distinct e.department from Employee e where e.department is not null and e.department <> '' order by e.department")
    java.util.List<String> findDistinctDepartments();

    java.util.List<Employee> findAllByOrderByIdDesc(org.springframework.data.domain.Pageable pageable);
}
