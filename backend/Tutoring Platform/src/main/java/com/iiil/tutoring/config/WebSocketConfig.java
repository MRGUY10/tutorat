package com.iiil.tutoring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket configuration for real-time messaging
 */
@Configuration
public class WebSocketConfig {

    /**
     * Configure WebSocket handler mapping for chat endpoints
     */
    @Bean
    public HandlerMapping webSocketHandlerMapping(WebSocketHandler chatWebSocketHandler) {
        Map<String, WebSocketHandler> map = new HashMap<>();
        
        // Main chat endpoint for sending/receiving messages
        map.put("/ws/chat", chatWebSocketHandler);
        
        // Typing indicators endpoint
        map.put("/ws/typing", chatWebSocketHandler);
        
        // Online presence endpoint
        map.put("/ws/presence", chatWebSocketHandler);

        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setUrlMap(map);
        handlerMapping.setOrder(1);
        
        return handlerMapping;
    }

    /**
     * WebSocket handler adapter for reactive WebSocket support
     */
    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}