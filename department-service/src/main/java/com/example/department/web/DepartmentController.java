package com.example.department.web;

import com.example.department.domain.Department;

import com.example.department.exception.DepartmentNotFoundException;
import com.example.department.exception.DuplicateCodeException;
import com.example.department.repo.DepartmentRepository;
import com.example.department.service.KafkaProducerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository repository;
    private final KafkaProducerService kafkaProducerService;

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
        return repository.findById(id).orElseThrow(() -> new DepartmentNotFoundException("Department with id " + id + " not found"));
    }

    // Step 3: POST /departments — create with unique code (short string identifier).
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Department create(@Valid @RequestBody Department d) {
        // 检查code是否已存在，如果存在则抛出异常
        repository.findByCode(d.getCode())
                .ifPresent(existing -> {
                    throw new DuplicateCodeException("Department code '" + d.getCode() + "' already exists");
                });

        Department savedDepartment = repository.save(d);

        // 发布部门创建事件
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "DEPARTMENT_CREATED");
        eventData.put("departmentId", savedDepartment.getId());
        eventData.put("name", savedDepartment.getName());
        eventData.put("code", savedDepartment.getCode());
        eventData.put("description", savedDepartment.getDescription());
        eventData.put("timestamp", System.currentTimeMillis());

        kafkaProducerService.sendDepartmentEvent("DEPARTMENT_CREATED", eventData);

        // 发送通知事件
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("eventType", "SYSTEM");
        notificationData.put("recipient", "admin@company.com");
        notificationData.put("message", "新部门已创建: " + savedDepartment.getName() + " (" + savedDepartment.getCode() + ")");
        notificationData.put("timestamp", System.currentTimeMillis());

        kafkaProducerService.sendNotificationEvent("SYSTEM", notificationData);

        return savedDepartment;
    }

    // Step 4: PUT /departments/{id} — full update; code remains unique.
    @PutMapping("/{id}")
    public Department update(@PathVariable Long id, @Valid @RequestBody Department d) {
        Department existing = repository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException("Department with id " + id + " not found"));

        // Check if code is being changed and if new code already exists
        if (!existing.getCode().equals(d.getCode()) && repository.existsByCode(d.getCode())) {
            throw new DuplicateCodeException("Department code '" + d.getCode() + "' already exists");
        }

        existing.setName(d.getName());
        existing.setCode(d.getCode());
        existing.setDescription(d.getDescription());

        Department updatedDepartment = repository.save(existing);

        // 发布部门更新事件
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "DEPARTMENT_UPDATED");
        eventData.put("departmentId", updatedDepartment.getId());
        eventData.put("name", updatedDepartment.getName());
        eventData.put("code", updatedDepartment.getCode());
        eventData.put("description", updatedDepartment.getDescription());
        eventData.put("timestamp", System.currentTimeMillis());

        kafkaProducerService.sendDepartmentEvent("DEPARTMENT_UPDATED", eventData);

        // 发送通知事件
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("eventType", "SYSTEM");
        notificationData.put("recipient", "admin@company.com");
        notificationData.put("message", "部门信息已更新: " + updatedDepartment.getName() + " (" + updatedDepartment.getCode() + ")");
        notificationData.put("timestamp", System.currentTimeMillis());

        kafkaProducerService.sendNotificationEvent("SYSTEM", notificationData);

        return updatedDepartment;
    }

    // Step 5: PATCH /departments/{id} — partial update (e.g., managerEmail).
    @PatchMapping("/{id}")
    public Department partialUpdate(@PathVariable Long id, @Valid @RequestBody Department d) {
        Department existing = repository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException("Department with id " + id + " not found"));

        if (d.getName() != null) {
            existing.setName(d.getName());
        }
        if (d.getCode() != null) {
            // Check if code is being changed and if new code already exists
            if (!existing.getCode().equals(d.getCode()) && repository.existsByCode(d.getCode())) {
                throw new DuplicateCodeException("Department code '" + d.getCode() + "' already exists");
            }
            existing.setCode(d.getCode());
        }
        if (d.getDescription() != null) {
            existing.setDescription(d.getDescription());
        }

        Department updatedDepartment = repository.save(existing);

        // 发布部门更新事件
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "DEPARTMENT_UPDATED");
        eventData.put("departmentId", updatedDepartment.getId());
        eventData.put("name", updatedDepartment.getName());
        eventData.put("code", updatedDepartment.getCode());
        eventData.put("description", updatedDepartment.getDescription());
        eventData.put("timestamp", System.currentTimeMillis());

        kafkaProducerService.sendDepartmentEvent("DEPARTMENT_UPDATED", eventData);

        // 发送通知事件
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("eventType", "SYSTEM");
        notificationData.put("recipient", "admin@company.com");
        notificationData.put("message", "部门信息已更新: " + updatedDepartment.getName() + " (" + updatedDepartment.getCode() + ")");
        notificationData.put("timestamp", System.currentTimeMillis());

        kafkaProducerService.sendNotificationEvent("SYSTEM", notificationData);

        return updatedDepartment;
    }

    // Step 6: DELETE /departments/{id} — protective delete; if any Employee references the department, return 409 with guidance.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        // 验证部门是否存在并获取部门信息
        Department department = repository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException("Department with id " + id + " not found"));

        // TODO: 检查是否有Employee引用该部门
        // 这里应该调用Employee service检查是否有员工属于该部门
        // 如果有，应该抛出异常返回409状态码
        // 暂时实现简单删除

        // 发布部门删除事件
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "DEPARTMENT_DELETED");
        eventData.put("departmentId", department.getId());
        eventData.put("name", department.getName());
        eventData.put("code", department.getCode());
        eventData.put("timestamp", System.currentTimeMillis());

        kafkaProducerService.sendDepartmentEvent("DEPARTMENT_DELETED", eventData);

        // 发送通知事件
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("eventType", "SYSTEM");
        notificationData.put("recipient", "admin@company.com");
        notificationData.put("message", "部门已删除: " + department.getName() + " (" + department.getCode() + ")，请处理相关员工");
        notificationData.put("timestamp", System.currentTimeMillis());

        kafkaProducerService.sendNotificationEvent("SYSTEM", notificationData);

        repository.deleteById(id);
    }

    // Step 7: GET /departments/by-code/{code} — lookup by business key.
    @GetMapping("/by-code/{code}")
    public Department byCode(@PathVariable String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new DepartmentNotFoundException("Department with code '" + code + "' not found"));
    }

    // Step 8: GET /departments/{id}/employees — composed list via Employee service (gateway-routed; may paginate).
    @GetMapping("/{id}/employees")
    public Map<String, Object> getEmployees(@PathVariable Long id) {
        // 验证部门是否存在
        Department department = repository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException("Department with id " + id + " not found"));

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
