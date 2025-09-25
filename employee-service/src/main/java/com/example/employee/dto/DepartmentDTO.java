package com.example.employee.dto;

import lombok.Data;

@Data
public class DepartmentDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
}

// DTO = Data Transfer Object (数据传输对象)
// 部门服务有自己的 Department 实体类。员工服务需要获取部门信息，但不能直接使用部门服务的实体类。
// 原因：
// 1. 服务独立性: 每个微服务应该有自己的数据结构
// 2. 版本控制: 部门服务更新实体类时，不会影响员工服务
// 3. 数据安全: 只暴露需要的数据字段
// 4. 网络传输: 优化JSON序列化/反序列化

// DTO的优势
// 1. 解耦: 服务间不直接依赖对方的实体类
// 2. 灵活性: 可以只传输需要的字段
// 3. 稳定性: 内部实体变更不影响外部接口
// 4. 性能: 减少网络传输的数据量