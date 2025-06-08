package com.iotmining.services.notification.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iotmining.common.base.notifications.dto.NotificationResponse;
import com.iotmining.common.base.notifications.dto.NotificationWrapper;
import com.iotmining.common.base.notifications.dto.payload.WebSocketPayload;
import com.iotmining.common.base.notifications.enums.NotificationType;
import com.iotmining.services.notification.dto.WsNotificationResponsePayload;
import com.iotmining.services.notification.utils.NotificationFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class WebSocketNotificationHandler implements NotificationHandler<WebSocketPayload> {

    @Autowired
    private RawWebSocketHandler webSocketHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public NotificationType getType() {
        return NotificationType.WEB;
    }

    @Override
    public NotificationResponse handle(NotificationWrapper<WebSocketPayload> request) {
        UUID userId = request.getUserId();
        if (userId == null) throw new IllegalArgumentException("Missing userId for WebSocket");

        try {
            // Convert the incoming request into the full notification payload
            WsNotificationResponsePayload payload = NotificationFactory.fromRequest(request.getPayload());

            // ✅ Wrap it into { type: "NOTIFICATION", payload: {...} }
            Map<String, Object> wrapper = new HashMap<>();
            wrapper.put("type", "NOTIFICATION");
            wrapper.put("payload", payload);

            // Serialize the wrapped message
            String message = objectMapper.writeValueAsString(wrapper);

            // Send to all or specific user
            webSocketHandler.sendToUser(userId, message);
//            webSocketHandler.sendToAll(message);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize WebSocket payload", e);
        }

        NotificationResponse res = new NotificationResponse();
        res.setDelivered(true);
        res.setChannel("WEB");
        return res;
    }

    @Override
    public Class<WebSocketPayload> payloadType() {
        return WebSocketPayload.class;
    }
}
