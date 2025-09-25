package com.example.employee.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class EmployeeUpdatedEvent extends ApplicationEvent {
    private final Long employeeId;
    private final String email;
    private final Long oldDepartmentId;
    private final Long newDepartmentId;
    private final String firstName;
    private final String lastName;

    public EmployeeUpdatedEvent(Object source, Long employeeId, String email, Long oldDepartmentId, Long newDepartmentId, String firstName, String lastName) {
        super(source);
        this.employeeId = employeeId;
        this.email = email;
        this.oldDepartmentId = oldDepartmentId;
        this.newDepartmentId = newDepartmentId;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
