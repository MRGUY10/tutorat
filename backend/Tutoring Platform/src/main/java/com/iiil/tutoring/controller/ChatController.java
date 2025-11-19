package com.iiil.tutoring.controller;

import com.iiil.tutoring.dto.chat.*;
import com.iiil.tutoring.service.chat.ConversationService;
import com.iiil.tutoring.service.chat.MessageService;
import com.iiil.tutoring.websocket.WebSocketSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

/**
 * REST Controller for chat operations
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private WebSocketSessionManager sessionManager;

    // ===== CONVERSATION ENDPOINTS =====

    /**
     * Create a new conversation
     */
    @PostMapping("/conversations")
    public Mono<ResponseEntity<ConversationDTO>> createConversation(
            @Valid @RequestBody CreateConversationDTO createDto,
            @RequestHeader("X-User-Id") Long userId) {
        
        log.info("POST /api/chat/conversations - UserId: {}, Subject: '{}', ParticipantIds: {}", 
                userId, createDto.getSujet(), createDto.getParticipantIds());
        
        return conversationService.createConversation(createDto, userId)
                .map(conversation -> {
                    log.info("Conversation created successfully - ID: {}", conversation.getId());
                    return ResponseEntity.ok(conversation);
                })
                .onErrorResume(IllegalArgumentException.class, error -> {
                    log.error("Validation error creating conversation: {}", error.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                })
                .onErrorResume(error -> {
                    log.error("Unexpected error creating conversation: {}", error.getMessage(), error);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    /**
     * Get user's conversations
     */
    @GetMapping("/conversations")
    public Flux<ConversationDTO> getUserConversations(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "false") boolean includeArchived) {
        
        return conversationService.getUserConversations(userId, includeArchived);
    }

    /**
     * Get specific conversation
     */
    @GetMapping("/conversations/{conversationId}")
    public Mono<ResponseEntity<ConversationDTO>> getConversation(
            @PathVariable Long conversationId,
            @RequestHeader("X-User-Id") Long userId) {
        
        return conversationService.getConversation(conversationId, userId)
                .map(conversation -> ResponseEntity.ok(conversation))
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    /**
     * Update conversation
     */
    @PutMapping("/conversations/{conversationId}")
    public Mono<ResponseEntity<ConversationDTO>> updateConversation(
            @PathVariable Long conversationId,
            @Valid @RequestBody ConversationUpdateDTO updateDto,
            @RequestHeader("X-User-Id") Long userId) {
        
        updateDto.setConversationId(conversationId);
        return conversationService.updateConversation(updateDto, userId)
                .map(conversation -> ResponseEntity.ok(conversation))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Archive/unarchive conversation
     */
    @PutMapping("/conversations/{conversationId}/archive")
    public Mono<ResponseEntity<Void>> archiveConversation(
            @PathVariable Long conversationId,
            @RequestParam boolean archived,
            @RequestHeader("X-User-Id") Long userId) {
        
        return conversationService.archiveConversation(conversationId, userId, archived)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Leave conversation
     */
    @DeleteMapping("/conversations/{conversationId}/leave")
    public Mono<ResponseEntity<Void>> leaveConversation(
            @PathVariable Long conversationId,
            @RequestHeader("X-User-Id") Long userId) {
        
        return conversationService.leaveConversation(conversationId, userId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Get conversation participants
     */
    @GetMapping("/conversations/{conversationId}/participants")
    public Flux<ConversationParticipantDTO> getConversationParticipants(
            @PathVariable Long conversationId,
            @RequestHeader("X-User-Id") Long userId) {
        
        return conversationService.getConversationParticipants(conversationId, userId);
    }

    /**
     * Add participant to conversation
     */
    @PostMapping("/conversations/{conversationId}/participants")
    public Mono<ResponseEntity<ConversationParticipantDTO>> addParticipant(
            @PathVariable Long conversationId,
            @RequestParam Long userIdToAdd,
            @RequestHeader("X-User-Id") Long userId) {
        
        return conversationService.addParticipant(conversationId, userIdToAdd, userId)
                .map(participant -> ResponseEntity.ok(participant))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Remove participant from conversation
     */
    @DeleteMapping("/conversations/{conversationId}/participants/{userIdToRemove}")
    public Mono<ResponseEntity<Void>> removeParticipant(
            @PathVariable Long conversationId,
            @PathVariable Long userIdToRemove,
            @RequestHeader("X-User-Id") Long userId) {
        
        return conversationService.removeParticipant(conversationId, userIdToRemove, userId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Search conversations
     */
    @GetMapping("/conversations/search")
    public Flux<ConversationDTO> searchConversations(
            @RequestParam String q,
            @RequestHeader("X-User-Id") Long userId) {
        
        return conversationService.searchConversations(userId, q);
    }

    /**
     * Get conversations with unread messages
     */
    @GetMapping("/conversations/unread")
    public Flux<ConversationDTO> getUnreadConversations(
            @RequestHeader("X-User-Id") Long userId) {
        
        return conversationService.getConversationsWithUnreadMessages(userId);
    }

    /**
     * Get conversation statistics
     */
    @GetMapping("/conversations/stats")
    public Mono<ConversationService.ConversationStatsDTO> getConversationStats(
            @RequestHeader("X-User-Id") Long userId) {
        
        return conversationService.getConversationStatistics(userId);
    }

    // ===== MESSAGE ENDPOINTS =====

    /**
     * Send a message (REST endpoint - WebSocket is preferred for real-time)
     */
    @PostMapping("/messages")
    public Mono<ResponseEntity<MessageResponseDTO>> sendMessage(
            @Valid @RequestBody CreateMessageDTO createDto,
            @RequestHeader("X-User-Id") Long userId) {
        
        return messageService.sendMessage(createDto, userId)
                .map(message -> ResponseEntity.ok(message))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Get messages in conversation
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public Flux<MessageResponseDTO> getConversationMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestHeader("X-User-Id") Long userId) {
        
        return messageService.getConversationMessages(conversationId, userId, page, size);
    }

    /**
     * Get recent messages in conversation
     */
    @GetMapping("/conversations/{conversationId}/messages/recent")
    public Flux<MessageResponseDTO> getRecentMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestHeader("X-User-Id") Long userId) {
        
        return messageService.getRecentMessages(conversationId, userId, limit);
    }

    /**
     * Mark messages as read
     */
    @PutMapping("/conversations/{conversationId}/messages/read")
    public Mono<ResponseEntity<Void>> markMessagesAsRead(
            @PathVariable Long conversationId,
            @RequestHeader("X-User-Id") Long userId) {
        
        return messageService.markAsRead(conversationId, userId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Get unread message count
     */
    @GetMapping("/conversations/{conversationId}/messages/unread/count")
    public Mono<Long> getUnreadMessageCount(
            @PathVariable Long conversationId,
            @RequestHeader("X-User-Id") Long userId) {
        
        return messageService.getUnreadMessageCount(conversationId, userId);
    }

    /**
     * Search messages
     */
    @GetMapping("/messages/search")
    public Flux<MessageResponseDTO> searchMessages(
            @RequestParam String q,
            @RequestParam(defaultValue = "50") int limit,
            @RequestHeader("X-User-Id") Long userId) {
        
        return messageService.searchMessages(userId, q, limit);
    }

    /**
     * Get message by ID
     */
    @GetMapping("/messages/{messageId}")
    public Mono<ResponseEntity<MessageResponseDTO>> getMessage(
            @PathVariable Long messageId,
            @RequestHeader("X-User-Id") Long userId) {
        
        return messageService.getMessage(messageId, userId)
                .map(message -> ResponseEntity.ok(message))
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    /**
     * Delete message
     */
    @DeleteMapping("/messages/{messageId}")
    public Mono<ResponseEntity<Void>> deleteMessage(
            @PathVariable Long messageId,
            @RequestHeader("X-User-Id") Long userId) {
        
        return messageService.deleteMessage(messageId, userId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    // ===== PRESENCE & STATUS ENDPOINTS =====

    /**
     * Get online users
     */
    @GetMapping("/presence/online")
    public Mono<ResponseEntity<Object>> getOnlineUsers() {
        var onlineUsers = sessionManager.getOnlineUsers();
        return Mono.just(ResponseEntity.ok((Object) onlineUsers));
    }

    /**
     * Check if user is online
     */
    @GetMapping("/presence/online/{userId}")
    public Mono<ResponseEntity<Object>> isUserOnline(@PathVariable String userId) {
        boolean isOnline = sessionManager.isUserOnline(userId);
        return Mono.just(ResponseEntity.ok((Object) Map.of("userId", userId, "online", isOnline)));
    }

    /**
     * Get WebSocket session statistics
     */
    @GetMapping("/presence/stats")
    public Mono<ResponseEntity<WebSocketSessionManager.SessionStats>> getSessionStats() {
        var stats = sessionManager.getSessionStats();
        return Mono.just(ResponseEntity.ok(stats));
    }

    // ===== ERROR HANDLING =====

    /**
     * Handle validation errors
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<Object>> handleValidationError(IllegalArgumentException e) {
        return Mono.just(ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage())));
    }

    /**
     * Handle general errors
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Object>> handleGeneralError(Exception e) {
        return Mono.just(ResponseEntity.internalServerError()
                .body(Map.of("error", "Une erreur interne s'est produite")));
    }

    // Helper for creating error response maps
    private static class Map {
        public static java.util.Map<String, Object> of(String key, Object value) {
            return java.util.Collections.singletonMap(key, value);
        }
        
        public static java.util.Map<String, Object> of(String key1, Object value1, String key2, Object value2) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put(key1, value1);
            map.put(key2, value2);
            return map;
        }
    }
}