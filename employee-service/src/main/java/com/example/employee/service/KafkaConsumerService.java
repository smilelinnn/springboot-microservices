package com.example.employee.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class KafkaConsumerService {

    /**
     * 监听部门事件
     */
    @KafkaListener(topics = "department-events", groupId = "employee-service-group")
    public void handleDepartmentEvent(Map<String, Object> eventData) {
        log.info("收到部门事件: {}", eventData);

        String eventType = (String) eventData.get("eventType");
        switch (eventType) {
            case "DEPARTMENT_CREATED":
                log.info("处理部门创建事件");
                break;
            case "DEPARTMENT_UPDATED":
                log.info("处理部门更新事件");
                break;
            case "DEPARTMENT_DELETED":
                log.info("处理部门删除事件");
                break;
            default:
                log.warn("未知的部门事件类型: {}", eventType);
        }
    }

    /**
     * 监听通知事件
     */
    @KafkaListener(topics = "notifications", groupId = "employee-service-group")
    public void handleNotificationEvent(Map<String, Object> eventData) {
        log.info("收到通知事件: {}", eventData);

        String eventType = (String) eventData.get("eventType");
        String message = (String) eventData.get("message");
        String recipient = (String) eventData.get("recipient");

        log.info("处理通知: 类型={}, 接收者={}, 消息={}", eventType, recipient, message);
    }
}
