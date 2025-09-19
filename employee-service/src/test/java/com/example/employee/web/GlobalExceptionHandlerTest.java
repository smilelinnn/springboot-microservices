package com.example.employee.web;

import com.example.employee.dto.EmployeeDTO;
import com.example.employee.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@DisplayName("Global Exception Handler Tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @Test
    @DisplayName("POST /api/v1/employees with invalid data should return 400 with validation errors")
    void createEmployee_withInvalidData_shouldReturn400WithValidationErrors() throws Exception {
        // Given: Invalid employee data (missing required fields)
        EmployeeDTO invalidEmployee = EmployeeDTO.builder()
                .firstName("") // Empty first name
                .lastName("")  // Empty last name
                .email("invalid-email") // Invalid email format
                .build();

        // When & Then: Should return 400 with validation errors
        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").exists())
                .andExpect(jsonPath("$.errors[0].message").exists())
                .andExpect(jsonPath("$.instance").value("/api/v1/employees"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(header().string("X-Trace-Id", org.hamcrest.Matchers.notNullValue()));

        // Verify service is not called due to validation failure
        verify(employeeService, never()).create(any(EmployeeDTO.class));
    }

    @Test
    @DisplayName("GET /api/v1/employees/{id} with non-existent ID should return 404")
    void getEmployeeById_withNonExistentId_shouldReturn404() throws Exception {
        // Given: Non-existent employee ID
        Long nonExistentId = 999L;
        when(employeeService.getById(nonExistentId))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Employee not found"));

        // When & Then: Should return 404
        mockMvc.perform(get("/api/v1/employees/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.detail").value("Employee not found"))
                .andExpect(jsonPath("$.instance").value("/api/v1/employees/999"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(header().string("X-Trace-Id", org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/employees with duplicate email should return 409")
    void createEmployee_withDuplicateEmail_shouldReturn409() throws Exception {
        // Given: Valid employee data with duplicate email
        EmployeeDTO employeeWithDuplicateEmail = EmployeeDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("alice@example.com") // This email already exists
                .departmentId(1L)
                .build();

        when(employeeService.create(any(EmployeeDTO.class)))
                .thenThrow(new IllegalArgumentException("Email already exists"));

        // When & Then: Should return 409
        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeWithDuplicateEmail)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Business Rule Violation"))
                .andExpect(jsonPath("$.detail").value("Email already exists"))
                .andExpect(jsonPath("$.instance").value("/api/v1/employees"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(header().string("X-Trace-Id", org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    @DisplayName("Any unexpected exception should return 500 without stack trace")
    void handleUnexpectedException_shouldReturn500WithoutStackTrace() throws Exception {
        // Given: Service throws unexpected exception
        when(employeeService.getAll())
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then: Should return 500 without stack trace
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.instance").value("/api/v1/employees"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(header().string("X-Trace-Id", org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    @DisplayName("Validation errors should short-circuit service calls")
    void validationErrors_shouldShortCircuitServiceCalls() throws Exception {
        // Given: Invalid employee data
        EmployeeDTO invalidEmployee = EmployeeDTO.builder()
                .firstName("") // Empty first name
                .lastName("")  // Empty last name
                .email("invalid-email") // Invalid email format
                .build();

        // When: Making request with invalid data
        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(status().isBadRequest());

        // Then: Service should not be called
        verify(employeeService, never()).create(any(EmployeeDTO.class));
    }
}
