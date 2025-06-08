package com.iotmining.services.notification.handler;


import com.iotmining.common.base.notifications.dto.NotificationWrapper;
import com.iotmining.common.base.notifications.enums.NotificationType;
import com.iotmining.common.base.notifications.dto.NotificationResponse;


public interface NotificationHandler<T> {
    NotificationType getType();
    NotificationResponse handle(NotificationWrapper<T> request);
    Class<T> payloadType();
}

