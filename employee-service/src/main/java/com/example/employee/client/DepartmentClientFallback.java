package com.example.employee.client;

import com.example.employee.dto.DepartmentDTO;
import org.springframework.stereotype.Component;

@Component
public class DepartmentClientFallback implements DepartmentClient {

    @Override
    public DepartmentDTO getDepartment(Long id) {
        // 降级处理
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(id);
        dto.setName("Department Service Unavailable");
        dto.setCode("SERVICE_DOWN");
        dto.setDescription("Department service is temporarily down, please try again later");
        return dto;
    }
}

// 当部门服务正常时:
// DepartmentDTO dept = departmentClient.getDepartment(1L);
// 实际发送: GET http://department-service:8082/api/v1/departments/1
// 返回: {"id": 1, "name": "Technology Department", "code": "TECH", "description": "Technology development department"}

// 当部门服务不可用时:
// DepartmentDTO dept = departmentClient.getDepartment(1L);
// 不会发送HTTP请求，直接调用 DepartmentClientFallback.getDepartment(1L)
// 返回: {"id": 1, "name": "Department Service Unavailable", "code": "SERVICE_DOWN", "description": "Department service is temporarily down, please try again later"}

