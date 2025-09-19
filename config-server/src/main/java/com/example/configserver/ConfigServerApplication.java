package com.example.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Config Server Application
 *
 * 这是 Spring Cloud Config Server 的主启动类
 *
 * 关键注解解释：
 * - @SpringBootApplication: 标记这是一个 Spring Boot 应用
 *   - 包含 @Configuration（配置类）
 *   - 包含 @EnableAutoConfiguration（自动配置）
 *   - 包含 @ComponentScan（组件扫描）
 *
 * - @EnableConfigServer: 启用 Spring Cloud Config Server 功能
 *   - 自动配置 Config Server
 *   - 提供 REST API 端点来获取配置
 *   - 支持多种配置源（文件系统、Git、数据库等）
 *
 * Config Server 的作用：
 * 1. 集中化配置管理：所有微服务从 Config Server 获取配置
 * 2. 环境特定配置：支持不同环境（dev, test, prod）的配置
 * 3. 运行时刷新：服务可以刷新配置而不需要重启
 *      之前配置文件在服务内部，配置打包在 jar 内部，修改配置需要重新打包，
 *      现在有了 config server，配置从服务的 jar 中移出统一管理了，再配合 exposure: include: refresh 就可以实现运行时刷新
 * 4. 版本控制：配置文件的变更可以追踪
 *
 * 工作原理：
 * 1. 服务启动时从 Config Server 获取配置
 * 2. Config Server 从配置仓库（Git/文件系统）读取配置
 * 3. 服务可以通过 /actuator/refresh 端点刷新配置
 * 4. Config Server 支持多环境配置（application.yml, service-profile.yml）
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    /**
     * 主方法 - Spring Boot 应用的入口点
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}