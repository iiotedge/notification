//package com.iotmining.services.notification.controller;
//
//import com.iotmining.common.base.notifications.NotificationDtoImpl;
//import com.iotmining.common.base.notifications.dto.NotificationResponse;
//import com.iotmining.services.notification.exceptions.RateLimitExceededException;
//import com.iotmining.services.notification.services.RateLimitMetricsService;
//import com.iotmining.services.notification.services.sender.SmsSender;
//import com.twilio.rest.api.v2010.account.Message;
//import jakarta.validation.Valid;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/sms")
//@Slf4j
//public class SmsController {
//
//    private final SmsSender smsService;
//    private final RateLimitMetricsService metricsService;
//
//    public SmsController(SmsSender smsService, RateLimitMetricsService metricsService) {
//        this.smsService = smsService;
//        this.metricsService = metricsService;
//    }
//
//    @PostMapping("/send")
//    public ResponseEntity<Map<String, String>> sendSms(@Valid @RequestBody NotificationDtoImpl smsRequest) {
//        try {
//            NotificationResponse message = smsService.send(smsRequest);
//
//            // Record successful request
//            metricsService.recordSuccessfulRequest(smsRequest.getUserId());
//
//            Map<String, String> response = new HashMap<>();
//            response.put("status", message.getStatus().toString());
//            response.put("sid", message.getProviderMessageId());
//            response.put("message", "SMS sent successfully");
//
//            return ResponseEntity.ok(response);
//        } catch (RateLimitExceededException e) {
//            // Record rate limited request
//            metricsService.recordRateLimitedRequest(smsRequest.getUserId());
//            throw e;
//        }
//    }
//
//    @GetMapping("/metrics")
//    public ResponseEntity<Map<String, RateLimitMetricsService.UserMetrics>> getMetrics() {
//        return ResponseEntity.ok(metricsService.getAllMetrics());
//    }
//
//    @ExceptionHandler(RateLimitExceededException.class)
//    public ResponseEntity<Map<String, String>> handleRateLimitExceeded(RateLimitExceededException e) {
//        Map<String, String> response = new HashMap<>();
//        response.put("error", "Rate limit exceeded");
//        response.put("message", e.getMessage());
//
//        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Map<String, String>> handleException(Exception e) {
//        Map<String, String> response = new HashMap<>();
//        response.put("error", "Failed to send SMS");
//        response.put("message", e.getMessage());
//
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//    }
//}