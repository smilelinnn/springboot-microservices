package com.example.employee.web;
import com.example.employee.dto.EmployeeDTO;
import com.example.employee.dto.EmployeeStatsDTO;
import com.example.employee.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SpringBootTest annotation
 *  it is used to test the entire spring boot application
 *  which means spring boot need to load all beans you defined in your application
 *  from application context.
 *
 *  key feature of spring boot test annotation
 *  1: full application context: it loads the full spring boot application context,
 *      including configuration, repo, service, and all beans
 *  2: integration testing, it is ideal for integration tests where you want to
 *  test the interaction between components(controller, service, db connection, repo. etc....)
 *
 *  webmvctest annotation
 *   it is used and designed for testing spring mvc controllers
 *   it focuses only one the web layer
 *
 *
 *   what is @Retention()? it defines how long
 *    your annotation should be kept in the java lifecycle
 *      RetentionPolicy.RUNTIME -> your annotation can be found at runtime
 *      RetentionPolicy.Source -> your annotation only can be found at compile time
 *
 *   @BeforeEach // this comes from junit 5
 *     public void setup(){
 *         System.out.println("this is before each annotation ");
 *     }
 *     @BeforeAll
 *     @AfterAll
 *
 * this is key features from junit 5
 *
 *  @ParameterizedTest(name = "create({0}) → duplicate? {1}")
 *  @CsvSource({
 *             "dina@example.com, false",
 *             "alice@example.com, true"
 *     })
 *
 *    why use it?
 *    avoiding code duplication: if you need to run the same test function with multiple inputs
 *    this annotation allows you to do that without writing separate test methods for each input!!
 *
 *    csvsource() -> csv formatted data with multiple parames
 *    valueSource() -> provide single type of data (string, integer) to the test method
 *    enumsource()
 *    methodsource()
 *    ...
 *
 * @Nested annotation
 * this annotation to define nested test classes inside a test class.
 * this annotation helps you organize and group all same test cases.
 *
 *
 *
 * grey box testing:
 * tester like you has partial knowledge of the internal code.
 *
 * TDD -> test driven development
 * writes tests code first, then code to pass them
 * like red(fail) -> green( pass) -> refactor
 *
 * you are first time to build your project:
 *
 * stress testing
 * test system under extreme load beyond normal limits
 *
 * Spike testing
 * test your system where load is suddenly increased/ decreased.
 *
 *
 * Smoke testing
 *  it is used for testing your system and ensures critical functionally works
 *
 *
 *  summary of steps from testing to production
 *  1: plan and define requirements
 *  2: write code (feature development)
 *  3: write unit test cases(test individual components)
 *  4: write integration test( test interaction with other service)
 *  5: run tests locally
 *  6: pass all test cases and put code to git repo
 *  7: ci  -> continuous integration (run tests in ci pipeline after pushing code)
 *  8: code review (peer review for code quality)
 *  9: deploy to staging(QA)
 *  10: performance and security testing (optional but recommended)
 *  11: deploy to production
 *  12: monitoring all the time....
 */

@WebMvcTest(EmployeeController.class)
@DisplayName("EmployeeController (WebMvc slice)")
public class EmployeeControllerTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @MockBean
    EmployeeService service;

    // READ 测试
    @Nested
    class List_and_Get {
        // 负例：HTTP 404，找不到员工数据
        @Test
        void get_by_id_not_found_returns_404() throws Exception {
            when(service.getById(999L, false))
                    .thenThrow(new jakarta.persistence.EntityNotFoundException("Employee not found"));

            mvc.perform(get("/api/v1/employees/999"))
                    .andExpect(status().isNotFound());
        }
    }

    // CREATE 测试
    @Nested
    class Create_validation {
        // 负例：HTTP 400，缺少邮箱字段
        @Test
        void create_missing_email_returns_400() throws Exception {
            var body = EmployeeDTO.builder().firstName("No").lastName("Email").build();
            mvc.perform(post("/api/v1/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(body)))
                    .andExpect(status().isBadRequest());
        }

        // 负例：HTTP 400，邮箱格式不正确
        @Test
        void create_invalid_email_format_returns_400() throws Exception {
            var req = EmployeeDTO.builder().firstName("Test").lastName("User").email("invalid-email").build();

            mvc.perform(post("/api/v1/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        // 负例：HTTP 400，缺少姓名字段
        @Test
        void create_missing_firstName_returns_400() throws Exception {
            var req = EmployeeDTO.builder().lastName("User").email("test@example.com").build();

            mvc.perform(post("/api/v1/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        // 负例：HTTP 400，缺少姓名字段
        @Test
        void create_missing_lastName_returns_400() throws Exception {
            var req = EmployeeDTO.builder().firstName("Test").email("test@example.com").build();

            mvc.perform(post("/api/v1/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    // UPDATE 测试
    @Nested
    class Update_validation {
        // 负例：HTTP 409，邮箱地址重复导致冲突
        @Test
        void update_duplicate_email_returns_409() throws Exception {
            var req = EmployeeDTO.builder().firstName("Test").lastName("User").email("duplicate@example.com").build();
            when(service.update(eq(1L), any(EmployeeDTO.class)))
                    .thenThrow(new com.example.employee.exception.DuplicateEmailException("Email already exists"));

            mvc.perform(put("/api/v1/employees/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isConflict());
        }

        // 负例：HTTP 400，邮箱格式不正确
        @Test
        void update_invalid_email_format_returns_400() throws Exception {
            var req = EmployeeDTO.builder().firstName("Test").lastName("User").email("invalid-email").build();

            mvc.perform(put("/api/v1/employees/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    // PATCH 测试
    @Nested
    class Partial_Update_validation {
        // 正例：HTTP 200，只更新部门信息，更新成功
        @Test
        void partial_update_department_only_returns_200() throws Exception {
            var req = EmployeeDTO.builder().departmentId(2L).build(); // 只更新部门ID
            var res = EmployeeDTO.builder().id(1L).firstName("John").lastName("Doe").email("john@example.com").departmentId(2L).build();
            when(service.partialUpdate(eq(1L), any(EmployeeDTO.class))).thenReturn(res);

            mvc.perform(patch("/api/v1/employees/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.departmentId").value(2L));
        }
    }

    // DELETE 测试
    @Nested
    class Delete_validation {
        // 正例：HTTP 204，删除成功
        @Test
        void delete_existing_employee_returns_204() throws Exception {
            doNothing().when(service).delete(1L);

            mvc.perform(delete("/api/v1/employees/1"))
                    .andExpect(status().isNoContent());
        }

        // 负例：HTTP 404，删除失败
        @Test
        void delete_nonexistent_employee_returns_404() throws Exception {
            doThrow(new jakarta.persistence.EntityNotFoundException("Employee not found"))
                    .when(service).delete(999L);

            mvc.perform(delete("/api/v1/employees/999"))
                    .andExpect(status().isNotFound());
        }
    }

    // SEARCH 测试
    @Nested
    class Search_validation {
        @Test
        void search_by_name_returns_200() throws Exception {
            // 创建真实的Employee对象列表
            List<EmployeeDTO> employees = List.of(
                    EmployeeDTO.builder().id(1L).firstName("John").lastName("Doe").email("john@example.com").build()
            );

            // 创建真实的Page对象
            Page<EmployeeDTO> page = new org.springframework.data.domain.PageImpl<>(employees);
            when(service.search(eq("john"), any(Pageable.class))).thenReturn(page);

            mvc.perform(get("/api/v1/employees/search")
                            .param("query", "john")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].firstName").value("John"));
        }

        @Test
        void search_by_email_returns_200() throws Exception {
            // 创建真实的Employee对象列表
            List<EmployeeDTO> employees = List.of(
                    EmployeeDTO.builder().id(1L).firstName("John").lastName("Doe").email("john@example.com").build()
            );

            // 创建真实的Page对象
            Page<EmployeeDTO> page = new org.springframework.data.domain.PageImpl<>(employees);
            when(service.search(eq("example.com"), any(Pageable.class))).thenReturn(page);

            mvc.perform(get("/api/v1/employees/search")
                            .param("query", "example.com")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].email").value("john@example.com"));
        }

        @Test
        void search_case_insensitive_returns_200() throws Exception {
            // 创建真实的Employee对象列表
            List<EmployeeDTO> employees = List.of(
                    EmployeeDTO.builder().id(1L).firstName("John").lastName("Doe").email("john@example.com").build()
            );

            // 创建真实的Page对象
            Page<EmployeeDTO> page = new org.springframework.data.domain.PageImpl<>(employees);
            when(service.search(eq("JOHN"), any(Pageable.class))).thenReturn(page);

            mvc.perform(get("/api/v1/employees/search")
                            .param("query", "JOHN")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].firstName").value("John"));
        }
    }

    // STATS 测试
    @Nested
    class Stats_validation {
        // 正例：统计员工数据，HTTP 200，统计成功
        @Test
        void get_stats_returns_200() throws Exception {
            var stats = EmployeeStatsDTO.builder()
                    .totalEmployees(10L)
                    .employeesByDepartment(Map.of(1L, 5L, 2L, 3L))
                    .employeesWithoutDepartment(2L)
                    .build();
            when(service.getStats()).thenReturn(stats);

            mvc.perform(get("/api/v1/employees/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalEmployees").value(10))
                    .andExpect(jsonPath("$.employeesByDepartment.1").value(5))
                    .andExpect(jsonPath("$.employeesByDepartment.2").value(3))
                    .andExpect(jsonPath("$.employeesWithoutDepartment").value(2));
        }
    }
}
