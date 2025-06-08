//package com.iotmining.services.notification.controller;
//
////import com.iotmining.common.base.notifications.NotificationDtoImpl;
//import com.iotmining.common.base.notifications.dto.NotificationResponse;
//import com.iotmining.common.data.notifications.NotificationChannel;
//import com.iotmining.services.notification.dto.NotificationPayload;
//import com.iotmining.services.notification.enums.NotificationSeverity;
//import com.iotmining.services.notification.handler.NotificationWebSocketHandler;
//import com.iotmining.services.notification.services.sender.apis.TelegramNotificationSender;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
//@Slf4j
//@RestController
//@RequestMapping("/api/notifications/telegram")
//@RequiredArgsConstructor
//@Validated
//public class TelegramNotificationController {
//
//    private final TelegramNotificationSender telegramNotificationSender;
//
//    @PostMapping("/send")
//    public ResponseEntity<NotificationResponse> sendTelegramNotification(
//            @RequestBody NotificationDtoImpl dto) {
//
//        log.info("Received Telegram notification request: {}", dto);
//
//        if (!telegramNotificationSender.supports(NotificationChannel.TELEGRAM)) {
//            log.error("Telegram channel not supported.");
//            return ResponseEntity.badRequest().body(NotificationResponse.builder()
//                    .status(com.iotmining.common.data.notifications.NotificationStatus.FAILURE)
//                    .message("Telegram channel not supported.")
//                    .build());
//        }
//
//        NotificationPayload payload = new NotificationPayload();
//        payload.setType("ALERT");
//        payload.setTitle("Device threshold");
//        payload.setMessage(dto.getMessage());
//        payload.setRead(false);
//        payload.setCreatedTime(java.time.Instant.now().toString());
//        payload.setId(java.util.UUID.randomUUID().toString());
//        payload.setSeverity(NotificationSeverity.CRITICAL);
//
//        NotificationWebSocketHandler.broadcast(payload);  // ✅ call static broadcaster
//
////        NotificationResponse response = telegramNotificationSender.send(dto);
//        return ResponseEntity.ok(response);
//    }
//
////    @PostMapping("/send/{telegramUserId}")
////    public ResponseEntity<NotificationResponse> sendTelegramNotification(
////            @PathVariable("telegramUserId") int telegramUserId,
////            @RequestBody TelegramDto dto) {
////        log.info("Received Telegram notification request for user({}): {}", telegramUserId, dto);
////
////        if (!telegramNotificationSender.supports(NotificationChannel.TELEGRAM)) {
////            log.error("Telegram channel not supported.");
////            return ResponseEntity.badRequest().body(NotificationResponse.builder()
////                    .status(com.iotmining.common.data.notifications.NotificationStatus.FAILURE)
////                    .message("Telegram channel not supported.")
////                    .build());
////        }
////        NotificationResponse response = telegramNotificationSender.send(telegramUserId, dto);
////        return ResponseEntity.ok(response);
////    }
//}
