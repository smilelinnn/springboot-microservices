package com.example.department.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class TraceIdFilter implements Filter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_MDC_KEY = "traceId";

    @Override
    // ServletRequest request：HTTP 请求对象
    // ServletResponse response：HTTP 响应对象
    // FilterChain chain：过滤器链，用于调用下一个过滤器
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 类型转换，因为 ServletRequest 是接触接口，只有基础方法
        // HttpServletRequest 是扩展接口，有 HTTP 特有的额外的方法：
        // String getHeader(String name);           // 获取请求头
        // String getMethod();                      // 获取请求方法
        // String getRequestURI();                  // 获取请求路径
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 尝试从请求头获取 TraceId，如果没有 TraceId，生成新的
        // UUID = Universally Unique Identifier（通用唯一标识符），生成一个全球唯一的字符串，格式：550e8400-e29b-41d4-a716-446655440000
        String traceId = httpRequest.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }

        // 设置响应头（返回给客户端）
        httpResponse.setHeader(TRACE_ID_HEADER, traceId);

        // MDC = Mapped Diagnostic Context（映射诊断上下文）
        // 是日志框架（如 Logback）提供的功能
        // 可以在日志中自动包含上下文信息
        // Add trace ID to MDC for logging
        // 设置 MDC（用于日志）
        MDC.put(TRACE_ID_MDC_KEY, traceId);

        try {
            chain.doFilter(request, response);
        } finally {
            // Clean up MDC
            MDC.remove(TRACE_ID_MDC_KEY);
        }
    }
}
