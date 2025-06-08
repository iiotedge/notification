//package com.iotmining.services.notification.services.sender.apis;
//
//import com.iotmining.common.base.notifications.NotificationDtoImpl;
//import com.iotmining.common.base.notifications.dto.NotificationResponse;
//import com.iotmining.common.data.notifications.NotificationChannel;
//
//import com.iotmining.common.data.notifications.NotificationStatus;
//
//import com.iotmining.common.interfaces.notification.NotificationSender;
//import com.iotmining.services.notification.annotations.SmsRateLimited;
////import com.iotmining.services.notification.dto.TelegramDto;
//import com.iotmining.services.notification.externalapis.TelegramProperties;
//import com.iotmining.services.notification.model.Plan;
//import com.iotmining.services.notification.model.Priority;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.*;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Collections;
//
//@Slf4j
//@RequiredArgsConstructor
//@Component
//public class TelegramNotificationSender implements NotificationSender {
//
//    private final TelegramProperties telegramProperties;
//    private final RestTemplate restTemplate;
//
//    private static final int RETRIES = 3;
//    private static final int DELAY_MS = 2000;
//
//    @Override
//    public boolean supports(NotificationChannel channel) {
//        return NotificationChannel.TELEGRAM == channel;
//    }
//
//    @Override
////    @SmsRateLimited(userId = "#dto.userId", plan = Plan.BASIC, priority = Priority.LOW)
//    public NotificationResponse send(NotificationDtoImpl dto) {
//        for (int attempt = 1; attempt <= RETRIES; attempt++) {
//            try {
//                String url = "https://api.telegram.org/bot" + telegramProperties.getBotToken() + "/sendMessage";
//
//                HttpHeaders headers = new HttpHeaders();
//                headers.setContentType(MediaType.APPLICATION_JSON);
//                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//
//                String body = "{\"chat_id\":\"" + telegramProperties.getChatId() + "\",\"text\":\"" + dto.getMessage() + "\"}";
//                HttpEntity<String> request = new HttpEntity<>(body, headers);
//
//                ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
//
//                if (response.getStatusCode() == HttpStatus.OK) {
//                    log.info("Telegram alert sent successfully: {}", dto.getMessage());
//                    return NotificationResponse.builder()
//                            .status(NotificationStatus.SUCCESS)
//                            .message("Telegram alert sent successfully.")
//                            .build();
//                } else {
//                    log.error("Failed to send Telegram alert, status code: {}", response.getStatusCodeValue());
//                }
//
//            } catch (Exception e) {
//                log.error("Attempt {}/{} failed to send Telegram alert: {}", attempt, RETRIES, e.getMessage());
//                if (attempt == RETRIES) {
//                    log.error("All retry attempts failed. Giving up.");
//                    return NotificationResponse.builder()
//                            .status(NotificationStatus.FAILURE)
//                            .message("Failed to send Telegram alert after retries: " + e.getMessage())
//                            .build();
//                }
//                try {
//                    Thread.sleep(DELAY_MS);
//                } catch (InterruptedException interruptedException) {
//                    Thread.currentThread().interrupt();
//                    log.error("Thread interrupted while retrying telegram alert", interruptedException);
//                }
//            }
//        }
//
//        // Should never reach here
//        return NotificationResponse.builder()
//                .status(NotificationStatus.FAILURE)
//                .message("Unknown error in sending Telegram alert.")
//                .build();
//    }
//
////    public NotificationResponse send(int telegramUserId, TelegramDto dto) {
////        for (int attempt = 1; attempt <= RETRIES; attempt++) {
////            try {
////                String url = "https://api.telegram.org/bot" + telegramProperties.getBotToken() + "/sendMessage";
////
////                HttpHeaders headers = new HttpHeaders();
////                headers.setContentType(MediaType.APPLICATION_JSON);
////                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
////
////                // Use the provided chat ID instead of a fixed one from properties
////                String body = "{\"chat_id\":\"" + telegramUserId + "\",\"text\":\"" + dto.getMessage() + "\"}";
////
////                HttpEntity<String> request = new HttpEntity<>(body, headers);
////
////                ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
////
////                if (response.getStatusCode() == HttpStatus.OK) {
////                    log.info("Telegram alert sent successfully: {}", dto.getMessage());
////                    return NotificationResponse.builder()
////                            .status(NotificationStatus.SUCCESS)
////                            .message("Telegram alert sent successfully.")
////                            .build();
////                } else {
////                    log.error("Failed to send Telegram alert, status code: {}", response.getStatusCodeValue());
////                }
////
////            } catch (Exception e) {
////                log.error("Attempt {}/{} failed to send Telegram alert: {}", attempt, RETRIES, e.getMessage());
////                if (attempt == RETRIES) {
////                    log.error("All retry attempts failed. Giving up.");
////                    return NotificationResponse.builder()
////                            .status(NotificationStatus.FAILURE)
////                            .message("Failed to send Telegram alert after retries: " + e.getMessage())
////                            .build();
////                }
////                try {
////                    Thread.sleep(DELAY_MS);
////                } catch (InterruptedException interruptedException) {
////                    Thread.currentThread().interrupt();
////                    log.error("Thread interrupted while retrying telegram alert", interruptedException);
////                }
////            }
////        }
////        // Should never reach here
////        return NotificationResponse.builder()
////                .status(NotificationStatus.FAILURE)
////                .message("Unknown error in sending Telegram alert.")
////                .build();
////    }
//}
