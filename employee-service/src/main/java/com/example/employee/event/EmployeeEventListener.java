package com.example.employee.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmployeeEventListener {

    @EventListener
    public void handleEmployeeCreated(EmployeeCreatedEvent event) {
        log.info("Employee created: ID={}, Email={}, Department ID={}, Name={} {}",
                event.getEmployeeId(), event.getEmail(), event.getDepartmentId(), event.getFirstName(), event.getLastName());

        // 员工创建后的处理逻辑
        // 例如：发送欢迎邮件、更新统计信息等
        log.info("Sending welcome email to: {}", event.getEmail());
    }

    @EventListener
    public void handleEmployeeUpdated(EmployeeUpdatedEvent event) {
        log.info("Employee updated: ID={}, Email={}, Old Department ID={}, New Department ID={}",
                event.getEmployeeId(), event.getEmail(), event.getOldDepartmentId(), event.getNewDepartmentId());

        // 员工更新后的处理逻辑
        // 例如：处理部门变更、发送通知等
        if (event.getOldDepartmentId() != null && !event.getOldDepartmentId().equals(event.getNewDepartmentId())) {
            log.info("Employee {} moved from department {} to department {}",
                    event.getEmployeeId(), event.getOldDepartmentId(), event.getNewDepartmentId());
        }
    }

    @EventListener
    public void handleEmployeeDeleted(EmployeeDeletedEvent event) {
        log.info("Employee deleted: ID={}, Email={}, Department ID={}, Name={} {}",
                event.getEmployeeId(), event.getEmail(), event.getDepartmentId(), event.getFirstName(), event.getLastName());

        // 员工删除后的处理逻辑
        // 例如：清理相关数据、发送通知等
        log.info("Cleaning up data for deleted employee: {}", event.getEmail());
    }
}
