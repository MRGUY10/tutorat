package com.iiil.tutoring.repository;

import com.iiil.tutoring.entity.ConversationParticipant;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository for conversation participant operations
 */
@Repository
public interface ConversationParticipantRepository extends ReactiveCrudRepository<ConversationParticipant, Long> {

    /**
     * Find all participants in a conversation
     */
    @Query("""
            SELECT cp.* FROM conversation_participants cp
            WHERE cp.conversation_id = :conversationId
            ORDER BY cp.date_adhesion ASC
            """)
    Flux<ConversationParticipant> findByConversationId(Long conversationId);

    /**
     * Find specific participant in conversation
     */
    @Query("""
            SELECT cp.* FROM conversation_participants cp
            WHERE cp.conversation_id = :conversationId
            AND cp.user_id = :userId
            LIMIT 1
            """)
    Mono<ConversationParticipant> findByConversationIdAndUserId(Long conversationId, Long userId);

    /**
     * Check if user is participant in conversation
     */
    @Query("""
            SELECT COUNT(*) > 0 FROM conversation_participants cp
            WHERE cp.conversation_id = :conversationId
            AND cp.user_id = :userId
            """)
    Mono<Boolean> existsByConversationIdAndUserId(Long conversationId, Long userId);

    /**
     * Remove participant from conversation
     */
    @Query("""
            DELETE FROM conversation_participants 
            WHERE conversation_id = :conversationId 
            AND user_id = :userId
            """)
    Mono<Void> removeParticipant(Long conversationId, Long userId);

    /**
     * Find participants with user information
     */
        // Note: joined projection removed because R2DBC mapping of mixed entity + scalar columns
        // is unreliable. Use findByConversationId(...) and fetch user info in the service layer.
}
