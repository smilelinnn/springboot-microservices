package com.example.department.web;

import com.example.department.domain.Department;
import com.example.department.repo.DepartmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)
@DisplayName("DepartmentController (WebMvc slice)")
public class DepartmentControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean
    DepartmentRepository repository;

    @Nested
    class Create_validation {
        @Test
        void create_valid_returns_201() throws Exception {
            var req = Department.builder()
                    .name("Marketing")
                    .code("MKT")  // ✅ 添加有效的code
                    .description("Sales dept")
                    .build();
            var res = Department.builder()
                    .id(1L)
                    .name("Marketing")
                    .code("MKT")
                    .description("Sales dept")
                    .build();

            when(repository.existsByCode("MKT")).thenReturn(false);
            when(repository.save(any(Department.class))).thenReturn(res);

            mvc.perform(post("/api/v1/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Marketing"))
                    .andExpect(jsonPath("$.code").value("MKT"));
        }

        @Test
        void create_duplicate_code_returns_409() throws Exception {
            var req = Department.builder()
                    .name("Marketing")
                    .code("MKT")
                    .description("Sales dept")
                    .build();

            when(repository.existsByCode("MKT")).thenReturn(true);

            mvc.perform(post("/api/v1/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isConflict());
        }

        @Test
        void create_missing_name_returns_400() throws Exception {
            var req = Department.builder()
                    .code("MKT")
                    .description("Sales dept")
                    .build();

            mvc.perform(post("/api/v1/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_missing_code_returns_400() throws Exception {
            var req = Department.builder()
                    .name("Marketing")
                    .description("Sales dept")
                    .build();

            mvc.perform(post("/api/v1/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class Get_validation {
        @Test
        void get_by_id_returns_200() throws Exception {
            var dept = Department.builder()
                    .id(1L)
                    .name("Marketing")
                    .code("MKT")
                    .description("Sales dept")
                    .build();

            when(repository.findById(1L)).thenReturn(Optional.of(dept));

            mvc.perform(get("/api/v1/departments/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Marketing"))
                    .andExpect(jsonPath("$.code").value("MKT"));
        }

        @Test
        void get_by_id_not_found_returns_404() throws Exception {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            mvc.perform(get("/api/v1/departments/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void get_by_code_returns_200() throws Exception {
            var dept = Department.builder()
                    .id(1L)
                    .name("Marketing")
                    .code("MKT")
                    .description("Sales dept")
                    .build();

            when(repository.findByCode("MKT")).thenReturn(Optional.of(dept));

            mvc.perform(get("/api/v1/departments/by-code/MKT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("MKT"));
        }

        @Test
        void get_by_code_not_found_returns_404() throws Exception {
            when(repository.findByCode("INVALID")).thenReturn(Optional.empty());

            mvc.perform(get("/api/v1/departments/by-code/INVALID"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class List_validation {
        @Test
        void list_returns_200() throws Exception {
            // 创建真实的Department对象列表
            List<Department> departments = List.of(
                    Department.builder().id(1L).name("Marketing").code("MKT").build()
            );

            // 创建真实的Page对象
            Page<Department> page = new org.springframework.data.domain.PageImpl<>(departments);
            when(repository.findAll(any(Pageable.class))).thenReturn(page);

            mvc.perform(get("/api/v1/departments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name").value("Marketing"));
        }

        @Test
        void list_with_name_filter_returns_200() throws Exception {
            // 创建真实的Department对象列表
            List<Department> departments = List.of(
                    Department.builder().id(1L).name("Marketing").code("MKT").build()
            );

            // 创建真实的Page对象
            Page<Department> page = new org.springframework.data.domain.PageImpl<>(departments);
            when(repository.findByNameContainingIgnoreCase(eq("Marketing"), any(Pageable.class))).thenReturn(page);

            mvc.perform(get("/api/v1/departments")
                            .param("name", "Marketing"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name").value("Marketing"));
        }
    }

    @Nested
    class Update_validation {
        @Test
        void update_valid_returns_200() throws Exception {
            var existing = Department.builder()
                    .id(1L)
                    .name("Old Marketing")
                    .code("OLD")
                    .description("Old dept")
                    .build();
            var req = Department.builder()
                    .name("New Marketing")
                    .code("NEW")
                    .description("New dept")
                    .build();
            var updated = Department.builder()
                    .id(1L)
                    .name("New Marketing")
                    .code("NEW")
                    .description("New dept")
                    .build();

            when(repository.findById(1L)).thenReturn(Optional.of(existing));
            when(repository.existsByCode("NEW")).thenReturn(false);
            when(repository.save(any(Department.class))).thenReturn(updated);

            mvc.perform(put("/api/v1/departments/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("New Marketing"))
                    .andExpect(jsonPath("$.code").value("NEW"));
        }

        @Test
        void update_duplicate_code_returns_409() throws Exception {
            var existing = Department.builder()
                    .id(1L)
                    .name("Marketing")
                    .code("MKT")
                    .build();
            var req = Department.builder()
                    .name("Marketing")
                    .code("DUPLICATE")
                    .description("Dept")
                    .build();

            when(repository.findById(1L)).thenReturn(Optional.of(existing));
            when(repository.existsByCode("DUPLICATE")).thenReturn(true);

            mvc.perform(put("/api/v1/departments/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isConflict());
        }

        @Test
        void update_not_found_returns_404() throws Exception {
            var req = Department.builder()
                    .name("Marketing")
                    .code("MKT")
                    .description("Dept")
                    .build();

            when(repository.findById(999L)).thenReturn(Optional.empty());

            mvc.perform(put("/api/v1/departments/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Delete_validation {
        @Test
        void delete_existing_returns_204() throws Exception {
            var dept = Department.builder()
                    .id(1L)
                    .name("Marketing")
                    .code("MKT")
                    .build();

            when(repository.findById(1L)).thenReturn(Optional.of(dept));
            doNothing().when(repository).deleteById(1L);

            mvc.perform(delete("/api/v1/departments/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void delete_not_found_returns_404() throws Exception {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            mvc.perform(delete("/api/v1/departments/999"))
                    .andExpect(status().isNotFound());
        }
    }
}
