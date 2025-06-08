//package com.iotmining.services.notification.configuration;
//
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.http.server.ServerHttpResponse;
//import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
//
//import java.util.List;
//
//public class SubprotocolEchoingHandshakeHandler extends DefaultHandshakeHandler {
//
//    protected String determineProtocol(List<String> requestedProtocols, ServerHttpRequest request, ServerHttpResponse response) {
//        if (requestedProtocols != null && !requestedProtocols.isEmpty()) {
//            String protocol = requestedProtocols.get(0);
//            response.getHeaders().set("Sec-WebSocket-Protocol", protocol); // echo back!
//            return protocol;
//        }
//        return null;
//    }
//}
