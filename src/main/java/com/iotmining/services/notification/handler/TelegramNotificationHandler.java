package com.iotmining.services.notification.handler;

import com.iotmining.common.base.notifications.dto.NotificationWrapper;
import com.iotmining.common.base.notifications.dto.payload.TelegramPayload;
import com.iotmining.common.base.notifications.enums.NotificationType;
import com.iotmining.common.data.notifications.NotificationStatus;
import com.iotmining.common.base.notifications.dto.NotificationResponse;
import com.iotmining.services.notification.externalapis.TelegramProperties;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramNotificationHandler implements NotificationHandler<TelegramPayload> {

    private final TelegramProperties telegramProperties;
    private final RestTemplate restTemplate;

    private static final int RETRIES = 3;
    private static final long DELAY_MS = 2000L;

    @Override
    public NotificationType getType() {
        return NotificationType.TELEGRAM;
    }

    /**
     * Attempts to send a Telegram notification with retry logic.
     * Logs detailed information about each attempt and outcome.
     *
     * @param request The notification wrapper containing TelegramPayload and metadata.
     * @return A NotificationResponse indicating success or failure.
     */
    public NotificationResponse send(NotificationWrapper<TelegramPayload> request) {
        UUID correlationId = request.getCorrelationId();
        // Null check for payload to prevent NullPointerException if message is null
        String messageContent = request.getPayload().getMessage();
        String messageSnippet = messageContent != null ?
                messageContent.substring(0, Math.min(messageContent.length(), 50)) : "N/A";
        String chatId = telegramProperties.getChatId();

        log.debug("Attempting to send Telegram notification for Correlation-ID: {}, Chat ID: {}, Message Snippet: '{}'",
                correlationId, chatId, messageSnippet);

        for (int attempt = 1; attempt <= RETRIES; attempt++) {
            try {
                String url = "https://api.telegram.org/bot" + telegramProperties.getBotToken() + "/sendMessage";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                // Ensure messageContent is not null when building the body
                String body = "{\"chat_id\":\"" + telegramProperties.getChatId() + "\",\"text\":\"" + (messageContent != null ? messageContent : "") + "\"}";
                HttpEntity<String> httpRequest = new HttpEntity<>(body, headers);

                log.debug("Sending Telegram API request (Attempt {}/{}) for Correlation-ID: {}", attempt, RETRIES, correlationId);
                ResponseEntity<String> response = restTemplate.postForEntity(url, httpRequest, String.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    log.info("Telegram notification sent successfully for Correlation-ID: {}. Chat ID: {}", correlationId, chatId);
                    return NotificationResponse.builder()
                            .status(NotificationStatus.SUCCESS)
                            .delivered(true)
                            .message("Telegram notification sent successfully.")
                            .correlationId(correlationId) // Ensure correlationId is passed back
                            .build();
                } else {
                    // Use response.getStatusCode().value() instead of getStatusCodeValue()
                    log.warn("Telegram API responded with non-OK status (Attempt {}/{}). Correlation-ID: {}, Status: {}, Body: {}",
                            attempt, RETRIES, correlationId, response.getStatusCode().value(), response.getBody());
                }

            } catch (HttpClientErrorException | HttpServerErrorException e) {
                // Log specific HTTP client/server errors with status code and response body
                log.error("HTTP error (Attempt {}/{}) sending Telegram alert for Correlation-ID: {}. Status: {}, Body: {}, Error: {}",
                        attempt, RETRIES, correlationId, e.getStatusCode().value(), e.getResponseBodyAsString(), e.getMessage());
            } catch (ResourceAccessException e) {
                // Log network/connection errors
                log.error("Network/Resource access error (Attempt {}/{}) sending Telegram alert for Correlation-ID: {}. Error: {}",
                        attempt, RETRIES, correlationId, e.getMessage());
            } catch (Exception e) {
                // Catch any other unexpected exceptions
                log.error("Unhandled error (Attempt {}/{}) sending Telegram alert for Correlation-ID: {}: {}",
                        attempt, RETRIES, correlationId, e.getMessage(), e); // Log stack trace for unexpected errors
            }

            if (attempt < RETRIES) {
                log.info("Retrying Telegram notification for Correlation-ID: {} in {} ms...", correlationId, DELAY_MS);
                try {
                    Thread.sleep(DELAY_MS);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt(); // Restore interrupt status
                    log.warn("Thread interrupted during retry delay for Telegram alert. Correlation-ID: {}", correlationId);
                    // If interrupted, don't continue retries, just break out
                    break;
                }
            }
        }

        // If loop completes, all retries failed
        log.error("All {} retry attempts failed to send Telegram notification for Correlation-ID: {}. Giving up.", RETRIES, correlationId);
        return NotificationResponse.builder()
                .status(NotificationStatus.FAILURE)
                .delivered(false)
                .message("Failed to send Telegram alert after " + RETRIES + " retries.")
                .correlationId(correlationId) // Ensure correlationId is passed back even on failure
                .build();
    }

    /**
     * Handles the initial request for Telegram notification.
     * Logs basic information about the incoming request.
     *
     * @param request The notification wrapper containing TelegramPayload and metadata.
     * @return The response from the send method.
     */
    @Override
    public NotificationResponse handle(NotificationWrapper<TelegramPayload> request) {
        // Null check for payload message before using it
        String messageContent = request.getPayload().getMessage();
        String messageSnippet = messageContent != null ?
                messageContent.substring(0, Math.min(messageContent.length(), 100)) : "N/A";

        log.info("📨 [Telegram] Handling notification for Correlation-ID: {}, Priority: {}, Chat ID: {}",
                request.getCorrelationId(), request.getPriority(), request.getPayload().getChatId());
        log.debug("➡ Telegram message content (snippet): {}", messageSnippet);


        NotificationResponse res = send(request);
        res.setChannel("TELEGRAM");

        log.debug("Telegram notification handler finished processing for Correlation-ID: {}. Status: {}",
                request.getCorrelationId(), res.getStatus());
        return res;
    }

    @Override
    public Class<TelegramPayload> payloadType() {
        return TelegramPayload.class;
    }
}

//package com.iotmining.services.notification.handler;
//
//import com.iotmining.common.base.notifications.dto.NotificationWrapper;
//import com.iotmining.common.base.notifications.dto.payload.TelegramPayload;
//import com.iotmining.common.base.notifications.enums.NotificationType;
//import com.iotmining.common.data.notifications.NotificationStatus;
//
//import com.iotmining.common.base.notifications.dto.NotificationResponse;
//import com.iotmining.services.notification.externalapis.TelegramProperties;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.*;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Collections;
//import java.util.UUID;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class TelegramNotificationHandler implements NotificationHandler<TelegramPayload> {
//
//    private final TelegramProperties telegramProperties;
//    private final RestTemplate restTemplate;
//
//    private static final int RETRIES = 3;
//    private static final int DELAY_MS = 2000;
//
//    @Override
//    public NotificationType getType() {
//        return NotificationType.TELEGRAM;
//    }
//
//    public NotificationResponse send(NotificationWrapper<TelegramPayload> request) {
//        for (int attempt = 1; attempt <= RETRIES; attempt++) {
//            try {
//                String url = "https://api.telegram.org/bot" + telegramProperties.getBotToken() + "/sendMessage";
//
//                HttpHeaders headers = new HttpHeaders();
//                headers.setContentType(MediaType.APPLICATION_JSON);
//                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//
//                UUID userId = request.getUserId();
//                String body = "{\"chat_id\":\"" + telegramProperties.getChatId() + "\",\"text\":\"" + request.getPayload().getMessage() + "\"}";
//                HttpEntity<String> httpRequest = new HttpEntity<>(body, headers);
//
//                ResponseEntity<String> response = restTemplate.postForEntity(url, httpRequest, String.class);
//
//                if (response.getStatusCode() == HttpStatus.OK) {
//                    log.info("Telegram alert sent successfully: {}", request.getPayload().getMessage());
//                    return NotificationResponse.builder()
//                            .status(NotificationStatus.SUCCESS)
//                            .delivered(true)
//                            .message("Notification sent successfully.")
//                            .build();
//                } else {
//                    log.error("Failed to send Telegram alert, status code: {}", response.getStatusCodeValue());
//                }
//
//            } catch (Exception e) {
//                log.error("Attempt {}/{} failed to send Telegram alert: {}", attempt, RETRIES, e.getMessage());
//                if (attempt == RETRIES) {
//                    log.error("All retry attempts failed. Giving up.");
//                    NotificationResponse res = send(request);
//                    res.setDelivered(true);
//                    res.setChannel("TELEGRAM");
//
//                    return NotificationResponse.builder()
//                            .status(NotificationStatus.FAILURE)
//                            .delivered(false)
//                            .message("Failed to send Telegram alert after retries: ")
//                            .build();
//
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
//                .delivered(false)
//                .message("Unknown error in sending Telegram alert.")
//                .build();
//    }
//
//    @Override
//    public NotificationResponse handle(NotificationWrapper<TelegramPayload> request) {
//        System.out.printf("📨 [Telegram] Priority=%s, CID=%s\n", request.getPriority(), request.getCorrelationId());
//        System.out.printf("➡ Chat: %s, Msg: %s\n", request.getPayload().getChatId(), request.getPayload().getMessage());
//
//        NotificationResponse res = send(request);
//        res.setChannel("TELEGRAM");
//        return res;
//    }
//
//    @Override
//    public Class<TelegramPayload> payloadType() {
//        return TelegramPayload.class;
//    }
//}
//
