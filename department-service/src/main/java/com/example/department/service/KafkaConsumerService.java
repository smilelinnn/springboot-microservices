package com.example.department.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class KafkaConsumerService {

    /**
     * 监听员工事件
     */
    @KafkaListener(topics = "employee-events", groupId = "department-service-group")
    public void handleEmployeeEvent(Map<String, Object> eventData) {
        log.info("收到员工事件: {}", eventData);

        String eventType = (String) eventData.get("eventType");
        switch (eventType) {
            case "EMPLOYEE_CREATED":
                log.info("处理员工创建事件");
                break;
            case "EMPLOYEE_UPDATED":
                log.info("处理员工更新事件");
                break;
            case "EMPLOYEE_DELETED":
                log.info("处理员工删除事件");
                break;
            default:
                log.warn("未知的员工事件类型: {}", eventType);
        }
    }

    /**
     * 监听通知事件
     */
    @KafkaListener(topics = "notifications", groupId = "department-service-group")
    public void handleNotificationEvent(Map<String, Object> eventData) {
        log.info("收到通知事件: {}", eventData);

        String eventType = (String) eventData.get("eventType");
        String message = (String) eventData.get("message");
        String recipient = (String) eventData.get("recipient");

        log.info("处理通知: 类型={}, 接收者={}, 消息={}", eventType, recipient, message);
    }
}
