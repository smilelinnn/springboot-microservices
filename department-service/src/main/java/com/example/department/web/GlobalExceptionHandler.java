package com.example.department.web;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

// @RestControllerAdvice 是 Spring 的一个特殊注解
// 意思是"这个类会监听整个应用程序中的所有控制器"
// 当任何控制器发生异常时，Spring 会自动调用这个类中对应的方法
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 告诉 Spring："当发生 MethodArgumentNotValidException 异常时，调用这个方法"
    // MethodArgumentNotValidException 是当用户输入的数据不符合验证规则时抛出的异常
    // 处理流程：用户提交无效数据 -> Spring 验证失败，抛出异常 -> 异常处理器捕获异常 -> 提取错误信息，创建 ProblemDetail -> Spring 自动转换成 JSON 返回给前端
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        List<Map<String, String>> errors = new ArrayList<>();

        /*
        // 1. Spring 创建 BindingResult 对象
        BindingResult bindingResult = new BindingResult();

        // 2. Spring 验证每个字段，创建 FieldError 对象
        FieldError firstNameError = new FieldError("firstName", "", "firstName is required");
        FieldError lastNameError = new FieldError("lastName", "", "lastName is required");
        FieldError emailError = new FieldError("email", "invalid-email", "email must be valid");

        // 3. Spring 将所有 FieldError 添加到 BindingResult
        bindingResult.addError(firstNameError);
        bindingResult.addError(lastNameError);
        bindingResult.addError(emailError);

        // 4. Spring 抛出 MethodArgumentNotValidException
        throw new MethodArgumentNotValidException(parameter, bindingResult);
         */

        // FieldError 表示单个字段的验证错误
        // BindingResult 是 Spring 用来存储验证结果的对象，包含所有验证错误信息。
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            // 这里的 error.getField() 就是字段名，error.getDefaultMessage() 就是错误信息
            // 为每个错误创建一个 Map 对象，其中包含字段名和错误信息
            // errors 是一个列表，里面包含比如三个 map，每个 map 包含两个键值对，第一个键值对是字段名，第二个键值对是错误提示信息
            errors.add(Map.of(
                    "field", error.getField(),
                    "message", error.getDefaultMessage()
            ));
        }

        // ProblemDetail 是Spring 6 引入的标准错误响应格式, 类似于 HTTP 标准中的 Problem Details for HTTP APIs
        // 设置的各种属性：
        // status: HTTP 状态码（400 = 客户端错误）
        // title: 错误类型标题
        // detail: 详细错误描述
        // errors: 具体的字段错误列表
        // instance: 请求路径（如 "/api/v1/employees"）
        // timestamp: 错误发生的时间
        // traceId: 追踪ID（用于日志追踪）

        // 创建 ProblemDetail 对象
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problemDetail.setTitle("Validation Error");
        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("instance", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("traceId", MDC.get("traceId"));

        // ProblemDetail 是 Spring 的标准错误响应对象，会自动将这个对象转换成 JSON 返回给前端
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        List<Map<String, String>> errors = new ArrayList<>();

        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            errors.add(Map.of(
                    "field", fieldName,
                    "message", violation.getMessage()
            ));
        });

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problemDetail.setTitle("Validation Error");
        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("instance", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("traceId", MDC.get("traceId"));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(NoSuchElementException ex, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Resource not found");
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setProperty("instance", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("traceId", MDC.get("traceId"));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleEntityNotFound(jakarta.persistence.EntityNotFoundException ex, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setProperty("instance", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("traceId", MDC.get("traceId"));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleBusinessRuleViolation(IllegalArgumentException ex, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Business Rule Violation");
        problemDetail.setProperty("instance", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("traceId", MDC.get("traceId"));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setProperty("instance", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("traceId", MDC.get("traceId"));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    /* 最终返回给用户的 JSON：
    {
        "status": 400,
        "title": "Validation Error",
        "detail": "Validation failed",
        "errors": [
            {"field": "firstName", "message": "firstName is required"},
            {"field": "lastName", "message": "lastName is required"},
            {"field": "email", "message": "email must be valid"}
        ],
        "instance": "/api/v1/employees",
        "timestamp": "2025-01-16T18:30:00Z",
        "traceId": "abc123-def456"
    }
    */
}

