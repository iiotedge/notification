//package com.iotmining.services.notification.grpc;
//
//import com.iotmining.services.notification.grpc.CriticalAlertProto;
//import com.iotmining.services.notification.services.processor.NotificationProcessor;
//import io.grpc.stub.StreamObserver;
//import org.springframework.stereotype.Service;
//
//@Service
//public class CriticalAlertServiceImpl extends CriticalAlertServiceGrpc.CriticalAlertServiceImplBase {
//
//    private final NotificationProcessor notificationProcessor;
//
//    // Constructor to inject dependencies
//    public CriticalAlertServiceImpl(NotificationProcessor notificationProcessor) {
//        this.notificationProcessor = notificationProcessor;
//    }
//
//    @Override
//    public void sendCriticalAlert(CriticalAlertProto.CriticalAlert request, StreamObserver<CriticalAlertProto.AlertResponse> responseObserver) {
//        try {
//            // Extract the data from the request
//            String userId = request.getUserId();
//            String message = request.getMessage();
//            String priority = request.getPriority();
//            String timestamp = request.getTimestamp();
//            // Here you can add logic to send notifications (SMS, Email, etc.)
//            notificationProcessor.processCriticalAlert(userId, message, priority, timestamp, request.getChannelsList(), request.getMetaMap());
//
//            // Create a successful response
//            CriticalAlertProto.AlertResponse response = CriticalAlertProto.AlertResponse.newBuilder()
//                    .setStatus("Success")
//                    .setDetails("Critical alert sent successfully")
//                    .build();
//
//            // Send the response back to the client
//            responseObserver.onNext(response);
//            responseObserver.onCompleted();
//        } catch (Exception e) {
//            // Handle error and send a failure response
//            CriticalAlertProto.AlertResponse response = CriticalAlertProto.AlertResponse.newBuilder()
//                    .setStatus("Failure")
//                    .setDetails("Failed to send critical alert: " + e.getMessage())
//                    .build();
//            responseObserver.onNext(response);
//            responseObserver.onCompleted();
//        }
//    }
//}
