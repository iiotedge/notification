package com.iotmining.services.notification.dto;


import com.iotmining.services.notification.enums.NotificationSeverity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationPayload {
    private String id;
    private String title;
    private String message;
    private String type;
    private String entityId;
    private boolean read;
    private String createdTime;
    private NotificationSeverity severity;
}
