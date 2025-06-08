//package com.iotmining.services.notification.services.producer;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.iotmining.common.base.notifications.NotificationDtoImpl;
//import com.iotmining.common.data.notifications.NotificationChannel;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.Arrays;
//import java.util.Collections;
//
//@Service
//public class NotificationProducer {
//
//    @Autowired
//    private KafkaTemplate<String, String> kafkaTemplate;
//
//    // Kafka topic name where notifications will be sent
//    private static final String TOPIC = "notification-topic";
//
//    public void sendNotificationToKafka(String userId, String message, String fcmToken) {
//        NotificationDtoImpl dto = new NotificationDtoImpl();
//        dto.setUserId(userId);
//        dto.setMessage(message);
//        dto.setType("ALERT");
//        dto.setPriority("high");
//        dto.setTimestamp("2025-04-20T12:00:00Z");
//        dto.setChannels(Arrays.asList(NotificationChannel.PUSH));
//        dto.setMeta(Collections.singletonMap("fcmToken", fcmToken));
//
//        try {
//            // Convert the NotificationDto to a JSON string and send to Kafka topic
//            String jsonMessage = new ObjectMapper().writeValueAsString(dto);
//            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, jsonMessage);
//            kafkaTemplate.send(record);
//            System.out.println("Sent notification to Kafka!");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
