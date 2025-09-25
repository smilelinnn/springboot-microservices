package com.example.employee.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class EmployeeCreatedEvent extends ApplicationEvent {
    private final Long employeeId;
    private final String email;
    private final Long departmentId;
    private final String firstName;
    private final String lastName;

    public EmployeeCreatedEvent(Object source, Long employeeId, String email, Long departmentId, String firstName, String lastName) {
        super(source);
        this.employeeId = employeeId;
        this.email = email;
        this.departmentId = departmentId;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
