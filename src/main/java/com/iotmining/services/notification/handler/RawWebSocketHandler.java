package com.iotmining.services.notification.handler;

import com.iotmining.services.notification.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RawWebSocketHandler extends TextWebSocketHandler {

    // userId → WebSocketSession
    private final Map<UUID, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    // sessionId → userId (for cleanup)
    private final Map<String, UUID> sessionIdToUserId = new ConcurrentHashMap<>();

    public void sendToUser(UUID userId, String message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                System.err.println("❌ Error sending to user " + userId + ": " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ No open session found for userId = " + userId);
        }
    }

    public void sendToAll(String message) {
        for (WebSocketSession session : userSessions.values()) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    System.err.println("❌ Error sending message: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String token = getTokenFromQuery(session.getUri());

        if (token == null) {
            System.err.println("❌ Missing token in query params, closing session: " + session.getId());
            closeSession(session, CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        try {
            Jws<Claims> claims = JwtUtil.validateToken(token);
            Claims body = claims.getBody();
//            UUID userId = body.get("userId", UUID.class);
            UUID userId = UUID.fromString(body.get("userId", String.class));
            String username = body.getSubject(); // Optional logging

            if (userId == null) {
                System.err.println("❌ Missing userId claim in token");
                closeSession(session, CloseStatus.NOT_ACCEPTABLE);
                return;
            }

            userSessions.put(userId, session);
            sessionIdToUserId.put(session.getId(), userId);

            System.out.println("🟢 Authenticated user: " + username + " (userId=" + userId + ") sessionId = " + session.getId());

        } catch (JwtException e) {
            System.err.println("❌ Invalid token, closing session " + session.getId() + ": " + e.getMessage());
            closeSession(session, CloseStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        UUID userId = sessionIdToUserId.remove(session.getId());
        if (userId != null) {
            userSessions.remove(userId);
            System.out.println("🔴 WebSocket closed: userId = " + userId + ", sessionId = " + session.getId());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.err.println("⚠️ Transport error on session " + session.getId() + ": " + exception.getMessage());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        System.out.println("📨 Received message from " + session.getId() + ": " + payload);

        try {
            session.sendMessage(new TextMessage("Echo: " + payload));
        } catch (IOException e) {
            System.err.println("❌ Failed to send echo: " + e.getMessage());
        }
    }

    private String getTokenFromQuery(URI uri) {
        if (uri == null || uri.getQuery() == null) return null;
        for (String param : uri.getQuery().split("&")) {
            if (param.startsWith("token=")) {
                return param.substring("token=".length());
            }
        }
        return null;
    }

    private void closeSession(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (IOException e) {
            System.err.println("❌ Error closing session: " + e.getMessage());
        }
    }
}



//package com.iotmining.services.notification.handler;
//
//import jakarta.websocket.OnError;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.*;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.io.IOException;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Component
//public class RawWebSocketHandler extends TextWebSocketHandler {
//
//    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
//
//    public void sendToAll(String message) {
//        for (WebSocketSession session : sessions.values()) {
//            if (session.isOpen()) {
//                try {
//                    session.sendMessage(new TextMessage(message));
//                } catch (IOException e) {
//                    System.err.println("❌ Error sending message: " + e.getMessage());
//                }
//            }
//        }
//    }
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) {
//        sessions.put(session.getId(), session);
//        System.out.println("🟢 WebSocket connected: sessionId = " + session.getId());
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
//        sessions.remove(session.getId());
//        System.out.println("🔴 WebSocket closed: sessionId = " + session.getId());
//    }
//
//    @Override
//    public void handleTransportError(WebSocketSession session, Throwable exception) {
//        System.err.println("⚠️ Transport error on session " + session.getId() +
//                ": " + (exception != null ? exception.getMessage() : "null"));
//
////        try {
//////            session.close(CloseStatus.SERVER_ERROR);
////        } catch (IOException e) {
////            System.err.println("❌ Failed to close session on error: " + e.getMessage());
////        }
//    }
//    @OnError
//    public void onError(WebSocketSession session, Throwable throwable) {
//        System.err.println("⚠️ Error in session " + session.getId() + ": " + throwable.getMessage());
//        throwable.printStackTrace();
//    }
//
//
//    @Override
//    public void handleTextMessage(WebSocketSession session, TextMessage message) {
//        String payload = message.getPayload();
//        System.out.println("📨 Received message from " + session.getId() + ": " + payload);
//
//        try {
//            session.sendMessage(new TextMessage("Echo: " + payload));
//        } catch (IOException e) {
//            System.err.println("❌ Failed to send echo: " + e.getMessage());
//        }
//    }
//
//}


//package com.iotmining.services.notification.handler;
//
//import com.iotmining.services.notification.configuration.JwtConfig;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.JwtParser;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.http.HttpHeaders;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.*;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.io.IOException;
//import java.security.Key;
//import java.util.Base64;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Component
//public class RawWebSocketHandler extends TextWebSocketHandler {
//
//    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
//    private final JwtConfig jwtConfig;
//
//    public RawWebSocketHandler(JwtConfig jwtConfig) {
//        this.jwtConfig = jwtConfig;
//    }
//
//    public void sendToUser(Long userId, String message) {
//        WebSocketSession session = userSessions.get(userId);
//        if (session != null && session.isOpen()) {
//            try {
//                session.sendMessage(new TextMessage(message));
//            } catch (IOException e) {
//                System.err.println("❌ Error sending WebSocket message: " + e.getMessage());
//            }
//        }
//    }
//
//    private Long extractUserIdFromToken(WebSocketSession session) {
//        try {
//            HttpHeaders headers = session.getHandshakeHeaders();
//            String token = headers.getFirst("Sec-WebSocket-Protocol");
//
//            if (token != null && !token.isBlank()) {
//                return parseUserIdFromToken(token.trim());
//            } else {
//                System.err.println("❌ Missing token in Sec-WebSocket-Protocol header");
//            }
//        } catch (Exception e) {
//            System.err.println("❌ Token extraction failed: " + e.getMessage());
//        }
//        return null;
//    }
//
//    private Long parseUserIdFromToken(String token) {
//        try {
//            // ✅ Base64 decode the secret before use
//            byte[] decodedSecret = Base64.getDecoder().decode(jwtConfig.getSecret());
//            Key key = Keys.hmacShaKeyFor(decodedSecret);
//
//            JwtParser parser = Jwts.parserBuilder().setSigningKey(key).build();
//            Claims claims = parser.parseClaimsJws(token).getBody();
//
//            Object uid = claims.get("userId");
//            if (uid instanceof Integer) return ((Integer) uid).longValue();
//            if (uid instanceof Long) return (Long) uid;
//            if (uid instanceof String && ((String) uid).matches("\\d+")) return Long.parseLong((String) uid);
//
//        } catch (Exception e) {
//            System.err.println("❌ JWT parsing failed: " + e.getMessage());
//        }
//        return null;
//    }
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) {
////        Long userId = extractUserIdFromToken(session);
//        Long userId = 124412L;
//        if (userId != null) {
//            userSessions.put(userId, session);
//            System.out.println("🟢 WebSocket connected: userId = " + userId);
//        } else {
//            try {
//                session.close(CloseStatus.BAD_DATA);
//                System.err.println("❌ Invalid token, session closed.");
//            } catch (IOException e) {
//                System.err.println("❌ Failed to close session: " + e.getMessage());
//            }
//        }
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
//        userSessions.values().removeIf(sess -> sess.getId().equals(session.getId()));
//        System.out.println("🔴 WebSocket closed: sessionId = " + session.getId());
//    }
//}



//package com.iotmining.services.notification.handler;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.JwtParser;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.http.HttpHeaders;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.*;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.security.Key;
//import java.util.Map;
//import java.util.Objects;
//import java.util.concurrent.ConcurrentHashMap;
//
//import static javax.crypto.Cipher.SECRET_KEY;
//
//@Component
//public class RawWebSocketHandler extends TextWebSocketHandler {
//
//    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
//    private static final String SECRET_KEY = "your-256-bit-secret-your-256-bit-secret"; // Replace with actual key
//
//    public void sendToUser(Long userId, String message) {
//        WebSocketSession session = userSessions.get(userId);
//        if (session != null && session.isOpen()) {
//            try {
//                session.sendMessage(new TextMessage(message));
//            } catch (IOException e) {
//                System.err.println("Error sending WebSocket message: " + e.getMessage());
//            }
//        }
//    }
//
//    private Long extractUserIdFromToken(WebSocketSession session) {
//        try {
//            HttpHeaders headers = session.getHandshakeHeaders();
//            // Expecting: Sec-WebSocket-Protocol: <token>
//            String protocolHeader = headers.getFirst("Sec-WebSocket-Protocol");
//
//            System.out.println("🔐 Sec-WebSocket-Protocol received = " + protocolHeader);
//
//            if (protocolHeader != null && !protocolHeader.isBlank()) {
//                String token = protocolHeader.trim();
//               // return parseUserIdFromToken(token);
//            } else {
//                System.err.println("❌ Missing Sec-WebSocket-Protocol format.");
//            }
//        } catch (Exception e) {
//            System.err.println("❌ Token extraction failed: " + e.getMessage());
//        }
//        return null;
//    }
//
//    private Long parseUserIdFromToken(String token) {
//        try {
//            Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
//            JwtParser parser = Jwts.parserBuilder().setSigningKey(key).build();
//            Claims claims = parser.parseClaimsJws(token).getBody();
//
//            // You can extract based on how you encode the token
//            String userIdStr = claims.getSubject(); // often "sub" is username or userId
//            if (userIdStr != null && userIdStr.matches("\\d+")) {
//                return Long.parseLong(userIdStr);
//            }
//
//            // If your claim is stored with key "userId"
//            Object uid = claims.get("userId");
//            if (uid instanceof Integer) return ((Integer) uid).longValue();
//            if (uid instanceof Long) return (Long) uid;
//            if (uid instanceof String && ((String) uid).matches("\\d+")) {
//                return Long.parseLong((String) uid);
//            }
//
//        } catch (Exception e) {
//            System.err.println("❌ JWT parsing failed: " + e.getMessage());
//        }
//        return null;
//    }
//
////    @Override
////    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
//////        Long userId = extractUserIdFromToken(session);
////        Long userId = 10111235L;
//////        if (userId != null) {
////            userSessions.put(userId, session);
////            System.out.println("🟢 WebSocket connected: userId = " + userId);
//////        } else {
//////            System.err.println("❌ Invalid or missing token. Closing WebSocket.");
//////            session.close(CloseStatus.BAD_DATA);
//////        }
////    }
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        String token = session.getHandshakeHeaders().getFirst("Sec-WebSocket-Protocol");
//
//        if (token != null && !token.isBlank()) {
//            Long userId = parseUserIdFromToken(token);
//            if (userId != null) {
//                userSessions.put(userId, session);
//                System.out.println("🟢 WebSocket connected: userId = " + userId);
//            } else {
//                System.err.println("❌ Invalid token. Closing session.");
//                session.close(CloseStatus.BAD_DATA);
//            }
//        } else {
//            System.err.println("⚠️ No token provided. Fallback to anonymous session: " + session.getId());
//            userSessions.put(-1L, session); // or some anonymous user bucket
//        }
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, @NotNull CloseStatus status) {
////        userSessions.values().removeIf(sess -> sess.getId().equals(session.getId()));
//        System.out.println("🔴 WebSocket closed: sessionId = " + session.getId());
//    }
//
////    @Override
////    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
////        System.err.println("❌ WebSocket transport error: sessionId = " + session.getId() + " | error = " + exception.getMessage());
////    }
//
//}
