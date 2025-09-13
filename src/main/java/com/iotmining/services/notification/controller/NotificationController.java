package com.iotmining.services.notification.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.iotmining.common.base.notifications.dto.BaseResponse;
import com.iotmining.common.base.notifications.dto.NotificationResponse;
import com.iotmining.services.notification.services.dispatcher.NotificationDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for handling notification-related API requests.
 * This controller serves as the entry point for sending notifications,
 * orchestrating the dispatch process to various notification handlers.
 * It focuses on providing immediate, local traceability for each incoming request
 * using an internal request ID.
 */
@RestController
@Slf4j // Provides a logger instance named 'log' for logging messages
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationDispatcher dispatcher;

    /**
     * Constructs a new NotificationController with the necessary dispatcher.
     * Spring's @Autowired handles the injection of the NotificationDispatcher.
     *
     * @param dispatcher The service responsible for dispatching notifications to appropriate handlers.
     */
    @Autowired
    public NotificationController(NotificationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * Handles POST requests to the /api/notifications/send endpoint.
     * This method receives a raw JSON request body, generates an internal request ID
     * for local traceability, dispatches the notification through the NotificationDispatcher,
     * and returns a standardized BaseResponse.
     *
     * @param requestBody The JSON payload containing the notification details.
     * @return A {@link BaseResponse} indicating the success or failure of the notification dispatch.
     * The response includes the correlation ID (if available from the dispatcher) and delivery status.
     */
    @PostMapping("/send")
    public BaseResponse<NotificationResponse> send(@RequestBody JsonNode requestBody) {
        // Generate a unique internal ID for this specific incoming HTTP request.
        // This ID helps trace the request's journey within this controller method and its immediate downstream calls.
        String internalRequestId = UUID.randomUUID().toString();

        // Put the internal ID into the Mapped Diagnostic Context (MDC).
        // This makes 'internalRequestId' available in all log messages emitted by the current thread.
        MDC.put("internalRequestId", internalRequestId);

        try {
            // Log the reception of the notification send request.
            // Using INFO level for high-level operational visibility.
            log.info("Received notification send request (Internal ID: {})", internalRequestId);

            // Log a debug snippet of the incoming request body.
            // This is useful for detailed debugging of request payloads without logging the entire (potentially large or sensitive) body at higher levels.
            log.debug("Request body (Internal ID: {}, first 200 chars): {}",
                    internalRequestId, requestBody.toString().substring(0, Math.min(requestBody.toString().length(), 200)));

            // Dispatch the notification request to the NotificationDispatcher service.
            // The dispatcher is responsible for parsing the payload, identifying the notification type,
            // and routing it to the appropriate handler, as well as handling correlation IDs downstream.
            NotificationResponse response = dispatcher.dispatch(requestBody);

            // Log the successful completion of the notification dispatch.
            // Includes the internal ID and the delivery status reported by the dispatcher.
            log.info("Notification dispatch completed (Internal ID: {}). Delivered: {}",
                    internalRequestId, response.isDelivered());

            // Return a success response. The correlation ID returned by the dispatcher
            // is used in the BaseResponse to maintain end-to-end traceability for the client.
            String correlationId = response.getCorrelationId() != null ? response.getCorrelationId().toString() : null;
            return BaseResponse.success(correlationId, response, response.isDelivered());

        } catch (Exception e) {
            // Log any unexpected exceptions that occur during the dispatch process.
            // ERROR level is used for critical failures. The internal ID provides context,
            // and the full stack trace ('e') is included for thorough debugging.
            log.error("Error dispatching notification (Internal ID: {}): {}. Details: {}",
                    internalRequestId, e.getMessage(), e);

            // Return a failure response, using the internal ID if a correlation ID is not available.
            return BaseResponse.failure(internalRequestId, "Error: " + e.getMessage());

        } finally {
            // Crucially, clear the MDC after the request processing is complete.
            // This prevents context (like the internalRequestId) from leaking to other requests
            // that might be processed by the same thread from the thread pool, avoiding log confusion.
            log.debug("MDC cleared for internalRequestId: {}", internalRequestId);
            MDC.clear();
        }
    }

    // in com.iotmining.services.notification.controller.NotificationController
    @PostMapping("/internal/send")
    public BaseResponse<NotificationResponse> internalSend(
            @RequestBody com.fasterxml.jackson.databind.JsonNode requestBody,
            @RequestHeader("X-Prospect-ID") String prospectId) {

        String internalRequestId = java.util.UUID.randomUUID().toString();
        org.slf4j.MDC.put("internalRequestId", internalRequestId);
        org.slf4j.MDC.put("prospectId", prospectId);

        try {
            // Ensure 'userId' exists for dispatcher (it expects a textual UUID)
            com.fasterxml.jackson.databind.node.ObjectNode mutable = requestBody.deepCopy();
            if (!mutable.hasNonNull("userId")) {
                mutable.put("userId", prospectId);
            }

            NotificationResponse response = dispatcher.dispatch(mutable);
            String correlationId = response.getCorrelationId() != null ? response.getCorrelationId().toString() : null;
            return BaseResponse.success(correlationId, response, response.isDelivered());

        } catch (Exception e) {
            log.error("Internal notification error (ID {}): {}", internalRequestId, e.getMessage(), e);
            return BaseResponse.failure(internalRequestId, "Error: " + e.getMessage());
        } finally {
            org.slf4j.MDC.clear();
        }
    }
}

//
////@RestController
////@RequestMapping("/api/notify")
////public class NotificationController {
////
////    @PostMapping("/send")
////    public void sendNotification(@RequestBody NotificationPayload payload) {
////        payload.setRead(false);
////        payload.setCreatedTime(java.time.Instant.now().toString());
////        payload.setId(java.util.UUID.randomUUID().toString());
////
////        NotificationWebSocketHandler.broadcast(payload);  // ✅ call static broadcaster
////    }
//////    @DeleteMapping("/notifications/{id}")
//////    public ResponseEntity<Void> deleteNotification(@PathVariable String id) {
//////        notificationService.deleteById(id); // assuming you store by ID
//////        return ResponseEntity.noContent().build();
//////    }
////
////}
//
//
//@RestController
//@Slf4j
//@RequestMapping("/api/notifications")
//public class NotificationController {
//
//    @Autowired
//    private NotificationDispatcher dispatcher;
//
//    @PostMapping("/send")
//    public BaseResponse<NotificationResponse> send(
//            @RequestBody JsonNode requestBody,
//            @RequestHeader(value = "Correlation-ID", required = false) String cid,
//            @RequestHeader(value = "Tenant-ID", required = false) String tenantId
//    ) {
//        // If Correlation-ID is not provided, generate a new one
//        if (cid == null) {
//            cid = UUID.randomUUID().toString();
//            // Log at debug level when a new Correlation-ID is generated
//            log.debug("Generated new Correlation-ID: {}", cid);
//        }
//
//        // Put Correlation-ID into MDC for tracing across logs
//        MDC.put("correlationId", cid);
//
//        // Put Tenant-ID into MDC if present, and log its presence or absence
//        if (tenantId != null) {
//            MDC.put("tenantId", tenantId);
//            log.debug("Tenant-ID received: {}", tenantId);
//        } else {
//            log.debug("No Tenant-ID provided for the request.");
//        }
//
//        // Log the start of the notification send request processing
//        log.info("Received notification send request for Correlation-ID: {}", cid);
//        // Optionally, log a snippet of the request body for debugging if it's not sensitive
//         log.debug("Request body (first 200 chars): {}", requestBody.toString().substring(0, Math.min(requestBody.toString().length(), 200)));
//
//        try {
//            // Log before dispatching the notification to indicate intent
//            log.debug("Attempting to dispatch notification for Correlation-ID: {}", cid);
//            NotificationResponse response = dispatcher.dispatch(requestBody);
//
//            // Log the outcome of the dispatch, including delivery status
//            log.info("Notification dispatch completed for Correlation-ID: {}. Delivered: {}", cid, response.isDelivered());
//            return BaseResponse.success(cid, response, response.isDelivered());
//        } catch (Exception e) {
//            // Log errors with the full stack trace for better debugging.
//            // This clearly indicates the failure point and the associated correlation ID.
//            log.error("Error dispatching notification for Correlation-ID {}: {}", cid, e.getMessage(), e);
//            return BaseResponse.failure(cid, "Error: " + e.getMessage());
//        } finally {
//            // Always clear MDC to prevent context leakage to subsequent requests
//            MDC.clear();
//            // Log that MDC has been cleared for this request's correlation ID
//            log.debug("MDC cleared for Correlation-ID: {}", cid);
//        }
//    }
//}
