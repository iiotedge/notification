package com.iotmining.services.notification.externalapis;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "messaging.telegram")

@Data
public class TelegramProperties {

    private String botToken;
    private String chatId;
}
