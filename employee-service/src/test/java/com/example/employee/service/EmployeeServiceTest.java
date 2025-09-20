package com.example.employee.service;

import com.example.employee.client.DepartmentClient;
import com.example.employee.domain.Employee;
import com.example.employee.dto.EmployeeDTO;
import com.example.employee.exception.DuplicateEmailException;
import com.example.employee.repo.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class EmployeeServiceTest {
    @Mock
    EmployeeRepository repository;
    @Mock
    DepartmentClient departmentClient;

    @InjectMocks
    EmployeeService service;

    @ParameterizedTest(name = "create({0}) → duplicate? {1}")
    @CsvSource({
            "dina@example.com, false",
            "alice@example.com, true"
    })
    @DisplayName("create(): throws when email exists; persists otherwise")
    // CREATE，参数化测试
    void create_handles_duplicates(String email, boolean duplicate) {
        when(repository.existsByEmail(email)).thenReturn(duplicate);
        // 负例：邮箱重复，抛出 IllegalArgumentException 异常
        if (duplicate) {
            assertThatThrownBy(() -> service.create(EmployeeDTO.builder()
                    .firstName("X").lastName("Y").email(email).build(), null))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining("Email already exists");
        }

        // 正例：邮箱未重复，创建成功
        else {
            when(repository.save(any(Employee.class)))
                    .thenAnswer(inv -> { Employee e = inv.getArgument(0); e.setId(101L); return e; });
            var out = service.create(EmployeeDTO.builder()
                    .firstName("X").lastName("Y").email(email).build(), null);
            assertThat(out.getId()).isEqualTo(101L);
        }
    }

}
