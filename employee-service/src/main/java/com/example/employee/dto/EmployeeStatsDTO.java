package com.example.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeStatsDTO {
    private long totalEmployees;
    private Map<Long, Long> employeesByDepartment;
    private long employeesWithoutDepartment;
}

