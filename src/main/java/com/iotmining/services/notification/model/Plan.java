package com.iotmining.services.notification.model;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Getter
public enum Plan {
    BASIC(2),
    PREMIUM(5),
    ENTERPRISE(200);

    private final int tokensPerMinute;

    Plan(int tokensPerMinute) {
        this.tokensPerMinute = tokensPerMinute;
    }

    public Bandwidth getBasicBandwidth() {
        return Bandwidth.builder()
                .capacity(tokensPerMinute)
                .refillGreedy(tokensPerMinute, Duration.ofMinutes(1))
                .build();
//        return Bandwidth.classic(tokensPerMinute, Refill.intervally(tokensPerMinute, Duration.ofMinutes(1)));
    }

    // For high priority messages - 25% more capacity
    public Bandwidth getHighPriorityBandwidth() {
        int highPriorityTokens = (int) (tokensPerMinute * 1.25);
        return Bandwidth.builder()
                .capacity(highPriorityTokens)
                .refillGreedy(highPriorityTokens, Duration.ofMinutes(1))
                .build();
//        return Bandwidth.classic(highPriorityTokens, Refill.intervally(highPriorityTokens, Duration.ofMinutes(1)));
    }
}