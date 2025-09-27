package com.example.employee.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // Redis缓存配置已通过application.yml完成
    // 这里只需要启用缓存注解支持
}
