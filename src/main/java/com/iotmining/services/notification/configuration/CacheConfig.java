package com.iotmining.services.notification.configuration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // Spring Boot will auto-configure Caffeine cache based on properties
}