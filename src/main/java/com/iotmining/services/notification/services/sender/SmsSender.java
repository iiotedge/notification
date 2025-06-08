//package com.iotmining.services.notification.services.sender;
//
//import com.iotmining.common.base.notifications.NotificationDtoImpl;
//import com.iotmining.common.base.notifications.dto.NotificationResponse;
//import com.iotmining.common.data.notifications.NotificationChannel;
//import com.iotmining.common.data.notifications.NotificationStatus;
//import com.iotmining.common.interfaces.notification.NotificationSender;
//import com.iotmining.common.interfaces.notification.SmsProvider;
//import com.iotmining.services.notification.annotations.SmsRateLimited;
//import com.iotmining.services.notification.exceptions.RateLimitExceededException;
//import com.iotmining.services.notification.model.Plan;
//import com.iotmining.services.notification.model.Priority;
//import com.iotmining.services.notification.ratelimiter.Bucket4jRateLimiterService;
//import com.iotmining.services.notification.services.RateLimitMetricsService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class SmsSender implements NotificationSender {
//
//    private final SmsProvider smsProvider; // <- Injected dynamically
//
//    private final RateLimitMetricsService metricsService;
//
//    @Override
//    public boolean supports(NotificationChannel channel) {
//        return channel == NotificationChannel.SMS;
//    }
//
//    @Autowired
//    private final Bucket4jRateLimiterService rateLimiterService;
//
//    //                    () -> {
////                        String message = "📩 SMS to: " + smsRequest.getMeta().get("phone") + " | Message: " + smsRequest.getMessage();
////                        System.out.println(message);
////                        return message; // Simulate Twilio's Message response with a String
////                    }
//    @Override
//    @SmsRateLimited(userId = "#smsRequest.userId", plan = Plan.BASIC, priority = Priority.LOW)
//    public NotificationResponse send(NotificationDtoImpl smsRequest) {
//        try {
////            smsProvider.send(smsRequest);
//
//            return NotificationResponse.builder()
//                    .status(NotificationStatus.SUCCESS)
//                    .message("SMS notification sent successfully")
//                    .providerMessageId(smsRequest.getUserId())
//                    .build();
//
//        } catch (RateLimitExceededException e) {
//            return NotificationResponse.builder()
//                    .status(NotificationStatus.FAILURE)
//                    .message("Email failed: " + e.getMessage())
//                    .build();
////            log.error("Rate limit exceeded for user {} with plan {}", smsRequest.getU(), user.getPlan());
//        } catch (Exception e) {
//            log.error("Error sending SMS: {}", e.getMessage());
//            throw new RuntimeException("Failed to send SMS: " + e.getMessage());
//        }
//    }
//}
