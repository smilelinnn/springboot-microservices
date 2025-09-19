package com.example.employee.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employees")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "first_name", nullable = false, length = 120)
    private String firstName;
    @Column(name = "last_name", nullable = false, length = 120)
    private String lastName;
    @Column(nullable = false, unique = true, length = 200)
    private String email;
    @Column(name = "department_id")
    private Long departmentId;
}
