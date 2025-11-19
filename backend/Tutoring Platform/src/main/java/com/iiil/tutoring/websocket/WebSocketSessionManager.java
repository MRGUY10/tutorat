package com.iiil.tutoring.websocket;

import com.iiil.tutoring.service.chat.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Manages WebSocket sessions for real-time communication
 */
@Component
public class WebSocketSessionManager {

    @Autowired
    private ConversationService conversationService;

    // Map of user ID to their active WebSocket sessions
    private final ConcurrentHashMap<String, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    
    // Map of conversation ID to users in that conversation
    private final ConcurrentHashMap<Long, Set<Long>> conversationParticipants = new ConcurrentHashMap<>();
    
    // Map of user ID to their active conversations
    private final ConcurrentHashMap<Long, Set<Long>> userConversations = new ConcurrentHashMap<>();

    /**
     * Add a WebSocket session for a user
     */
    public void addSession(String userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
        System.out.println("User " + userId + " connected. Total sessions: " + userSessions.get(userId).size());
        
        // Load user's conversations
        loadUserConversations(Long.parseLong(userId));
    }

    /**
     * Remove a WebSocket session for a user
     */
    public void removeSession(String userId, WebSocketSession session) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
                // Clean up user's conversation data when they go offline
                cleanupUserData(Long.parseLong(userId));
            }
        }
        System.out.println("User " + userId + " disconnected");
    }

    /**
     * Get all active sessions for a user
     */
    public Set<WebSocketSession> getUserSessions(String userId) {
        return userSessions.getOrDefault(userId, Set.of());
    }

    /**
     * Check if user is online (has active sessions)
     */
    public boolean isUserOnline(String userId) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    /**
     * Get all online users
     */
    public Set<String> getOnlineUsers() {
        return userSessions.keySet();
    }

    /**
     * Check if user is participant in conversation
     */
    public boolean isUserInConversation(Long userId, Long conversationId) {
        Set<Long> userConvs = userConversations.get(userId);
        return userConvs != null && userConvs.contains(conversationId);
    }

    /**
     * Add user to conversation participants
     */
    public void addUserToConversation(Long userId, Long conversationId) {
        conversationParticipants.computeIfAbsent(conversationId, k -> new CopyOnWriteArraySet<>()).add(userId);
        userConversations.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(conversationId);
    }

    /**
     * Remove user from conversation participants
     */
    public void removeUserFromConversation(Long userId, Long conversationId) {
        Set<Long> participants = conversationParticipants.get(conversationId);
        if (participants != null) {
            participants.remove(userId);
            if (participants.isEmpty()) {
                conversationParticipants.remove(conversationId);
            }
        }
        
        Set<Long> userConvs = userConversations.get(userId);
        if (userConvs != null) {
            userConvs.remove(conversationId);
            if (userConvs.isEmpty()) {
                userConversations.remove(userId);
            }
        }
    }

    /**
     * Get all participants in a conversation
     */
    public Set<Long> getConversationParticipants(Long conversationId) {
        return conversationParticipants.getOrDefault(conversationId, Set.of());
    }

    /**
     * Get conversation statistics
     */
    public SessionStats getSessionStats() {
        return new SessionStats(
            userSessions.size(),
            userSessions.values().stream().mapToInt(Set::size).sum(),
            conversationParticipants.size()
        );
    }

    /**
     * Load user's conversations from database
     */
    private void loadUserConversations(Long userId) {
        conversationService.getUserConversations(userId, false)
                .subscribe(conversation -> {
                    addUserToConversation(userId, conversation.getId());
                });
    }

    /**
     * Clean up user's conversation data when they go offline
     */
    private void cleanupUserData(Long userId) {
        Set<Long> userConvs = userConversations.remove(userId);
        if (userConvs != null) {
            userConvs.forEach(conversationId -> {
                Set<Long> participants = conversationParticipants.get(conversationId);
                if (participants != null) {
                    participants.remove(userId);
                    if (participants.isEmpty()) {
                        conversationParticipants.remove(conversationId);
                    }
                }
            });
        }
    }

    /**
     * Session statistics
     */
    public static class SessionStats {
        private final int onlineUsers;
        private final int totalSessions;
        private final int activeConversations;

        public SessionStats(int onlineUsers, int totalSessions, int activeConversations) {
            this.onlineUsers = onlineUsers;
            this.totalSessions = totalSessions;
            this.activeConversations = activeConversations;
        }

        public int getOnlineUsers() { return onlineUsers; }
        public int getTotalSessions() { return totalSessions; }
        public int getActiveConversations() { return activeConversations; }

        @Override
        public String toString() {
            return "SessionStats{" +
                    "onlineUsers=" + onlineUsers +
                    ", totalSessions=" + totalSessions +
                    ", activeConversations=" + activeConversations +
                    '}';
        }
    }
}