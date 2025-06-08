//package com.iotmining.services.notification.configuration;
//
//import com.iotmining.services.notification.handler.RawWebSocketHandler;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.socket.config.annotation.EnableWebSocket;
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
//
//@Configuration
//@EnableWebSocket
//public class WebSocketRawConfig implements WebSocketConfigurer {
//
//    @Autowired
//    private RawWebSocketHandler webSocketHandler;
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry.addHandler(webSocketHandler, "/ws")
//                .setAllowedOriginPatterns("*");
//    }
//
//
//}
