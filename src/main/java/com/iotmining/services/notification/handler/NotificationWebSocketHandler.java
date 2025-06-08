//package com.iotmining.services.notification.handler;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.iotmining.services.notification.dto.NotificationPayload;
//import org.springframework.web.socket.*;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.util.*;
//
//public class NotificationWebSocketHandler extends TextWebSocketHandler {
//
//    private static final List<WebSocketSession> sessions = new ArrayList<>();
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        sessions.add(session);
//        System.out.println("WebSocket connected: " + session.getId());
//    }
//
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        // You can optionally parse messages from clients
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        sessions.remove(session);
//        System.out.println("WebSocket closed: " + session.getId());
//    }
//
//    public static void broadcast(NotificationPayload payload) {
//
//        try {
//            // Clean up any stale sessions
//            sessions.removeIf(session -> session == null || !session.isOpen());
//            System.out.println("🔔 Broadcasting to " + sessions.size() + " sessions");
//            String json = new ObjectMapper().writeValueAsString(Map.of(
//                    "type", "NOTIFICATION",
//                    "payload", payload
//            ));
//            for (WebSocketSession session : sessions) {
//                if (session != null && session.isOpen()) {
//                    session.sendMessage(new TextMessage(json));
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
