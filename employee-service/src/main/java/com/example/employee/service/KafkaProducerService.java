package com.example.employee.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 发送员工事件到Kafka
     */
    public void sendEmployeeEvent(String eventType, Object eventData) {
        kafkaTemplate.send("employee-events", eventType, eventData);
        log.info("员工事件已发送: {}", eventType);
    }

    /**
     * 发送部门事件到Kafka
     */
    public void sendDepartmentEvent(String eventType, Object eventData) {
        kafkaTemplate.send("department-events", eventType, eventData);
        log.info("部门事件已发送: {}", eventType);
    }

    /**
     * 发送通知事件到Kafka
     */
    public void sendNotificationEvent(String eventType, Object eventData) {
        kafkaTemplate.send("notifications", eventType, eventData);
        log.info("通知事件已发送: {}", eventType);
    }
}
