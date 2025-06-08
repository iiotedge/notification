package com.iotmining.services.notification.configuration;

import com.iotmining.common.interfaces.notification.SmsProvider;
import com.iotmining.services.notification.provider.InfobipSmsProvider;
import com.iotmining.services.notification.provider.NexmoSmsProvider;
import com.iotmining.services.notification.provider.TwilioSmsProvider;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmsProviderConfig {

    @Bean
    @ConditionalOnProperty(name = "messaging.provider", havingValue = "twilio")
    public SmsProvider twilioSmsProvider(
            @Value("${messaging.twilio.account-sid}") String sid,
            @Value("${messaging.twilio.auth-token}") String token,
            @Value("${messaging.twilio.phone-number}") String from
    ) {
        return new TwilioSmsProvider(sid, token, from);
    }

    @Bean
    @ConditionalOnProperty(name = "messaging.provider", havingValue = "infobip")
    public SmsProvider infobipSmsProvider(
            @Value("${messaging.infobip.api-key}") String apiKey,
            @Value("${messaging.infobip.base-url}") String baseUrl,
            @Value("${messaging.infobip.sender}") String sender
    ) {
        return new InfobipSmsProvider(apiKey, baseUrl, sender);
    }

    @Bean
    @ConditionalOnProperty(name = "messaging.provider", havingValue = "nexmo")
    public SmsProvider nexmoSmsProvider(
            @Value("${messaging.nexmo.api-key}") String apiKey,
            @Value("${messaging.nexmo.api-secret}") String apiSecret,
            @Value("${messaging.nexmo.from}") String sender
    ) {
        return new NexmoSmsProvider(apiKey, apiSecret, sender);
    }
}
