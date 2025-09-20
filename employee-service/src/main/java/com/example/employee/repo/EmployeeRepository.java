package com.example.employee.repo;

import com.example.employee.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmail(String email);

    // 1. GET /employees — pagination (page, size), sorting (sort=lastName,asc), filters (email, lastName contains, departmentId).
    // Filter methods
    Page<Employee> findByEmail(String email, Pageable pageable);
    Page<Employee> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);
    Page<Employee> findByDepartmentId(Long departmentId, Pageable pageable);

    // Combined filters
    Page<Employee> findByEmailAndLastNameContainingIgnoreCase(String email, String lastName, Pageable pageable);
    Page<Employee> findByEmailAndDepartmentId(String email, Long departmentId, Pageable pageable);
    Page<Employee> findByLastNameContainingIgnoreCaseAndDepartmentId(String lastName, Long departmentId, Pageable pageable);
    Page<Employee> findByEmailAndLastNameContainingIgnoreCaseAndDepartmentId(String email, String lastName, Long departmentId, Pageable pageable);

    // 7. GET /employees/search — convenience endpoint for case-insensitive name/email search
    @Query("SELECT e FROM Employee e WHERE " +
            "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Employee> searchByNameOrEmail(@Param("query") String query, Pageable pageable);

    // 8. GET /employees/stats — simple metrics (e.g., counts by departmentId).
    @Query("SELECT e.departmentId, COUNT(e) FROM Employee e WHERE e.departmentId IS NOT NULL GROUP BY e.departmentId")
    List<Object[]> countEmployeesByDepartment();

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.departmentId IS NULL")
    long countEmployeesWithoutDepartment();
}
