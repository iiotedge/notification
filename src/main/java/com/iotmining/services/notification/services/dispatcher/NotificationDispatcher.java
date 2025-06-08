package com.iotmining.services.notification.services.dispatcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iotmining.common.base.notifications.dto.BaseRequest;
import com.iotmining.common.base.notifications.dto.NotificationWrapper;
import com.iotmining.common.base.notifications.enums.NotificationType;
import com.iotmining.common.interfaces.notification.NotificationSender;
import com.iotmining.common.base.notifications.dto.NotificationResponse;
import com.iotmining.services.notification.handler.NotificationHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class NotificationDispatcher {

    private final Map<NotificationType, NotificationHandler<?>> handlerMap = new HashMap<>();
    private final ObjectMapper mapper;
    private final List<NotificationSender> senders;

    @Autowired
    public NotificationDispatcher(List<NotificationHandler<?>> handlers,
                                  ObjectMapper mapper,
                                  List<NotificationSender> senders) {
        this.mapper = mapper;
        this.senders = senders;
        handlers.forEach(h -> {
            handlerMap.put(h.getType(), h);
            // Log that a handler has been registered during initialization
            log.debug("Registered handler for NotificationType: {}", h.getType());
        });
        // Info log confirming dispatcher initialization and number of handlers
        log.info("NotificationDispatcher initialized with {} handlers.", handlers.size());
    }

    public NotificationResponse dispatch(JsonNode rawRequest) {
        // Log the initiation of the dispatch process for a new request
        log.debug("Starting dispatch process for an incoming notification request.");

        // 1. Type resolution
        String typeStr = rawRequest.path("type").asText(null);
        if (typeStr == null) {
            // Warn if the critical 'type' field is missing from the request
            log.warn("Dispatch failed: Missing 'type' field in the raw request. Request body snippet: {}",
                    rawRequest.toPrettyString().substring(0, Math.min(rawRequest.toPrettyString().length(), 200)));
            throw new IllegalArgumentException("Missing 'type' field");
        }
        // Debug log confirming the raw type string extracted
        log.debug("Extracted notification type string: {}", typeStr);

        NotificationType type;
        try {
            type = NotificationType.valueOf(typeStr);
            // Debug log confirming successful parsing of the NotificationType
            log.debug("Successfully parsed NotificationType: {}", type);
        } catch (IllegalArgumentException e) {
            // Error log for invalid NotificationType, including the problematic string and error message
            log.error("Dispatch failed: Invalid NotificationType '{}' provided. Error: {}", typeStr, e.getMessage());
            throw new RuntimeException("Invalid NotificationType: " + typeStr);
        }

        NotificationHandler<?> handler = handlerMap.get(type);
        if (handler == null) {
            // Error log when no handler is found for the resolved NotificationType
            log.error("Dispatch failed: No specific handler found for NotificationType: {}", type);
            throw new RuntimeException("No handler found for NotificationType: " + type);
        }
        // Debug log indicating which handler was found for the notification type
        log.debug("Identified handler for NotificationType {}: {}", type, handler.getClass().getName());


        // 2. Payload binding
        Class<?> payloadClass = handler.payloadType();
        // Debug log before attempting to bind the payload
        log.debug("Attempting to bind raw payload to target type: {}", payloadClass.getName());
        Object payload = mapper.convertValue(rawRequest.get("payload"), payloadClass);
        // Debug log after successful payload binding (consider sensitive data before logging payload content)
        log.debug("Payload successfully bound to type: {}", payload.getClass().getSimpleName());


        // 3. Build wrapper with metadata
        NotificationWrapper<Object> wrapper = new NotificationWrapper<>();
        wrapper.setType(type);
        wrapper.setPayload(payload);

        // Handle Correlation-ID: try to parse if textual, otherwise generate a new one
        UUID correlationId;
        JsonNode correlationNode = rawRequest.path("correlationId");
        if (correlationNode.isTextual()) {
            try {
                correlationId = UUID.fromString(correlationNode.asText());
                // Debug log if Correlation-ID was successfully parsed from request
                log.debug("Parsed Correlation-ID from request: {}", correlationId);
            } catch (IllegalArgumentException e) {
                // Warn if Correlation-ID string is invalid, then generate a new one
                correlationId = UUID.randomUUID();
                log.warn("Invalid UUID format for Correlation-ID '{}' in request. Generating new ID: {}. Error: {}",
                        correlationNode.asText(), correlationId, e.getMessage());
            }
        } else {
            // Debug log if Correlation-ID is missing or not a string, generating a new one
            correlationId = UUID.randomUUID();
            log.debug("Correlation-ID missing or not textual in request. Generating new ID: {}", correlationId);
        }
        wrapper.setCorrelationId(correlationId); // Set the resolved/generated Correlation-ID

        wrapper.setSourceApp(rawRequest.path("sourceApp").asText("unknown"));
        wrapper.setRetryCount(rawRequest.path("retryCount").asInt(0));

        // Parse priority with default fallback
        String priorityStr = rawRequest.path("priority").asText("MEDIUM");
        try {
            wrapper.setPriority(BaseRequest.Priority.valueOf(priorityStr.toUpperCase())); // Ensure case-insensitive parsing
            // Debug log confirming the set priority
            log.debug("Set priority to: {}", wrapper.getPriority());
        } catch (IllegalArgumentException e) {
            // Warn if priority value is invalid, defaulting to MEDIUM
            log.warn("Invalid priority value received: '{}'. Defaulting to MEDIUM for Correlation-ID: {}. Error: {}",
                    priorityStr, wrapper.getCorrelationId(), e.getMessage());
            wrapper.setPriority(BaseRequest.Priority.MEDIUM); // Fallback to default
        }

        wrapper.setTimestamp(rawRequest.path("timestamp").asLong(System.currentTimeMillis()));

        // Handle User-ID: ensure it's a textual UUID
        JsonNode userIdNode = rawRequest.path("userId");
        if (!userIdNode.isTextual()) {
            // Warn if userId field is not textual and throw bad request
            log.warn("Dispatch failed for Correlation-ID {}: 'userId' field is not textual. Value received: {}",
                    wrapper.getCorrelationId(), userIdNode.toPrettyString());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expected UUID string for userId, got: " + userIdNode);
        }
        try {
            UUID userId = UUID.fromString(userIdNode.asText());
            wrapper.setUserId(userId);
            // Debug log confirming successful userId parsing
            log.debug("Successfully parsed and set userId: {} for Correlation-ID: {}", userId, wrapper.getCorrelationId());
        } catch (IllegalArgumentException e) {
            // Warn if userId is not a valid UUID format and throw bad request
            log.warn("Dispatch failed for Correlation-ID {}: Invalid UUID format for userId '{}'. Error: {}",
                    wrapper.getCorrelationId(), userIdNode.asText(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid UUID format for userId");
        }

        // 4. Log the main dispatch action
        // This log serves as a key indicator of a notification being passed to its handler
        log.info("📨 Dispatching NotificationType={} with Correlation-ID={}", type, wrapper.getCorrelationId());

        // 5. Execute handler
        NotificationResponse response = dispatchTyped(handler, wrapper);
        // Debug log confirming handler execution completion
        log.debug("Handler execution completed for NotificationType={} with Correlation-ID={}", type, wrapper.getCorrelationId());
        return response;
    }

    private <T> NotificationResponse dispatchTyped(NotificationHandler<T> handler, NotificationWrapper<?> rawWrapper) {
        @SuppressWarnings("unchecked")
        NotificationWrapper<T> typedWrapper = (NotificationWrapper<T>) rawWrapper;
        // Debug log before invoking the specific handler's handle method
        log.debug("Invoking handler {}.handle() for NotificationType={} with Correlation-ID={}",
                handler.getClass().getSimpleName(), typedWrapper.getType(), typedWrapper.getCorrelationId());
        return handler.handle(typedWrapper);
    }
}

//package com.iotmining.services.notification.services.dispatcher;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
////import com.iotmining.common.base.notifications.NotificationDtoImpl;
//import com.iotmining.common.base.notifications.dto.BaseRequest;
//import com.iotmining.common.base.notifications.dto.NotificationWrapper;
//import com.iotmining.common.base.notifications.enums.NotificationType;
//import com.iotmining.common.interfaces.notification.NotificationSender;
//import com.iotmining.common.base.notifications.dto.NotificationResponse;
//import com.iotmining.services.notification.handler.NotificationHandler;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
////@Service
////@RequiredArgsConstructor
////public class NotificationDispatcher {
////
////    private final List<NotificationSender> senders;
////
////    public void dispatch(NotificationDtoImpl dto) {
////        for (NotificationChannel channel : dto.getChannels()) {
////            senders.stream()
////                    .filter(s -> s.supports(channel))
////                    .forEach(s -> s.send(dto));
////        }
////    }
////}
//@Service
//@Slf4j
//public class NotificationDispatcher {
//
//    private final Map<NotificationType, NotificationHandler<?>> handlerMap = new HashMap<>();
//    private final ObjectMapper mapper;
//
//    private final List<NotificationSender> senders;
//
////    public void dispatch(NotificationDtoImpl dto) {
////        for (NotificationChannel channel : dto.getChannels()) {
////            senders.stream()
////                    .filter(s -> s.supports(channel))
////                    .forEach(s -> s.send(dto));
////        }
////    }
//
//    @Autowired
//    public NotificationDispatcher(List<NotificationHandler<?>> handlers,
//                                  ObjectMapper mapper,
//                                  List<NotificationSender> senders) {
//        this.mapper = mapper;
//        this.senders = senders;
//        handlers.forEach(h -> handlerMap.put(h.getType(), h));
//    }
//
//    public NotificationResponse dispatch(JsonNode rawRequest) {
//        // 1. Type resolution
//        String typeStr = rawRequest.path("type").asText(null);
//        if (typeStr == null) throw new IllegalArgumentException("Missing 'type' field");
//
//        NotificationType type;
//        try {
//            type = NotificationType.valueOf(typeStr);
//        } catch (IllegalArgumentException e) {
//            throw new RuntimeException("Invalid NotificationType: " + typeStr);
//        }
//
//        NotificationHandler<?> handler = handlerMap.get(type);
//        if (handler == null) {
//            throw new RuntimeException("No handler found for NotificationType: " + type);
//        }
//
//        // 2. Payload binding
//        Class<?> payloadClass = handler.payloadType();
//        Object payload = mapper.convertValue(rawRequest.get("payload"), payloadClass);
//
//        // 3. Build wrapper with metadata
//        NotificationWrapper<Object> wrapper = new NotificationWrapper<>();
//        wrapper.setType(type);
//        wrapper.setPayload(payload);
//        wrapper.setCorrelationId(rawRequest.path("correlationId").asText(UUID.randomUUID().toString()));
//        wrapper.setSourceApp(rawRequest.path("sourceApp").asText("unknown"));
//        wrapper.setRetryCount(rawRequest.path("retryCount").asInt(0));
//        wrapper.setPriority(BaseRequest.Priority.valueOf(
//                rawRequest.path("priority").asText("MEDIUM")));
//        wrapper.setTimestamp(rawRequest.path("timestamp").asLong(System.currentTimeMillis()));
//
//        JsonNode userIdNode = rawRequest.path("userId");
//        if (!userIdNode.isTextual()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expected UUID string for userId, got: " + userIdNode);
//        }
//        try {
//            UUID userId = UUID.fromString(userIdNode.asText());
//            wrapper.setUserId(userId);
//        } catch (IllegalArgumentException e) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid UUID format for userId");
//        }
//
//
//
//        // 4. Log correlationId
//        log.info("📨 Dispatching type={} correlationId={}", type, wrapper.getCorrelationId());
//
//        // 5. Execute handler
//        return dispatchTyped(handler, wrapper);
////        @SuppressWarnings("unchecked")
////        NotificationHandler<Object> typedHandler = (NotificationHandler<Object>) handler;
////        return typedHandler.handle(wrapper);
//    }
//    private <T> NotificationResponse dispatchTyped(NotificationHandler<T> handler, NotificationWrapper<?> rawWrapper) {
//        @SuppressWarnings("unchecked")
//        NotificationWrapper<T> typedWrapper = (NotificationWrapper<T>) rawWrapper;
//        return handler.handle(typedWrapper);
//    }
//}
//
