# Spring Boot Microservices Project Report

## 1. Project Overview

This project implements a microservices architecture with Spring Boot, covering employee, department, and product services. It includes service discovery, configuration management, API gateway routing, caching, monitoring, and event-driven communication.

## 2. Core Business Modules

### 2.1 Employee Module

- Purpose: Employee management

- Features: CRUD, department association, versioning (v1/v2)

- Technology: Spring Boot, JPA, Redis caching (v2)

- Database: MySQL with Flyway migrations

- Location: employee-service/

### 2.2 Department Module

- Purpose: Department management

- Features: CRUD, employee association, versioning (v1/v2)

- Technology: Spring Boot, JPA, Redis caching (v2)

- Database: MySQL  with Flyway migrations

- Location: department-service/

### 2.3 Product Module

- Purpose: Product catalog via third-party API

- Features: Product listing, stats, external API integration

- Technology: Spring Boot, WebClient, Redis caching

- External API: FakeStore API

- Location: product-service/

## 3. Infrastructure Services

### 3.1 API Gateway (Port: 8080)

- Purpose: Single entry point, routing, load balancing

- Features: Version-based routing, path rewriting, load balancing

- Configuration: config-repo/api-gateway.yml

- Location: api-gateway/

### 3.2 Config Server (Port: 8888)

- Purpose: Centralized configuration

- Features: Environment-specific configs, centralized configuration management

- Configuration: config-server/.. / application.yml

- Location: config-server/

### 3.3 Discovery Service - Eureka (Port: 8761)

- Purpose: Service registration and discovery

- Features: Instance registration, health checks, load balancing

- Configuration: config-repo/discovery-service.yml

- Location: discovery-service/

## 4. Cross-Cutting Concerns

### 4.1 Redis Cache (Port: 6379)

- Purpose: Performance and reduced database load

- Implementation: @Cacheable, @CacheEvict in v2 controllers

- Configuration: TTL 5 minutes

- Location: Applied in EmployeeControllerV2, DepartmentControllerV2, ProductControllerV2

### 4.2 Kafka Event System (Port: 9092)

- Purpose: Asynchronous communication
- Components: KafkaConsumerService, KafkaProducerService
- Events: DepartmentCreatedEvent, DepartmentUpdatedEvent, DepartmentDeletedEvent
- Location: department-service/src/main/java/com/example/department/web/DepartmentControllerV2
- Events: EmployeeCreatedEvent, EmployeeUpdatedEvent, EmployeeDeletedEvent
- Location: employee-service/src/main/java/com/example/employee/service/EmployeeService

### 4.3 Prometheus Monitoring

- Purpose: Metrics and health monitoring

- Implementation: Actuator endpoints

- Endpoints: /actuator/prometheus, /actuator/health, /actuator/metrics

- Location: Applied in all services via pom.xml dependencies

### 4.4 Global Exception Handler

- Purpose: Centralized error handling
- Implementation: @ControllerAdvice
- Features: Consistent error responses
- Location: department-service/src/main/java/com/example/department/exception/GlobalExceptionHandler.java
- Location: employee-service/src/main/java/com/example/employee/exception/GlobalExceptionHandler.java

### 4.5 Circuit Breaker

- Purpose: Fault tolerance and graceful degradation

- Implementation: Resilience4j

- Features: Fallback methods, failure detection

- Location: employee-service/src/main/java/com/example/employee/client/DepartmentClient.java

- Location: employee-service/src/main/java/com/example/employee/client/DepartmentClientFallback.java

### 4.6 Centralized Logging

- Purpose: Unified logging across services

- Implementation: Logback with structured logging

- Features: Trace ID correlation, log levels

- Location: logback-spring.xml in each service

## 5. API Versioning Strategy

### 5.1 Version 1 (V1)

- Purpose: Legacy API

- Features: Basic CRUD

- Endpoints: /api/v1/employees/xx, /api/v1/departments/xx

### 5.2 Version 2 (V2)

- Purpose: Enhanced API with caching

- Features: Redis caching, improved performance

- Endpoints: /api/v2/employees/xx, /api/v2/departments/**, /api/v2/products/xx

## 6. Load Balancing Implementation

### 6.1 API Gateway Load Balancing

- Implementation: lb:// prefix in routes

- Configuration: config-repo/api-gateway.yml

- Features: Automatic instance selection, health checks

### 6.2 Service Discovery Integration

- Implementation: Eureka registration

- Features: Dynamic instance discovery, automatic failover

## 7. Key Benefits

- Independent scaling per service
- Load balancing across instances
- Circuit breakers for fault tolerance
- Fallback mechanisms
- Health monitoring
- Redis caching
- Asynchronous processing with Kafka
- Centralized configuration
- Service discovery
- Consistent error handling

## 8. Technology Stack

- Framework: Spring Boot 3.0.9

- Service Discovery: Eureka

- API Gateway: Spring Cloud Gateway

- Configuration: Spring Cloud Config

- Database: MySQL with Flyway

- Caching: Redis

- Event Communiation: Apache Kafka

- Monitoring: Prometheus

- Build Tool: Maven

## 9. Conclusion

The project demonstrates a microservices architecture with service discovery, configuration management, API gateway routing, caching, monitoring, and event-driven communication. It supports scalability, resilience, and maintainability through clear separation of concerns and cross-cutting concerns.