package com.example.employee.web;

import com.example.employee.dto.EmployeeDTO;
import com.example.employee.dto.EmployeeStatsDTO;
import com.example.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/employees")
@RequiredArgsConstructor
public class EmployeeControllerV2 {

    private final EmployeeService service;

    // 1. GET /api/v2/employees — pagination (page, size), sorting (sort=lastName,asc), filters (email, lastName contains, departmentId).
    @GetMapping
    public Page<EmployeeDTO> all(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) Long departmentId,
            Pageable pageable) {
        return service.getAll(email, lastName, departmentId, pageable);
    }

    // 2. GET /api/v2/employees/{id} — employee detail with Redis caching
    @GetMapping("/{id}")
    @Cacheable(value = "employees", key = "#id", unless = "#result == null")
    public EmployeeDTO byId(@PathVariable Long id,
                            @RequestParam(defaultValue = "false") boolean includeDepartment) {
        return service.getById(id, includeDepartment);
    }

    // 3. POST /api/v2/employees — create; enforce unique email; optional Idempotency-Key request header
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CacheEvict(value = {"employees", "employeeStats"}, allEntries = true)
    public EmployeeDTO create(@Valid @RequestBody EmployeeDTO dto,
                              @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return service.create(dto, idempotencyKey);
    }

    // 4. PUT /api/v2/employees/{id} — full update; reject changing to a duplicate email (409).
    @PutMapping("/{id}")
    @CacheEvict(value = {"employees", "employeeStats"}, allEntries = true)
    public EmployeeDTO update(@PathVariable Long id, @Valid @RequestBody EmployeeDTO dto) {
        return service.update(id, dto);
    }

    // 5. PATCH /api/v2/employees/{id} — partial update (e.g., only departmentId).
    @PatchMapping("/{id}")
    @CacheEvict(value = {"employees", "employeeStats"}, allEntries = true)
    public EmployeeDTO partialUpdate(@PathVariable Long id, @RequestBody EmployeeDTO dto) {
        return service.partialUpdate(id, dto);
    }

    // 6. DELETE /api/v2/employees/{id} — delete (204).
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CacheEvict(value = {"employees", "employeeStats"}, allEntries = true)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // 7. GET /api/v2/employees/search — convenience endpoint for case-insensitive name/email search.
    @GetMapping("/search")
    public Page<EmployeeDTO> search(@RequestParam String query, Pageable pageable) {
        return service.search(query, pageable);
    }

    // 8. GET /api/v2/employees/stats — simple metrics with Redis caching
    @GetMapping("/stats")
    @Cacheable(value = "employeeStats", key = "'all'", unless = "#result == null")
    public EmployeeStatsDTO getStats() {
        return service.getStats();
    }
}
