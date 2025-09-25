package com.example.department.exception;

/**
 * 部门代码重复异常
 * 当部门代码已存在时抛出
 */
public class DuplicateCodeException extends RuntimeException {

    public DuplicateCodeException(String message) {
        super(message);
    }
}
