//package com.iotmining.services.notification.services.consumer;
//
//
//import com.fasterxml.jackson.databind.ObjectMapper;
////import com.iotmining.common.base.notifications.NotificationDtoImpl;
//import com.iotmining.services.notification.services.processor.NotificationProcessor;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.core.task.TaskExecutor;
//import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//@Service
//@EnableKafka
//public class NotificationKafkaConsumer {
//    private final TaskExecutor notificationExecutor;
//    private final NotificationProcessor processor;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public NotificationKafkaConsumer(@Qualifier("notificationExecutor") TaskExecutor notificationExecutor, NotificationProcessor processor) {
//        this.notificationExecutor = notificationExecutor;
//        this.processor = processor;
//    }
//
//    // Kafka listener that listens to the "vms-alerts" topic with a specified concurrency level
//    @KafkaListener(topics = "vms-alerts", groupId = "vms-alert-group", concurrency = "3")
//    public void listen(String message) {
//        try {
//            // Deserialize message to NotificationDtoImpl object
//            NotificationDtoImpl dto = objectMapper.readValue(message, NotificationDtoImpl.class);
//
//            // Offload processing to the notification executor
//            notificationExecutor.execute(() -> {
//                try {
//                    processor.process(dto);  // Process the notification (e.g., SMS, email)
//                } catch (Exception e) {
//                    // Log exception with proper message context
//                    System.err.println("Error processing notification: " + e.getMessage());
////                    e.printStackTrace();
//                }
//            });
//        } catch (Exception e) {
//            // Log the error when unable to deserialize the message
//            System.err.println("Failed to deserialize message: " + e.getMessage());
////            e.printStackTrace();
//        }
//    }
//}
//
////@Service
////@RequiredArgsConstructor
////public class NotificationKafkaConsumer {
////
////    private final NotificationProcessor processor;
////    private final ObjectMapper objectMapper = new ObjectMapper();
////
//////    @KafkaListener(topics = "${kafka.consumer.notification-topic}", groupId = "notification-service")
////    @KafkaListener(topics = "vms-alerts", groupId = "vms-alert-group", concurrency = "3")
////    public void listen(String message) {
////        try {
////            NotificationDtoImpl dto = objectMapper.readValue(message, NotificationDtoImpl.class);
//////            System.out.println("🔥 VMS Alert Received: " + message);
////            processor.process(dto);
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////    }
////}
