//package com.iotmining.services.notification.services.sender;
//
//import com.iotmining.common.base.notifications.NotificationDtoImpl;
//import com.iotmining.common.base.notifications.dto.NotificationResponse;
//import com.iotmining.common.data.notifications.NotificationChannel;
//import com.iotmining.common.data.notifications.NotificationStatus;
//import com.iotmining.common.interfaces.notification.NotificationSender;
//import lombok.RequiredArgsConstructor;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class WebSender implements NotificationSender {
//
////    private final SimpMessagingTemplate messagingTemplate;
//
//    @Override
//    public boolean supports(NotificationChannel channel) {
//        return channel == NotificationChannel.WEB;
//    }
//
//    @Override
//    public NotificationResponse send(NotificationDtoImpl dto) {
//        return NotificationResponse.builder()
//                .status(NotificationStatus.FAILURE)
//                .message("TEST")
//                .build();
//    }
//}
