package com.iotmining.services.notification.model;

import lombok.Getter;

@Getter
public enum Priority {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    STANDARD(4),
    CRITICAL(5);

    private final int value;

    Priority(int value) {
        this.value = value;
    }

}