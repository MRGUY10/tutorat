package com.iiil.tutoring.repository;

import com.iiil.tutoring.entity.Conversation;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Repository for conversation operations
 */
@Repository
public interface ConversationRepository extends ReactiveCrudRepository<Conversation, Long> {

    /**
     * Find all conversations for a specific user (participant)
     */
    @Query("""
            SELECT c.* FROM conversations c
            INNER JOIN conversation_participants cp ON c.id = cp.conversation_id
            WHERE cp.user_id = :userId
            AND c.archivee = false
            ORDER BY c.date_creation DESC
            """)
    Flux<Conversation> findByParticipantUserId(Long userId);

    /**
     * Find all conversations (including archived) for a specific user
     */
    @Query("""
            SELECT c.* FROM conversations c
            INNER JOIN conversation_participants cp ON c.id = cp.conversation_id
            WHERE cp.user_id = :userId
            ORDER BY c.date_creation DESC
            """)
    Flux<Conversation> findAllByParticipantUserId(Long userId);

    /**
     * Find archived conversations for a specific user
     */
    @Query("""
            SELECT c.* FROM conversations c
            INNER JOIN conversation_participants cp ON c.id = cp.conversation_id
            WHERE cp.user_id = :userId
            AND c.archivee = true
            ORDER BY c.date_creation DESC
            """)
    Flux<Conversation> findArchivedByParticipantUserId(Long userId);

    /**
     * Find conversation by session ID
     */
    @Query("""
            SELECT c.* FROM conversations c
            WHERE c.session_id = :sessionId
            LIMIT 1
            """)
    Mono<Conversation> findBySessionId(Long sessionId);

    /**
     * Find conversations between two specific users
     */
    @Query("""
            SELECT DISTINCT c.* FROM conversations c
            INNER JOIN conversation_participants cp1 ON c.id = cp1.conversation_id
            INNER JOIN conversation_participants cp2 ON c.id = cp2.conversation_id
            WHERE cp1.user_id = :userId1
            AND cp2.user_id = :userId2
            AND c.session_id IS NULL
            ORDER BY c.date_creation DESC
            """)
    Flux<Conversation> findBetweenUsers(Long userId1, Long userId2);

    /**
     * Count unread conversations for a user
     */
    @Query("""
            SELECT COUNT(DISTINCT c.id) FROM conversations c
            INNER JOIN conversation_participants cp ON c.id = cp.conversation_id
            INNER JOIN messages m ON c.id = m.conversation_id
            WHERE cp.user_id = :userId
            AND c.archivee = false
            AND m.expediteur_id != :userId
            AND m.lu = false
                        """)
    Mono<Long> countUnreadConversations(Long userId);

    /**
     * Find conversations with unread messages for a user
     */
    @Query("""
            SELECT DISTINCT c.* FROM conversations c
            INNER JOIN conversation_participants cp ON c.id = cp.conversation_id
            INNER JOIN messages m ON c.id = m.conversation_id
            WHERE cp.user_id = :userId
            AND c.archivee = false
            AND m.expediteur_id != :userId
            AND m.lu = false
                        ORDER BY m.date_envoi DESC
            """)
    Flux<Conversation> findConversationsWithUnreadMessages(Long userId);

    /**
     * Get conversation statistics for a user
     */
    @Query("""
            SELECT 
                COUNT(DISTINCT c.id) as total_conversations,
                COUNT(DISTINCT CASE WHEN c.archivee = false THEN c.id END) as active_conversations,
                COUNT(DISTINCT CASE WHEN c.archivee = true THEN c.id END) as archived_conversations
            FROM conversations c
            INNER JOIN conversation_participants cp ON c.id = cp.conversation_id
            WHERE cp.user_id = :userId
            """)
    Mono<ConversationStats> getConversationStats(Long userId);

    /**
     * Find conversations by subject (search)
     */
    @Query("""
            SELECT DISTINCT c.* FROM conversations c
            INNER JOIN conversation_participants cp ON c.id = cp.conversation_id
            WHERE cp.user_id = :userId
            AND LOWER(c.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            ORDER BY c.date_creation DESC
            """)
    Flux<Conversation> searchBySubject(Long userId, String searchTerm);

    /**
     * Find recent conversations with activity
     */
    @Query("""
            SELECT DISTINCT c.* FROM conversations c
            INNER JOIN conversation_participants cp ON c.id = cp.conversation_id
            LEFT JOIN messages m ON c.id = m.conversation_id
            WHERE cp.user_id = :userId
            AND c.archivee = false
            AND (m.date_envoi >= :since OR c.date_creation >= :since)
            ORDER BY COALESCE(MAX(m.date_envoi), c.date_creation) DESC
            LIMIT :limit
            """)
    Flux<Conversation> findRecentConversations(Long userId, LocalDateTime since, int limit);

    /**
     * Check if user is participant in conversation
     */
    @Query("""
            SELECT COUNT(*) > 0 FROM conversation_participants cp
            WHERE cp.conversation_id = :conversationId
            AND cp.user_id = :userId
            """)
    Mono<Boolean> isUserParticipant(Long conversationId, Long userId);

    /**
     * Count total participants in conversation
     */
    @Query("""
            SELECT COUNT(*) FROM conversation_participants cp
            WHERE cp.conversation_id = :conversationId
            """)
    Mono<Long> countParticipants(Long conversationId);

    /**
     * Find group conversations (more than 2 participants)
     */
    @Query("""
            SELECT c.* FROM conversations c
            INNER JOIN conversation_participants cp ON c.id = cp.conversation_id
            WHERE cp.user_id = :userId
            AND (
                SELECT COUNT(*) FROM conversation_participants cp2 
                WHERE cp2.conversation_id = c.id
            ) > 2
            ORDER BY c.date_creation DESC
            """)
    Flux<Conversation> findGroupConversations(Long userId);

    /**
     * Update conversation archive status
     */
    @Query("""
            UPDATE conversations 
            SET archivee = :archived 
            WHERE id = :conversationId
            """)
    Mono<Void> updateArchiveStatus(Long conversationId, boolean archived);

    /**
     * Update conversation subject
     */
    @Query("""
            UPDATE conversations 
            SET nom = :newSubject 
            WHERE id = :conversationId
            """)
    Mono<Void> updateSubject(Long conversationId, String newSubject);

    /**
     * Get last message date for conversation
     */
    @Query("""
            SELECT MAX(m.date_envoi) FROM messages m
            WHERE m.conversation_id = :conversationId
            """)
    Mono<LocalDateTime> getLastMessageDate(Long conversationId);

    /**
     * Count messages in conversation
     */
    @Query("""
            SELECT COUNT(*) FROM messages m
            WHERE m.conversation_id = :conversationId
            """)
    Mono<Long> countMessages(Long conversationId);

    // DTO class for conversation statistics
    class ConversationStats {
        private final Long totalConversations;
        private final Long activeConversations;
        private final Long archivedConversations;

        public ConversationStats(Long totalConversations, Long activeConversations, Long archivedConversations) {
            this.totalConversations = totalConversations;
            this.activeConversations = activeConversations;
            this.archivedConversations = archivedConversations;
        }

        public Long getTotalConversations() { return totalConversations; }
        public Long getActiveConversations() { return activeConversations; }
        public Long getArchivedConversations() { return archivedConversations; }
    }
}
