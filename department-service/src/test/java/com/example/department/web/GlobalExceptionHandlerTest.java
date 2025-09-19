package com.example.department.web;

import com.example.department.domain.Department;
import com.example.department.repo.DepartmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)
@DisplayName("Global Exception Handler Tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DepartmentRepository departmentRepository;

    @Test
    @DisplayName("POST /api/v1/departments with invalid data should return 400 with validation errors")
    void createDepartment_withInvalidData_shouldReturn400WithValidationErrors() throws Exception {
        // Given: Invalid department data (missing required fields)
        Department invalidDepartment = Department.builder()
                .name("") // Empty name
                .description("Valid description")
                .build();

        // When & Then: Should return 400 with validation errors
        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDepartment)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").exists())
                .andExpect(jsonPath("$.errors[0].message").exists())
                .andExpect(jsonPath("$.instance").value("/api/v1/departments"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(header().string("X-Trace-Id", org.hamcrest.Matchers.notNullValue()));

        // Verify repository is not called due to validation failure
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    @DisplayName("GET /api/v1/departments/{id} with non-existent ID should return 404")
    void getDepartmentById_withNonExistentId_shouldReturn404() throws Exception {
        // Given: Non-existent department ID
        Long nonExistentId = 999L;
        when(departmentRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // When & Then: Should return 404
        mockMvc.perform(get("/api/v1/departments/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.detail").value("Resource not found"))
                .andExpect(jsonPath("$.instance").value("/api/v1/departments/999"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(header().string("X-Trace-Id", org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/departments with duplicate name should return 409")
    void createDepartment_withDuplicateName_shouldReturn409() throws Exception {
        // Given: Valid department data with duplicate name
        Department departmentWithDuplicateName = Department.builder()
                .name("Engineering") // This name already exists
                .description("Valid description")
                .build();

        when(departmentRepository.save(any(Department.class)))
                .thenThrow(new IllegalArgumentException("Department name already exists"));

        // When & Then: Should return 409
        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(departmentWithDuplicateName)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Business Rule Violation"))
                .andExpect(jsonPath("$.detail").value("Department name already exists"))
                .andExpect(jsonPath("$.instance").value("/api/v1/departments"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(header().string("X-Trace-Id", org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    @DisplayName("Any unexpected exception should return 500 without stack trace")
    void handleUnexpectedException_shouldReturn500WithoutStackTrace() throws Exception {
        // Given: Repository throws unexpected exception
        when(departmentRepository.findAll())
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then: Should return 500 without stack trace
        mockMvc.perform(get("/api/v1/departments"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.instance").value("/api/v1/departments"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(header().string("X-Trace-Id", org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    @DisplayName("Validation errors should short-circuit repository calls")
    void validationErrors_shouldShortCircuitRepositoryCalls() throws Exception {
        // Given: Invalid department data
        Department invalidDepartment = Department.builder()
                .name("") // Empty name
                .description("Valid description")
                .build();

        // When: Making request with invalid data
        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDepartment)))
                .andExpect(status().isBadRequest());

        // Then: Repository should not be called
        verify(departmentRepository, never()).save(any(Department.class));
    }
}