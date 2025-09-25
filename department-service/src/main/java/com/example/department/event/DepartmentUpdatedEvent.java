package com.example.department.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DepartmentUpdatedEvent extends ApplicationEvent {
    private final Long departmentId;
    private final String name;
    private final String code;
    private final String description;

    public DepartmentUpdatedEvent(Object source, Long departmentId, String name, String code, String description) {
        super(source);
        this.departmentId = departmentId;
        this.name = name;
        this.code = code;
        this.description = description;
    }
}
