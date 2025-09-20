package com.example.department.repo;

import com.example.department.domain.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    // Check if code exists
    boolean existsByCode(String code);

    // Find by code
    Optional<Department> findByCode(String code);

    // Filter methods for pagination
    Page<Department> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Department> findByCodeContainingIgnoreCase(String code, Pageable pageable);
    Page<Department> findByNameContainingIgnoreCaseAndCodeContainingIgnoreCase(String name, String code, Pageable pageable);
}
