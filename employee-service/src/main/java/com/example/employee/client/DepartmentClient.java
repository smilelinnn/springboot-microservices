package com.example.employee.client;

import com.example.employee.dto.DepartmentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// DepartmentClient类的作用是：跨服务 HTTP 请求 （员工服务 -> HTTP请求 -> 部门服务）
// 因为员工信息中需要包含部门信息，但部门信息存储在另一个微服务（部门服务）中，所以需要通过HTTP调用获取。

// 部门服务返回部门信息
//   {
//           "id": 1,
//           "name": "技术部",
//           "code": "TECH",
//           "description": "技术开发部门"
//    }

// 员工服务返回员工信息时，包含部门信息
//   {
//           "id": 1,
//           "firstName": "张三",
//           "lastName": "李四",
//           "email": "zhangsan@company.com",
//           "departmentId": 1,
//           "department": {
//           "id": 1,
//           "name": "技术部",
//           "code": "TECH"
//           }
//    }

// @FeignClient: 声明这是一个HTTP客户端，用来调用其他微服务
// fallback 参数：当服务调用失败时，使用 DepartmentClientFallback 类来处理
@FeignClient(name = "DEPARTMENT-SERVICE",
        path = "/api/v1/departments",
        fallback = DepartmentClientFallback.class)
public interface DepartmentClient {

    @GetMapping("/{id}")
    DepartmentDTO getDepartment(@PathVariable("id") Long id);
}