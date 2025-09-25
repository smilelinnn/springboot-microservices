package com.example.department.exception;

/**
 * 部门未找到异常
 * 当部门不存在时抛出
 */
public class DepartmentNotFoundException extends RuntimeException {

    public DepartmentNotFoundException(String message) {
        super(message);
    }
}
