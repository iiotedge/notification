package com.iotmining.services.notification.dto;

import com.iotmining.common.base.notifications.enums.NotificationSeverity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WsNotificationResponsePayload {
    private UUID id;
    private String title;
    private String message;
    private String type;
    private String entityId;
    private boolean read;
    private boolean deleted;
    private String createdTime;
    private String severity;
    private Map<String, Object> metadata;
}
