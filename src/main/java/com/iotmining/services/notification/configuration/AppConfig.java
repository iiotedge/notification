package com.iotmining.services.notification.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@Log4j2
@EnableAspectJAutoProxy
public class AppConfig {

    @PostConstruct
    public void initialize() {
        log.info("Beans Constructed...");
    }
}
