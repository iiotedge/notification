package com.iotmining.services.notification.handler;

import com.iotmining.common.base.notifications.dto.NotificationWrapper;
import com.iotmining.common.base.notifications.dto.payload.SmsPayload;
import com.iotmining.common.base.notifications.enums.NotificationType;
import com.iotmining.common.data.notifications.NotificationStatus;
import com.iotmining.common.interfaces.notification.SmsProvider;
import com.iotmining.services.notification.annotations.SmsRateLimited;
import com.iotmining.common.base.notifications.dto.BaseRequest;
import com.iotmining.common.base.notifications.dto.NotificationResponse;
import com.iotmining.services.notification.exceptions.RateLimitExceededException;
import com.iotmining.services.notification.model.Plan;
import com.iotmining.services.notification.model.Priority;
import com.iotmining.services.notification.ratelimiter.Bucket4jRateLimiterService;
import com.iotmining.services.notification.services.RateLimitMetricsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor // Consider using @RequiredArgsConstructor if you prefer final fields
@Slf4j
public class SmsNotificationHandler implements NotificationHandler<SmsPayload> {

    private final SmsProvider smsProvider;
    private final RateLimitMetricsService metricsService;

    // @Autowired is redundant with @AllArgsConstructor or @RequiredArgsConstructor for constructor injection
    private final Bucket4jRateLimiterService rateLimiterService;

    @Override
    public NotificationType getType() {
        return NotificationType.SMS;
    }

    /**
     * This method is responsible for the actual sending logic and error handling.
     * It's called internally by handle().
     */
    public NotificationResponse send(NotificationWrapper<SmsPayload> request) {
        UUID correlationId = request.getCorrelationId(); // Get correlationId for consistent logging

        try {
            log.info("Attempting to send SMS for Correlation-ID: {}", correlationId);
            smsProvider.send(request);
            log.info("SMS notification sent successfully for Correlation-ID: {}", correlationId);

            return NotificationResponse.builder()
                    .status(NotificationStatus.SUCCESS)
                    .message("SMS notification sent successfully")
                    .delivered(true)
                    .build();

        } catch (RateLimitExceededException e) {
            // This rate limit exception should ideally be caught by the AOP aspect using @SmsRateLimited
            // However, if it's explicitly thrown by smsProvider.send(), log it here.
            log.warn("Rate limit exceeded for SMS sending for User ID: {} with Correlation-ID: {}. Message: {}",
                    request.getUserId(), correlationId, e.getMessage());
            // It's important to use the correlationId consistently
            return NotificationResponse.builder()
                    .status(NotificationStatus.FAILURE)
                    .message("SMS failed: " + e.getMessage()) // Changed "Email failed" to "SMS failed"
                    .build();
        } catch (Exception e) {
            // Log the error with full stack trace for better debugging
            log.error("Error sending SMS for Correlation-ID {}: {}", correlationId, e.getMessage(), e);
            // Re-throwing a RuntimeException is generally fine here if it's an unexpected system error
            throw new RuntimeException("Failed to send SMS for Correlation-ID " + correlationId + ": " + e.getMessage());
        }
    }

    @Override
    @SmsRateLimited(userId = "#request.userId", plan = Plan.BASIC, priority = Priority.LOW)
    public NotificationResponse handle(NotificationWrapper<SmsPayload> request) {
        UUID userId = request.getUserId();
        SmsPayload payload = request.getPayload();

        // Access metadata for logging
        UUID correlationId = request.getCorrelationId();
        int retry = request.getRetryCount();
        BaseRequest.Priority priority = request.getPriority();

        // === Replaced System.out.printf with SLF4J logging ===
        log.info("📲 [SMS] Handling notification for User ID: {}, Priority: {}, Retry: {}, Correlation-ID: {}",
                userId, priority, retry, correlationId);
        log.debug("➡ SMS details: To: {}, Content: {}", payload.getPhoneNumber(), payload.getContent());

        // === ✅ Response ===
        NotificationResponse response = send(request);
        // Ensure consistent correlationId in the response for traceability
        response.setCorrelationId(correlationId);
        response.setChannel("SMS");
        // The 'delivered' status should come from the 'send' method's outcome, not hardcoded here.
        // It's already set in the 'send' method based on success/failure.
        // response.setDelivered(true); // Removed, as send() already sets this

        log.debug("SMS notification handler completed for Correlation-ID: {}. Status: {}",
                correlationId, response.getStatus());
        return response;
    }

    @Override
    public Class<SmsPayload> payloadType() {
        return SmsPayload.class;
    }
}

//package com.iotmining.services.notification.handler;
//
//import com.iotmining.common.base.notifications.dto.NotificationWrapper;
//import com.iotmining.common.base.notifications.dto.payload.SmsPayload;
//import com.iotmining.common.base.notifications.enums.NotificationType;
//import com.iotmining.common.data.notifications.NotificationStatus;
//import com.iotmining.common.interfaces.notification.SmsProvider;
//import com.iotmining.services.notification.annotations.SmsRateLimited;
//import com.iotmining.common.base.notifications.dto.BaseRequest;
//import com.iotmining.common.base.notifications.dto.NotificationResponse;
//import com.iotmining.services.notification.exceptions.RateLimitExceededException;
//import com.iotmining.services.notification.model.Plan;
//import com.iotmining.services.notification.model.Priority;
//import com.iotmining.services.notification.ratelimiter.Bucket4jRateLimiterService;
//import com.iotmining.services.notification.services.RateLimitMetricsService;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.UUID;
//
//@Component
//@AllArgsConstructor
//@Slf4j
//public class SmsNotificationHandler implements NotificationHandler<SmsPayload> {
//
//    private final SmsProvider smsProvider; // <- Injected dynamically
//
//    private final RateLimitMetricsService metricsService;
//
//    @Autowired
//    private final Bucket4jRateLimiterService rateLimiterService;
//
//    @Override
//    public NotificationType getType() {
//        return NotificationType.SMS;
//    }
//
//    public NotificationResponse send(NotificationWrapper<SmsPayload> request) {
//        try {
//            smsProvider.send(request);
//
//            return NotificationResponse.builder()
//                    .status(NotificationStatus.SUCCESS)
//                    .message("SMS notification sent successfully")
//                    .delivered(true)
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
//    @Override
//    @SmsRateLimited(userId = "#request.userId", plan = Plan.BASIC, priority = Priority.LOW)
//    public NotificationResponse handle(NotificationWrapper<SmsPayload> request) {
//        UUID serId = request.getUserId();
//        SmsPayload payload = request.getPayload();
//
//        // You can access these metadata for logging, retry control, or analytics
//        String correlationId = request.getCorrelationId();
//        int retry = request.getRetryCount();
//        BaseRequest.Priority priority = request.getPriority();
//
//        // === ✅ Your actual SMS logic here ===
//        System.out.printf("📲 [SMS] Priority=%s, Retry=%d, CID=%s\n", priority, retry, correlationId);
//        System.out.printf("➡ To: %s, Msg: %s\n", payload.getPhoneNumber(), payload.getContent());
//
//        // === ✅ Response ===
//        NotificationResponse response = send(request);
//        response.setChannel("SMS");
//        response.setDelivered(true);
//        return response;
//    }
//
//    @Override
//    public Class<SmsPayload> payloadType() {
//        return SmsPayload.class;
//    }
//}
