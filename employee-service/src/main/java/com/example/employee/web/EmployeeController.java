package com.example.employee.web;

import com.example.employee.dto.EmployeeDTO;
import com.example.employee.dto.EmployeeStatsDTO;
import com.example.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService service;

    // 1. GET /employees — pagination (page, size), sorting (sort=lastName,asc), filters (email, lastName contains, departmentId).
    @GetMapping
    public Page<EmployeeDTO> all(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) Long departmentId,
            Pageable pageable) {
        return service.getAll(email, lastName, departmentId, pageable);
    }

    // 2. GET /employees/{id} — employee detail, optionally enriched with department summary
    // GET /employees/1                          // includeDepartment = false（使用默认值）
    // GET /employees/1?includeDepartment=true   // includeDepartment = true
    // GET /employees/1?includeDepartment=false  // includeDepartment = false
    @GetMapping("/{id}")
    public EmployeeDTO byId(@PathVariable Long id,
                            @RequestParam(defaultValue = "false") boolean includeDepartment) {
        return service.getById(id, includeDepartment);
    }

    // 3. POST /employees — create; enforce unique email; optional Idempotency-Key request header (treat duplicate keys as safe replays).
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeDTO create(@Valid @RequestBody EmployeeDTO dto,
                              @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return service.create(dto, idempotencyKey);
    }

    // 4. PUT /employees/{id} — full update; reject changing to a duplicate email (409).
    @PutMapping("/{id}")
    public EmployeeDTO update(@PathVariable Long id, @Valid @RequestBody EmployeeDTO dto) {
        return service.update(id, dto);
    }

    // 5. PATCH /employees/{id} — partial update (e.g., only departmentId).
    @PatchMapping("/{id}")
    public EmployeeDTO partialUpdate(@PathVariable Long id, @RequestBody EmployeeDTO dto) {
        return service.partialUpdate(id, dto);
    }

    // 6. DELETE /employees/{id} — delete (204).
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // 7. GET /employees/search — convenience endpoint for case-insensitive name/email search.
    @GetMapping("/search")
    public Page<EmployeeDTO> search(@RequestParam String query, Pageable pageable) {
        return service.search(query, pageable);
    }

    // 8. GET /employees/stats — simple metrics (e.g., counts by departmentId).
    @GetMapping("/stats")
    public EmployeeStatsDTO getStats() {
        return service.getStats();
    }
}
