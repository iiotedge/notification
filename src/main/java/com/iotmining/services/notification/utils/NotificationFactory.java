package com.iotmining.services.notification.utils;

import com.iotmining.common.base.notifications.dto.payload.WebSocketPayload;
import com.iotmining.common.base.notifications.enums.NotificationSeverity;
import com.iotmining.services.notification.dto.WsNotificationResponsePayload;

import java.time.Instant;
import java.util.UUID;

public class NotificationFactory {

    public static WsNotificationResponsePayload fromRequest(WebSocketPayload req) {
        WsNotificationResponsePayload payload = new WsNotificationResponsePayload();
        payload.setId(UUID.randomUUID());
        payload.setTitle(req.getTitle());
        payload.setMessage(req.getMessage());
        payload.setType(req.getType());
//        payload.setEntityId(req.getEntityId());
        payload.setSeverity(req.getMetadata().get("severity") != null ? req.getMetadata().get("severity").toString() : "INFO");
        payload.setMetadata(req.getMetadata());
        payload.setCreatedTime(Instant.now().toString());
        payload.setRead(false);
        payload.setDeleted(false);
        return payload;
    }
}
