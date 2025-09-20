package com.example.department.web;

import com.example.department.domain.Department;
import com.example.department.repo.DepartmentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository repository;

    // Step 1: GET /departments — pagination, sorting, filter by name (contains) and code.
    @GetMapping
    public Page<Department> all(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            Pageable pageable) {

        if (name != null && code != null) {
            return repository.findByNameContainingIgnoreCaseAndCodeContainingIgnoreCase(name, code, pageable);
        } else if (name != null) {
            return repository.findByNameContainingIgnoreCase(name, pageable);
        } else if (code != null) {
            return repository.findByCodeContainingIgnoreCase(code, pageable);
        } else {
            return repository.findAll(pageable);
        }
    }

    // Step 2: GET /departments/{id} — detail
    @GetMapping("/{id}")
    public Department byId(@PathVariable Long id) {
        return repository.findById(id).orElseThrow();
    }

    // Step 3: POST /departments — create with unique code (short string identifier).
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Department create(@Valid @RequestBody Department d) {
        if (repository.existsByCode(d.getCode())) {
            throw new IllegalArgumentException("Code already exists");
        }
        return repository.save(d);
    }

    // Step 4: PUT /departments/{id} — full update; code remains unique.
    @PutMapping("/{id}")
    public Department update(@PathVariable Long id, @Valid @RequestBody Department d) {
        Department existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found"));

        // Check if code is being changed and if new code already exists
        if (!existing.getCode().equals(d.getCode()) && repository.existsByCode(d.getCode())) {
            throw new IllegalArgumentException("Code already exists");
        }

        existing.setName(d.getName());
        existing.setCode(d.getCode());
        existing.setDescription(d.getDescription());

        return repository.save(existing);
    }

    // Step 5: PATCH /departments/{id} — partial update (e.g., managerEmail).
    @PatchMapping("/{id}")
    public Department partialUpdate(@PathVariable Long id, @Valid @RequestBody Department d) {
        Department existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found"));

        if (d.getName() != null) {
            existing.setName(d.getName());
        }
        if (d.getCode() != null) {
            // Check if code is being changed and if new code already exists
            if (!existing.getCode().equals(d.getCode()) && repository.existsByCode(d.getCode())) {
                throw new IllegalArgumentException("Code already exists");
            }
            existing.setCode(d.getCode());
        }
        if (d.getDescription() != null) {
            existing.setDescription(d.getDescription());
        }

        return repository.save(existing);
    }

    // Step 6: DELETE /departments/{id} — protective delete; if any Employee references the department, return 409 with guidance.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        // 验证部门是否存在
        repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found"));

        // 检查是否有Employee引用该部门
        // 这里应该调用Employee service检查是否有员工属于该部门
        // 如果有，应该抛出异常返回409状态码
        // 暂时实现简单删除
        repository.deleteById(id);
    }

    // Step 7: GET /departments/by-code/{code} — lookup by business key.
    @GetMapping("/by-code/{code}")
    public Department byCode(@PathVariable String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Department not found"));
    }

    // Step 8: GET /departments/{id}/employees — composed list via Employee service (gateway-routed; may paginate).
    @GetMapping("/{id}/employees")
    public Map<String, Object> getEmployees(@PathVariable Long id) {
        // 验证部门是否存在
        Department department = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found"));

        // TODO: 这里应该调用Employee service获取该部门的员工列表
        // 可以通过API Gateway路由到Employee service: GET /api/v1/employees?departmentId={id}
        // 暂时返回部门信息和占位符
        return Map.of(
                "department", Map.of(
                        "id", department.getId(),
                        "name", department.getName(),
                        "code", department.getCode()
                ),
                "message", "Employee list should be fetched from Employee service via API Gateway",
                "suggestedEndpoint", "/api/v1/employees?departmentId=" + id
        );
    }
}
