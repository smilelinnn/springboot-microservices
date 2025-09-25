package com.example.department.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DepartmentEventListener {

    @EventListener
    public void handleDepartmentCreated(DepartmentCreatedEvent event) {
        log.info("Department created: ID={}, Name={}, Code={}",
                event.getDepartmentId(), event.getName(), event.getCode());

        // 部门创建后的处理逻辑
        // 例如：初始化部门统计信息、发送通知等
        log.info("Initializing department statistics for: {}", event.getName());
    }

    @EventListener
    public void handleDepartmentUpdated(DepartmentUpdatedEvent event) {
        log.info("Department updated: ID={}, Name={}, Code={}",
                event.getDepartmentId(), event.getName(), event.getCode());

        // 部门更新后的处理逻辑
        // 例如：更新缓存、发送通知等
        log.info("Updating department cache for: {}", event.getName());
    }

    @EventListener
    public void handleDepartmentDeleted(DepartmentDeletedEvent event) {
        log.info("Department deleted: ID={}, Name={}, Code={}",
                event.getDepartmentId(), event.getName(), event.getCode());

        // 部门删除后的处理逻辑
        // 例如：清理相关数据、发送通知等
        log.info("Cleaning up data for deleted department: {}", event.getName());
    }
}
