package com.iiil.tutoring.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iiil.tutoring.dto.chat.CreateMessageDTO;
import com.iiil.tutoring.dto.chat.MessageResponseDTO;
import com.iiil.tutoring.service.chat.MessageService;
import org.springframework.lang.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;

/**
 * WebSocket handler for real-time chat messaging
 */
@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    @Autowired
    private MessageService messageService;

    @Autowired
    private WebSocketSessionManager sessionManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Sink for broadcasting messages to all connected clients
    private final Sinks.Many<ChatEvent> chatEventSink = Sinks.many().multicast().onBackpressureBuffer();

    @Override
    public @NonNull Mono<Void> handle(@NonNull WebSocketSession session) {
        // Extract user ID from session attributes or query parameters
        String userId = extractUserId(session);
        
        if (userId == null) {
            return session.close();
        }

        // Register session
        sessionManager.addSession(userId, session);

        // Handle incoming messages
        Mono<Void> input = session.receive()
                .flatMap(message -> handleIncomingMessage(message, userId, session))
                .doOnError(error -> System.err.println("Error handling message: " + error.getMessage()))
                .onErrorResume(error -> Mono.empty())
                .then();

        // Handle outgoing messages (broadcast to this session)
        Mono<Void> output = session.send(
                chatEventSink.asFlux()
                        .filter(event -> shouldReceiveEvent(event, userId))
                        .map(event -> session.textMessage(serializeEvent(event)))
                        .onErrorResume(error -> {
                            System.err.println("Error sending message: " + error.getMessage());
                            return Flux.empty();
                        })
        );

        // Handle session cleanup
        return Mono.zip(input, output)
                .doFinally(signalType -> {
                    sessionManager.removeSession(userId, session);
                    broadcastUserOffline(userId);
                })
                .then();
    }

    /**
     * Handle incoming WebSocket messages
     */
    private Mono<Void> handleIncomingMessage(WebSocketMessage message, String userId, WebSocketSession session) {
        try {
            String payload = message.getPayloadAsText();
            ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);

            switch (chatMessage.getType()) {
                case "SEND_MESSAGE":
                    return handleSendMessage(chatMessage, Long.parseLong(userId));
                    
                case "TYPING_START":
                    return handleTypingIndicator(chatMessage, Long.parseLong(userId), true);
                    
                case "TYPING_STOP":
                    return handleTypingIndicator(chatMessage, Long.parseLong(userId), false);
                    
                case "MARK_READ":
                    return handleMarkAsRead(chatMessage, Long.parseLong(userId));
                    
                case "JOIN_CONVERSATION":
                    return handleJoinConversation(chatMessage, Long.parseLong(userId));
                    
                default:
                    System.err.println("Unknown message type: " + chatMessage.getType());
                    return Mono.empty();
            }
        } catch (Exception e) {
            System.err.println("Error parsing message: " + e.getMessage());
            return Mono.empty();
        }
    }

    /**
     * Handle sending a new message
     */
    private Mono<Void> handleSendMessage(ChatMessage chatMessage, Long senderId) {
        CreateMessageDTO createDto = new CreateMessageDTO();
        createDto.setConversationId(chatMessage.getConversationId());
        createDto.setContenu(chatMessage.getContent());
        createDto.setTypeMessage(chatMessage.getMessageType());

        return messageService.sendMessage(createDto, senderId)
                .doOnNext(messageResponse -> {
                    // Broadcast new message to all participants
                    ChatEvent event = new ChatEvent();
                    event.setType("NEW_MESSAGE");
                    event.setConversationId(chatMessage.getConversationId());
                    event.setMessage(messageResponse);
                    event.setTimestamp(LocalDateTime.now());

                    chatEventSink.tryEmitNext(event);
                })
                .then()
                .onErrorResume(error -> {
                    // Send error back to sender
                    ChatEvent errorEvent = new ChatEvent();
                    errorEvent.setType("ERROR");
                    errorEvent.setError("Failed to send message: " + error.getMessage());
                    errorEvent.setUserId(senderId);
                    
                    chatEventSink.tryEmitNext(errorEvent);
                    return Mono.empty();
                });
    }

    /**
     * Handle typing indicators
     */
    private Mono<Void> handleTypingIndicator(ChatMessage chatMessage, Long userId, boolean isTyping) {
        ChatEvent event = new ChatEvent();
        event.setType(isTyping ? "USER_TYPING" : "USER_STOPPED_TYPING");
        event.setConversationId(chatMessage.getConversationId());
        event.setUserId(userId);
        event.setTimestamp(LocalDateTime.now());

        chatEventSink.tryEmitNext(event);
        return Mono.empty();
    }

    /**
     * Handle marking messages as read
     */
    private Mono<Void> handleMarkAsRead(ChatMessage chatMessage, Long userId) {
        return messageService.markAsRead(chatMessage.getConversationId(), userId)
                .doOnNext(result -> {
                    ChatEvent event = new ChatEvent();
                    event.setType("MESSAGES_READ");
                    event.setConversationId(chatMessage.getConversationId());
                    event.setUserId(userId);
                    event.setTimestamp(LocalDateTime.now());

                    chatEventSink.tryEmitNext(event);
                })
                .then()
                .onErrorResume(error -> Mono.empty());
    }

    /**
     * Handle joining a conversation
     */
    private Mono<Void> handleJoinConversation(ChatMessage chatMessage, Long userId) {
        // Update user's presence in conversation
        ChatEvent event = new ChatEvent();
        event.setType("USER_JOINED");
        event.setConversationId(chatMessage.getConversationId());
        event.setUserId(userId);
        event.setTimestamp(LocalDateTime.now());

        chatEventSink.tryEmitNext(event);
        return Mono.empty();
    }

    /**
     * Broadcast that user went offline
     */
    private void broadcastUserOffline(String userId) {
        ChatEvent event = new ChatEvent();
        event.setType("USER_OFFLINE");
        event.setUserId(Long.parseLong(userId));
        event.setTimestamp(LocalDateTime.now());

        chatEventSink.tryEmitNext(event);
    }

    /**
     * Determine if a user should receive a specific event
     */
    private boolean shouldReceiveEvent(ChatEvent event, String userId) {
        // Send error events only to the specific user
        if ("ERROR".equals(event.getType())) {
            return event.getUserId() != null && event.getUserId().equals(Long.parseLong(userId));
        }

        // For conversation-specific events, check if user is participant
        if (event.getConversationId() != null) {
            return sessionManager.isUserInConversation(Long.parseLong(userId), event.getConversationId());
        }

        // Send global events to all users
        return true;
    }

    /**
     * Extract user ID from WebSocket session
     */
    private String extractUserId(WebSocketSession session) {
        // Try to get from query parameters first
        String userId = session.getHandshakeInfo().getUri().getQuery();
        if (userId != null && userId.startsWith("userId=")) {
            return userId.substring(7); // Remove "userId=" prefix
        }

        // Try to get from headers
        return session.getHandshakeInfo().getHeaders().getFirst("X-User-Id");
    }

    /**
     * Serialize chat event to JSON string
     */
    private String serializeEvent(ChatEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            System.err.println("Error serializing event: " + e.getMessage());
            return "{\"type\":\"ERROR\",\"error\":\"Serialization failed\"}";
        }
    }

    // Inner classes for WebSocket message handling

    /**
     * Incoming chat message structure
     */
    public static class ChatMessage {
        private String type;
        private Long conversationId;
        private String content;
        private com.iiil.tutoring.enums.MessageType messageType;

        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public Long getConversationId() { return conversationId; }
        public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public com.iiil.tutoring.enums.MessageType getMessageType() { return messageType; }
        public void setMessageType(com.iiil.tutoring.enums.MessageType messageType) { this.messageType = messageType; }
    }

    /**
     * Outgoing chat event structure
     */
    public static class ChatEvent {
        private String type;
        private Long conversationId;
        private Long userId;
        private MessageResponseDTO message;
        private String error;
        private LocalDateTime timestamp;

        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public Long getConversationId() { return conversationId; }
        public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public MessageResponseDTO getMessage() { return message; }
        public void setMessage(MessageResponseDTO message) { this.message = message; }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}