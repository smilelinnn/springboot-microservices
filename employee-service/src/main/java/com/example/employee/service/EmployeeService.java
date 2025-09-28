package com.example.employee.service;

import com.example.employee.client.DepartmentClient;
import com.example.employee.domain.Employee;
import com.example.employee.dto.DepartmentDTO;
import com.example.employee.dto.EmployeeDTO;
import com.example.employee.dto.EmployeeStatsDTO;
import com.example.employee.exception.DuplicateEmailException;
import com.example.employee.repo.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository repository;
    private final DepartmentClient departmentClient;
    private final KafkaProducerService kafkaProducerService;

    // 3. POST /employees — create; enforce unique email; optional Idempotency-Key request header (treat duplicate keys as safe replays).
    // 简单的内存缓存来存储幂等性键
    private final Map<String, EmployeeDTO> idempotencyCache = new ConcurrentHashMap<>();

    public List<EmployeeDTO> getAll() {
        return repository.findAll().stream()
                .map(e -> toDTO(e, false))
                .collect(Collectors.toList());
    }

    // 1. GET /employees — pagination (page, size), sorting (sort=lastName,asc), filters (email, lastName contains, departmentId).
    public Page<EmployeeDTO> getAll(String email, String lastName, Long departmentId, Pageable pageable) {
        Page<Employee> employees;

        if (email != null && lastName != null && departmentId != null) {
            // All filters
            employees = repository.findByEmailAndLastNameContainingIgnoreCaseAndDepartmentId(email, lastName, departmentId, pageable);
        } else if (email != null && lastName != null) {
            // Email + lastName
            employees = repository.findByEmailAndLastNameContainingIgnoreCase(email, lastName, pageable);
        } else if (email != null && departmentId != null) {
            // Email + departmentId
            employees = repository.findByEmailAndDepartmentId(email, departmentId, pageable);
        } else if (lastName != null && departmentId != null) {
            // LastName + departmentId
            employees = repository.findByLastNameContainingIgnoreCaseAndDepartmentId(lastName, departmentId, pageable);
        } else if (email != null) {
            // Email only
            employees = repository.findByEmail(email, pageable);
        } else if (lastName != null) {
            // LastName only
            employees = repository.findByLastNameContainingIgnoreCase(lastName, pageable);
        } else if (departmentId != null) {
            // DepartmentId only
            employees = repository.findByDepartmentId(departmentId, pageable);
        } else {
            // No filters
            employees = repository.findAll(pageable);
        }

        return employees.map(e -> toDTO(e, false));
    }

    // 2. GET /employees/{id} — employee detail, optionally enriched with department summary
    // GET /employees/1                          // includeDepartment = false（使用默认值）
    // GET /employees/1?includeDepartment=true   // includeDepartment = true
    // GET /employees/1?includeDepartment=false  // includeDepartment = false
    public EmployeeDTO getById(Long id, boolean includeDepartment) {
        Employee e = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        return toDTO(e, includeDepartment);
    }

    // 3. POST /employees — create; enforce unique email; optional Idempotency-Key request header (treat duplicate keys as safe replays).
    @Transactional
    public EmployeeDTO create(EmployeeDTO dto, String idempotencyKey) {
        // 如果有幂等性键，先检查缓存
        if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
            EmployeeDTO cached = idempotencyCache.get(idempotencyKey);
            if (cached != null) {
                return cached; // 返回缓存的结果
            }
        }

        // 检查邮箱唯一性
        if (repository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException("Email already exists");
        }

        // 创建新员工
        Employee e = Employee.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .departmentId(dto.getDepartmentId())
                .build();
        e = repository.save(e);
        EmployeeDTO result = toDTO(e, true);

        // 发布员工创建事件
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "EMPLOYEE_CREATED");
        eventData.put("employeeId", e.getId());
        eventData.put("email", e.getEmail());
        eventData.put("departmentId", e.getDepartmentId());
        eventData.put("firstName", e.getFirstName());
        eventData.put("lastName", e.getLastName());
        eventData.put("timestamp", System.currentTimeMillis());

        kafkaProducerService.sendEmployeeEvent("EMPLOYEE_CREATED", eventData);

        // 发送通知事件
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("eventType", "EMAIL");
        notificationData.put("recipient", e.getEmail());
        notificationData.put("message", "Welcome! Your employee ID is: " + e.getId());
        notificationData.put("timestamp", System.currentTimeMillis());

        kafkaProducerService.sendNotificationEvent("EMAIL", notificationData);

        // 如果有幂等性键，缓存结果
        if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
            idempotencyCache.put(idempotencyKey, result);
        }
        return result;
    }

    // 4. PUT /employees/{id} — full update; reject changing to a duplicate email (409).
    @Transactional
    public EmployeeDTO update(Long id, EmployeeDTO dto) {
        // 检查员工是否存在
        Employee existingEmployee = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        // 保存旧的部门ID用于事件
        Long oldDepartmentId = existingEmployee.getDepartmentId();

        // 检查邮箱是否重复（排除当前员工）
        if (!existingEmployee.getEmail().equals(dto.getEmail()) &&
                repository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException("Email already exists");
        }

        // 更新员工信息
        existingEmployee.setFirstName(dto.getFirstName());
        existingEmployee.setLastName(dto.getLastName());
        existingEmployee.setEmail(dto.getEmail());
        existingEmployee.setDepartmentId(dto.getDepartmentId());

        Employee updatedEmployee = repository.save(existingEmployee);

        // 发布员工更新事件
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "EMPLOYEE_UPDATED");
        eventData.put("employeeId", updatedEmployee.getId());
        eventData.put("email", updatedEmployee.getEmail());
        eventData.put("oldDepartmentId", oldDepartmentId);
        eventData.put("newDepartmentId", updatedEmployee.getDepartmentId());
        eventData.put("timestamp", System.currentTimeMillis());

        kafkaProducerService.sendEmployeeEvent("EMPLOYEE_UPDATED", eventData);

        // 如果部门变更，发送通知
        if (oldDepartmentId != null && !oldDepartmentId.equals(updatedEmployee.getDepartmentId())) {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("eventType", "SYSTEM");
            notificationData.put("recipient", "hr@company.com");
            notificationData.put("message", "员工 " + updatedEmployee.getEmail() + " 已从部门 " + oldDepartmentId + " 转移到部门 " + updatedEmployee.getDepartmentId());
            notificationData.put("timestamp", System.currentTimeMillis());

            kafkaProducerService.sendNotificationEvent("SYSTEM", notificationData);
        }

        return toDTO(updatedEmployee, true); // 更新后总是包含部门信息
    }

    // 5. PATCH /employees/{id} — partial update (e.g., only departmentId).
    @Transactional
    public EmployeeDTO partialUpdate(Long id, EmployeeDTO dto) {
        // 检查员工是否存在
        Employee existingEmployee = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        // 保存旧的部门ID用于事件
        Long oldDepartmentId = existingEmployee.getDepartmentId();

        // 部分更新：只更新非null字段
        if (dto.getFirstName() != null) {
            existingEmployee.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            existingEmployee.setLastName(dto.getLastName());
        }
        if (dto.getEmail() != null) {
            // 检查邮箱是否重复（排除当前员工）
            if (!existingEmployee.getEmail().equals(dto.getEmail()) &&
                    repository.existsByEmail(dto.getEmail())) {
                throw new DuplicateEmailException("Email already exists");
            }
            existingEmployee.setEmail(dto.getEmail());
        }
        if (dto.getDepartmentId() != null) {
            existingEmployee.setDepartmentId(dto.getDepartmentId());
        }

        Employee updatedEmployee = repository.save(existingEmployee);

        // 发布员工更新事件
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "EMPLOYEE_UPDATED");
        eventData.put("employeeId", updatedEmployee.getId());
        eventData.put("email", updatedEmployee.getEmail());
        eventData.put("oldDepartmentId", oldDepartmentId);
        eventData.put("newDepartmentId", updatedEmployee.getDepartmentId());
        eventData.put("timestamp", System.currentTimeMillis());

        kafkaProducerService.sendEmployeeEvent("EMPLOYEE_UPDATED", eventData);

        // 如果部门变更，发送通知
        if (oldDepartmentId != null && !oldDepartmentId.equals(updatedEmployee.getDepartmentId())) {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("eventType", "SYSTEM");
            notificationData.put("recipient", "hr@company.com");
            notificationData.put("message", "员工 " + updatedEmployee.getEmail() + " 已从部门 " + oldDepartmentId + " 转移到部门 " + updatedEmployee.getDepartmentId());
            notificationData.put("timestamp", System.currentTimeMillis());

            kafkaProducerService.sendNotificationEvent("SYSTEM", notificationData);
        }

        return toDTO(updatedEmployee, true); // 部分更新后总是包含部门信息
    }

    // 6. DELETE /employees/{id} — delete (204).
    @Transactional
    public void delete(Long id) {
        // 检查员工是否存在并获取员工信息
        Employee employee = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        // 发布员工删除事件
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "EMPLOYEE_DELETED");
        eventData.put("employeeId", employee.getId());
        eventData.put("email", employee.getEmail());
        eventData.put("departmentId", employee.getDepartmentId());
        eventData.put("firstName", employee.getFirstName());
        eventData.put("lastName", employee.getLastName());
        eventData.put("timestamp", System.currentTimeMillis());

        kafkaProducerService.sendEmployeeEvent("EMPLOYEE_DELETED", eventData);

        // 发送通知事件
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("eventType", "SYSTEM");
        notificationData.put("recipient", "hr@company.com");
        notificationData.put("message", "员工 " + employee.getEmail() + " has resigned. Please proceed with the relevant formalities.");
        notificationData.put("timestamp", System.currentTimeMillis());

        kafkaProducerService.sendNotificationEvent("SYSTEM", notificationData);

        // 删除员工
        repository.deleteById(id);
    }

    // 7. GET /employees/search — convenience endpoint for case-insensitive name/email search
    public Page<EmployeeDTO> search(String query, Pageable pageable) {
        Page<Employee> employees = repository.searchByNameOrEmail(query, pageable);
        return employees.map(e -> toDTO(e, false));
    }

    // 8. GET /employees/stats — simple metrics (e.g., counts by departmentId).
    public EmployeeStatsDTO getStats() {
        // 获取总员工数
        long totalEmployees = repository.count();

        // 获取按部门分组的员工数
        List<Object[]> departmentCounts = repository.countEmployeesByDepartment();
        Map<Long, Long> employeesByDepartment = new HashMap<>();
        for (Object[] result : departmentCounts) {
            Long departmentId = (Long) result[0];
            Long count = (Long) result[1];
            employeesByDepartment.put(departmentId, count);
        }

        // 获取没有部门的员工数
        long employeesWithoutDepartment = repository.countEmployeesWithoutDepartment();

        return EmployeeStatsDTO.builder()
                .totalEmployees(totalEmployees)
                .employeesByDepartment(employeesByDepartment)
                .employeesWithoutDepartment(employeesWithoutDepartment)
                .build();
    }

    // 都需要应用 toDTO 方法
    private EmployeeDTO toDTO(Employee e, boolean includeDepartment) {
        DepartmentDTO dept = null;
        if (includeDepartment && e.getDepartmentId() != null) {
            try {
                dept = departmentClient.getDepartment(e.getDepartmentId());
            } catch (Exception ignored) { }
        }
        return EmployeeDTO.builder()
                .id(e.getId())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .email(e.getEmail())
                .departmentId(e.getDepartmentId())
                .department(dept)
                .build();
    }
}
