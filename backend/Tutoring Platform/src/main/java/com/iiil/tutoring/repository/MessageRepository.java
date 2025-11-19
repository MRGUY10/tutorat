package com.iiil.tutoring.repository;

import com.iiil.tutoring.entity.Message;
import com.iiil.tutoring.enums.MessageType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Repository for message operations
 */
@Repository
public interface MessageRepository extends ReactiveCrudRepository<Message, Long> {

    /**
     * Find all messages in a conversation ordered by date
     */
    @Query("""
            SELECT m.* FROM messages m
            WHERE m.conversation_id = :conversationId
            ORDER BY m.date_envoi ASC
            """)
    Flux<Message> findByConversationIdOrderByDateEnvoi(Long conversationId);

    /**
     * Find messages in conversation with pagination
     */
    @Query("""
            SELECT m.* FROM messages m
            WHERE m.conversation_id = :conversationId
            ORDER BY m.date_envoi DESC
            LIMIT :limit OFFSET :offset
            """)
    Flux<Message> findByConversationIdWithPagination(Long conversationId, int limit, int offset);

    /**
     * Find recent messages in conversation (last N messages)
     */
    @Query("""
            SELECT m.* FROM messages m
            WHERE m.conversation_id = :conversationId
            ORDER BY m.date_envoi DESC
            LIMIT :limit
            """)
    Flux<Message> findRecentMessages(Long conversationId, int limit);

    /**
     * Find unread messages for a user in a conversation
     */
    @Query("""
            SELECT m.* FROM messages m
            INNER JOIN conversation_participant cp ON m.conversation_id = cp.conversation_id
            WHERE m.conversation_id = :conversationId
            AND cp.user_id = :userId
            AND m.expediteur_id != :userId
            AND m.lu = false
                        ORDER BY m.date_envoi ASC
            """)
    Flux<Message> findUnreadMessages(Long conversationId, Long userId);

    /**
     * Count unread messages for a user in a conversation
     */
    @Query("""
            SELECT COUNT(*) FROM messages m
            INNER JOIN conversation_participant cp ON m.conversation_id = cp.conversation_id
            WHERE m.conversation_id = :conversationId
            AND cp.user_id = :userId
            AND m.expediteur_id != :userId
            AND m.lu = false
                        """)
    Mono<Long> countUnreadMessages(Long conversationId, Long userId);

    /**
     * Count total unread messages for a user across all conversations
     */
    @Query("""
            SELECT COUNT(*) FROM messages m
            INNER JOIN conversation_participant cp ON m.conversation_id = cp.conversation_id
            INNER JOIN conversation c ON m.conversation_id = c.id
            WHERE cp.user_id = :userId
            AND m.expediteur_id != :userId
            AND m.lu = false
            AND c.archivee = false
                        """)
    Mono<Long> countTotalUnreadMessages(Long userId);

    /**
     * Find messages by sender
     */
    @Query("""
            SELECT m.* FROM messages m
            WHERE m.conversation_id = :conversationId
            AND m.expediteur_id = :senderId
            ORDER BY m.date_envoi DESC
            """)
    Flux<Message> findBySender(Long conversationId, Long senderId);

    /**
     * Search messages by content
     */
    @Query("""
            SELECT m.* FROM messages m
            INNER JOIN conversation_participant cp ON m.conversation_id = cp.conversation_id
            WHERE cp.user_id = :userId
            AND LOWER(m.contenu) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            ORDER BY m.date_envoi DESC
            LIMIT :limit
            """)
    Flux<Message> searchMessagesByContent(Long userId, String searchTerm, int limit);

    /**
     * Find messages by type in conversation
     */
    @Query("""
            SELECT m.* FROM messages m
            WHERE m.conversation_id = :conversationId
            AND m.type_message = :messageType
            ORDER BY m.date_envoi DESC
            """)
    Flux<Message> findByMessageType(Long conversationId, MessageType messageType);

    /**
     * Find messages sent after a specific date
     */
    @Query("""
            SELECT m.* FROM messages m
            WHERE m.conversation_id = :conversationId
            AND m.date_envoi > :since
            ORDER BY m.date_envoi ASC
            """)
    Flux<Message> findMessagesSince(Long conversationId, LocalDateTime since);

    /**
     * Get last message in conversation
     */
    @Query("""
            SELECT m.* FROM messages m
            WHERE m.conversation_id = :conversationId
            ORDER BY m.date_envoi DESC
            LIMIT 1
            """)
    Mono<Message> findLastMessage(Long conversationId);

    /**
     * Get message with sender information
     */
    @Query("""
            SELECT m.*, u.nom, u.prenom, u.email 
            FROM messages m
            INNER JOIN user u ON m.expediteur_id = u.id
            WHERE m.id = :messageId
            """)
    Mono<MessageWithSender> findMessageWithSender(Long messageId);

    /**
     * Get messages with sender information for conversation
     */
    @Query("""
            SELECT m.*, u.nom, u.prenom, u.email 
            FROM messages m
            INNER JOIN user u ON m.expediteur_id = u.id
            WHERE m.conversation_id = :conversationId
            ORDER BY m.date_envoi ASC
            """)
    Flux<MessageWithSender> findMessagesWithSender(Long conversationId);

    /**
     * Mark message as read
     */
    @Query("""
            UPDATE messages 
            SET lu = true 
            WHERE id = :messageId
            """)
    Mono<Void> markAsRead(Long messageId);

    /**
     * Mark multiple messages as read
     */
    @Query("""
            UPDATE messages 
            SET lu = true 
            WHERE id IN (:messageIds)
            """)
    Mono<Void> markMultipleAsRead(Iterable<Long> messageIds);

    /**
     * Mark all messages in conversation as read for user
     */
    @Query("""
            UPDATE messages 
            SET lu = true 
            WHERE conversation_id = :conversationId
            AND expediteur_id != :userId
            AND lu = false
            """)
    Mono<Void> markAllAsReadInConversation(Long conversationId, Long userId);

    /**
     * Delete message (soft delete by setting content to null)
     */
    @Query("""
            UPDATE messages 
            SET contenu = '[Message supprimé]', 
                fichier_url = null
            WHERE id = :messageId
            AND expediteur_id = :senderId
            """)
    Mono<Void> deleteMessage(Long messageId, Long senderId);

    /**
     * Count messages by type in conversation
     */
    @Query("""
            SELECT COUNT(*) FROM messages m
            WHERE m.conversation_id = :conversationId
            AND m.type_message = :messageType
            """)
    Mono<Long> countByMessageType(Long conversationId, MessageType messageType);

    /**
     * Get conversation message statistics
     */
    @Query("""
            SELECT 
                COUNT(*) as total_messages,
                COUNT(CASE WHEN type_message = 'TEXTE' THEN 1 END) as text_messages,
                COUNT(CASE WHEN type_message = 'FICHIER' THEN 1 END) as file_messages,
                COUNT(CASE WHEN type_message = 'IMAGE' THEN 1 END) as image_messages,
                COUNT(CASE WHEN lu = false THEN 1 END) as unread_messages
            FROM messages 
            WHERE conversation_id = :conversationId
            """)
    Mono<MessageStats> getMessageStats(Long conversationId);

    /**
     * Find replied messages (messages that are replies to other messages)
     */
    @Query("""
            SELECT m.* FROM messages m
            WHERE m.conversation_id = :conversationId
            AND m.reply_to_message_id IS NOT NULL
            ORDER BY m.date_envoi ASC
            """)
    Flux<Message> findRepliedMessages(Long conversationId);

    /**
     * Find replies to a specific message
     */
    @Query("""
            SELECT m.* FROM messages m
            WHERE m.reply_to_message_id = :messageId
            ORDER BY m.date_envoi ASC
            """)
    Flux<Message> findRepliesToMessage(Long messageId);

    /**
     * Get message activity for conversation (messages per day/hour)
     */
    @Query("""
            SELECT DATE(m.date_envoi) as message_date, COUNT(*) as message_count
            FROM messages m
            WHERE m.conversation_id = :conversationId
            AND m.date_envoi >= :since
            GROUP BY DATE(m.date_envoi)
            ORDER BY message_date DESC
            """)
    Flux<MessageActivity> getMessageActivity(Long conversationId, LocalDateTime since);

    // DTO classes for complex queries
    class MessageWithSender {
        private final Message message;
        private final String nom;
        private final String prenom;
        private final String email;

        public MessageWithSender(Message message, String nom, String prenom, String email) {
            this.message = message;
            this.nom = nom;
            this.prenom = prenom;
            this.email = email;
        }

        public Message getMessage() { return message; }
        public String getNom() { return nom; }
        public String getPrenom() { return prenom; }
        public String getEmail() { return email; }
        public String getFullName() { return prenom + " " + nom; }
    }

    class MessageStats {
        private final Long totalMessages;
        private final Long textMessages;
        private final Long fileMessages;
        private final Long imageMessages;
        private final Long unreadMessages;

        public MessageStats(Long totalMessages, Long textMessages, Long fileMessages, 
                           Long imageMessages, Long unreadMessages) {
            this.totalMessages = totalMessages;
            this.textMessages = textMessages;
            this.fileMessages = fileMessages;
            this.imageMessages = imageMessages;
            this.unreadMessages = unreadMessages;
        }

        public Long getTotalMessages() { return totalMessages; }
        public Long getTextMessages() { return textMessages; }
        public Long getFileMessages() { return fileMessages; }
        public Long getImageMessages() { return imageMessages; }
        public Long getUnreadMessages() { return unreadMessages; }
    }

    class MessageActivity {
        private final LocalDateTime messageDate;
        private final Long messageCount;

        public MessageActivity(LocalDateTime messageDate, Long messageCount) {
            this.messageDate = messageDate;
            this.messageCount = messageCount;
        }

        public LocalDateTime getMessageDate() { return messageDate; }
        public Long getMessageCount() { return messageCount; }
    }
}

