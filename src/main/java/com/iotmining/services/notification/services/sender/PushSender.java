//package com.iotmining.services.notification.services.sender;
//
//import com.iotmining.common.base.notifications.NotificationDtoImpl;
//import com.iotmining.common.base.notifications.dto.NotificationResponse;
//import com.iotmining.common.data.notifications.NotificationChannel;
//import com.iotmining.common.data.notifications.NotificationStatus;
//import com.iotmining.common.interfaces.notification.NotificationSender;
//
//
//import com.google.firebase.messaging.FirebaseMessaging;
//import com.google.firebase.messaging.Message;
//import com.google.firebase.messaging.Notification;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//
//@Service
//public class PushSender implements NotificationSender {
//
//    private final FirebaseMessaging firebaseMessaging;
//
//    public PushSender(FirebaseMessaging firebaseMessaging) {
//        this.firebaseMessaging = firebaseMessaging;
//    }
//
//    @Override
//    public boolean supports(NotificationChannel channel) {
//        return channel == NotificationChannel.PUSH;
//    }
//
//    //    @Override
//    public NotificationResponse send(NotificationDtoImpl dto) {
//        // Using the builder to create a Notification
//        Notification notification = Notification.builder()
//                .setTitle("Alert")
//                .setBody(dto.getMessage()) // Message content from NotificationDto
//                .build();
//
//        // Build the message to send via FCM
//        Message message = Message.builder()
//                .setToken(dto.getMeta().get("fcmToken"))  // Assuming meta contains FCM token
//                .setNotification(notification)
//                .build();
//
//        try {
//
//            // Send the notification and get messageId
//            String messageId = firebaseMessaging.send(message);
//
//            System.out.println("Push notification sent successfully!");
//            return NotificationResponse.builder()
//                    .status(NotificationStatus.SUCCESS)
//                    .message("Push notification sent successfully")
//                    .providerMessageId(messageId)
//                    .metadata(Map.of(
//                            "token", dto.getMeta().get("fcmToken"),
//                            "title", "Alert"
//                    ))
//                    .build();
//
//        } catch (Exception e) {
//            return NotificationResponse.builder()
//                    .status(NotificationStatus.SUCCESS)
//                    .message("Push notification failed: " + e.getMessage())
//                    .metadata(Map.of(
//                            "token", dto.getMeta().get("fcmToken"),
//                            "error", e.getMessage()
//                    ))
//                    .build();
//        }
//    }
//}