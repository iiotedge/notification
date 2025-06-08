//package com.iotmining.services.notification.services.processor;
//
//import com.iotmining.common.base.notifications.NotificationDtoImpl;
//import com.iotmining.services.notification.services.dispatcher.NotificationDispatcher;
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class NotificationProcessor {
//
//    private final NotificationDispatcher dispatcher;
//
//    @Async("notificationExecutor")
//    public void process(NotificationDtoImpl dto) {
//
//        try {
//            // Add any business logic for processing here, such as priority handling
//            if ("high".equalsIgnoreCase(dto.getPriority())) {
//                // Example of modifying the message or adding more logic based on priority
//                dto.setMessage("High Priority: " + dto.getMessage());
//            }
//
//            // Add rules like filter by priority etc. if needed
////            dispatcher.dispatch(dto);
//        } catch (Exception e) {
//            System.out.println("Failed to process notification"+e);
////            log.error("Failed to process notification", e);
//        }
//    }
//}
