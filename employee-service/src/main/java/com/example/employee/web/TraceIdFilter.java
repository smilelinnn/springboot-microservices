package com.example.employee.web;

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
// Filter 是 Java Servlet 规范定义的接口，用于在请求到达 Controller 之前和响应返回给客户端之后进行处理
public class TraceIdFilter implements Filter {

    // X-Trace-Id 不是 HTTP 标准头，是我们应用程序自定义的头，用于传递追踪ID，避免与标准头冲突，是业界约定俗成的做法
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_MDC_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Get trace ID from header or generate new one
        String traceId = httpRequest.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }

        // Add trace ID to response header
        httpResponse.setHeader(TRACE_ID_HEADER, traceId);

        // Add trace ID to MDC for logging
        // 将追踪ID放入日志上下文，所有后续的日志都会自动包含这个追踪ID，不需要在每个日志语句中手动添加
        MDC.put(TRACE_ID_MDC_KEY, traceId);

        try {
            chain.doFilter(request, response);
        } finally {
            // Clean up MDC
            MDC.remove(TRACE_ID_MDC_KEY);
        }
    }
    // 过滤器链的执行顺序：请求 → TraceIdFilter → 其他过滤器 → Controller → 其他过滤器 → TraceIdFilter → 响应
}