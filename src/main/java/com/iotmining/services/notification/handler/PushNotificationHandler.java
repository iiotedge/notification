package com.iotmining.services.notification.handler;


import com.iotmining.common.base.notifications.dto.NotificationWrapper;
import com.iotmining.common.base.notifications.dto.payload.PushPayload;
import com.iotmining.common.base.notifications.enums.NotificationType;
import com.iotmining.common.base.notifications.dto.NotificationResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PushNotificationHandler implements NotificationHandler<PushPayload> {

    @Override
    public NotificationType getType() {
        return NotificationType.PUSH;
    }

    @Override
    public NotificationResponse handle(NotificationWrapper<PushPayload> request) {
        UUID userId = request.getUserId();
        PushPayload payload = request.getPayload();

        if (userId == null) throw new IllegalArgumentException("userId is required for Push");

        System.out.printf("📲 [PUSH] userId=%s token=%s\nTitle: %s\nMsg: %s\n",
                userId, payload.getDeviceToken(), payload.getTitle(), payload.getMessage());

        // TODO: Add actual FCM/WebPush logic here

        NotificationResponse res = new NotificationResponse();
        res.setDelivered(true);
        res.setChannel("PUSH");
        return res;
    }

    @Override
    public Class<PushPayload> payloadType() {
        return PushPayload.class;
    }
}
