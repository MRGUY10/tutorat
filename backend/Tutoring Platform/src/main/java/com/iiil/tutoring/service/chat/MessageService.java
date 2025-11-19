package com.iiil.tutoring.service.chat;

import com.iiil.tutoring.dto.chat.*;
import com.iiil.tutoring.entity.Message;
import com.iiil.tutoring.repository.MessageRepository;
import com.iiil.tutoring.repository.ConversationParticipantRepository;
import com.iiil.tutoring.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Service for message operations including sending, receiving, and real-time features
 */
@Service
@Transactional
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationParticipantRepository participantRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Send a new message in a conversation
     */
    public Mono<MessageResponseDTO> sendMessage(CreateMessageDTO createMessageDTO, Long senderId) {
        return validateMessageCreation(createMessageDTO, senderId)
                .flatMap(valid -> {
                    // Create message entity
                    Message message = new Message();
                    message.setConversationId(createMessageDTO.getConversationId());
                    message.setExpediteurId(senderId);
                    message.setContenu(createMessageDTO.getContenu());
                    message.setDateEnvoi(LocalDateTime.now());
                    message.setLu(false);
                    message.setTypeMessage(createMessageDTO.getTypeMessage());
                    // Note: fichierUrl removed - not supported in current database schema

                    return messageRepository.save(message);
                })
                .flatMap(savedMessage -> buildMessageResponseDTO(savedMessage))
                .doOnNext(messageResponse -> {
                    // TODO: Send real-time notification via WebSocket
                    // This will be implemented when WebSocket infrastructure is ready
                });
    }

    /**
     * Get messages for a conversation with pagination
     */
    public Flux<MessageResponseDTO> getConversationMessages(Long conversationId, Long userId, int page, int size) {
        return validateUserAccess(conversationId, userId)
                .flatMapMany(hasAccess -> {
                    if (!hasAccess) {
                        return Flux.error(new IllegalArgumentException("Accès non autorisé à cette conversation"));
                    }

                    int offset = page * size;
                    return messageRepository.findByConversationIdWithPagination(conversationId, size, offset);
                })
                .flatMap(this::buildMessageResponseDTO);
    }

    /**
     * Get recent messages (last N messages)
     */
    public Flux<MessageResponseDTO> getRecentMessages(Long conversationId, Long userId, int limit) {
        log.info("Getting recent messages - ConversationId: {}, UserId: {}, Limit: {}", conversationId, userId, limit);
        
        return validateUserAccess(conversationId, userId)
                .doOnNext(hasAccess -> log.debug("User access validated: {}", hasAccess))
                .doOnError(error -> log.error("Error validating user access: {}", error.getMessage(), error))
                .flatMapMany(hasAccess -> {
                    if (!hasAccess) {
                        log.warn("User {} does not have access to conversation {}", userId, conversationId);
                        return Flux.error(new IllegalArgumentException("Accès non autorisé"));
                    }
                    log.debug("Fetching messages from repository");
                    return messageRepository.findRecentMessages(conversationId, limit)
                            .doOnNext(msg -> log.debug("Retrieved message: id={}, expediteurId={}", msg.getId(), msg.getExpediteurId()))
                            .doOnError(error -> log.error("Error fetching messages: {}", error.getMessage(), error));
                })
                .flatMap(message -> buildMessageResponseDTO(message)
                        .doOnError(error -> log.error("Error building DTO for message {}: {}", message.getId(), error.getMessage(), error)))
                .doOnComplete(() -> log.debug("Completed fetching recent messages"))
                .sort((m1, m2) -> m1.getDateEnvoi().compareTo(m2.getDateEnvoi())); // Sort ascending
    }

    /**
     * Get unread messages for user in conversation
     */
    public Flux<MessageResponseDTO> getUnreadMessages(Long conversationId, Long userId) {
        return validateUserAccess(conversationId, userId)
                .flatMapMany(hasAccess -> {
                    if (!hasAccess) {
                        return Flux.error(new IllegalArgumentException("Accès non autorisé"));
                    }
                    return messageRepository.findUnreadMessages(conversationId, userId);
                })
                .flatMap(this::buildMessageResponseDTO);
    }

    /**
     * Mark message as read
     */
    public Mono<Void> markMessageAsRead(Long messageId, Long userId) {
        return messageRepository.findById(messageId)
                .flatMap(message -> {
                    // Verify user has access to the conversation
                    return validateUserAccess(message.getConversationId(), userId)
                            .flatMap(hasAccess -> {
                                if (!hasAccess) {
                                    return Mono.error(new IllegalArgumentException("Accès non autorisé"));
                                }
                                // Don't mark own messages as read
                                if (message.getExpediteurId().equals(userId)) {
                                    return Mono.empty();
                                }
                                return messageRepository.markAsRead(messageId);
                            });
                });
    }

    /**
     * Mark all messages in conversation as read for user
     */
    public Mono<Void> markAllMessagesAsRead(Long conversationId, Long userId) {
        return validateUserAccess(conversationId, userId)
                .flatMap(hasAccess -> {
                    if (!hasAccess) {
                        return Mono.error(new IllegalArgumentException("Accès non autorisé"));
                    }
                    return messageRepository.markAllAsReadInConversation(conversationId, userId);
                });
    }

    /**
     * Get unread message count for user in conversation
     */
    public Mono<Long> getUnreadMessageCount(Long conversationId, Long userId) {
        return validateUserAccess(conversationId, userId)
                .flatMap(hasAccess -> {
                    if (!hasAccess) {
                        return Mono.error(new IllegalArgumentException("Accès non autorisé"));
                    }
                    return messageRepository.countUnreadMessages(conversationId, userId);
                });
    }

    /**
     * Get total unread message count for user across all conversations
     */
    public Mono<Long> getTotalUnreadMessageCount(Long userId) {
        return messageRepository.countTotalUnreadMessages(userId);
    }

    /**
     * Search messages by content
     */
    public Flux<MessageResponseDTO> searchMessages(Long userId, String searchTerm, int limit) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return Flux.empty();
        }

        return messageRepository.searchMessagesByContent(userId, searchTerm.trim(), limit)
                .flatMap(this::buildMessageResponseDTO);
    }

    /**
     * Get message by ID with access validation
     */
    public Mono<MessageResponseDTO> getMessage(Long messageId, Long userId) {
        return messageRepository.findById(messageId)
                .flatMap(message -> 
                    validateUserAccess(message.getConversationId(), userId)
                        .flatMap(hasAccess -> {
                            if (!hasAccess) {
                                return Mono.error(new IllegalArgumentException("Accès non autorisé"));
                            }
                            return buildMessageResponseDTO(message);
                        })
                );
    }

    /**
     * Delete message (soft delete)
     */
    public Mono<Void> deleteMessage(Long messageId, Long userId) {
        return messageRepository.findById(messageId)
                .flatMap(message -> {
                    // Only sender can delete their own messages
                    if (!message.getExpediteurId().equals(userId)) {
                        return Mono.error(new IllegalArgumentException("Vous ne pouvez supprimer que vos propres messages"));
                    }
                    return messageRepository.deleteMessage(messageId, userId);
                });
    }

    /**
     * Get message statistics for conversation
     */
    public Mono<MessageStatsDTO> getMessageStatistics(Long conversationId, Long userId) {
        return validateUserAccess(conversationId, userId)
                .flatMap(hasAccess -> {
                    if (!hasAccess) {
                        return Mono.error(new IllegalArgumentException("Accès non autorisé"));
                    }
                    return messageRepository.getMessageStats(conversationId);
                })
                .map(this::mapToMessageStatsDTO);
    }

    /**
     * Get messages sent since a specific date
     */
    public Flux<MessageResponseDTO> getMessagesSince(Long conversationId, Long userId, LocalDateTime since) {
        return validateUserAccess(conversationId, userId)
                .flatMapMany(hasAccess -> {
                    if (!hasAccess) {
                        return Flux.error(new IllegalArgumentException("Accès non autorisé"));
                    }
                    return messageRepository.findMessagesSince(conversationId, since);
                })
                .flatMap(this::buildMessageResponseDTO);
    }

    /**
     * Get last message in conversation
     */
    public Mono<MessageResponseDTO> getLastMessage(Long conversationId, Long userId) {
        return validateUserAccess(conversationId, userId)
                .flatMap(hasAccess -> {
                    if (!hasAccess) {
                        return Mono.error(new IllegalArgumentException("Accès non autorisé"));
                    }
                    return messageRepository.findLastMessage(conversationId);
                })
                .flatMap(this::buildMessageResponseDTO);
    }

    /**
     * Get replies to a specific message
     */
    public Flux<MessageResponseDTO> getRepliesToMessage(Long messageId, Long userId) {
        return messageRepository.findById(messageId)
                .flatMap(message -> validateUserAccess(message.getConversationId(), userId))
                .flatMapMany(hasAccess -> {
                    if (!hasAccess) {
                        return Flux.error(new IllegalArgumentException("Accès non autorisé"));
                    }
                    return messageRepository.findRepliesToMessage(messageId);
                })
                .flatMap(this::buildMessageResponseDTO);
    }

    /**
     * Mark all unread messages in conversation as read for user
     */
    public Mono<Void> markAsRead(Long conversationId, Long userId) {
        return validateUserAccess(conversationId, userId)
                .flatMap(hasAccess -> {
                    if (!hasAccess) {
                        return Mono.error(new IllegalArgumentException("Accès non autorisé"));
                    }
                    return messageRepository.markAllAsReadInConversation(conversationId, userId);
                });
    }

    private Mono<Boolean> validateMessageCreation(CreateMessageDTO createDto, Long senderId) {
        if (createDto.getConversationId() == null || 
            createDto.getContenu() == null || createDto.getContenu().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Données de message invalides"));
        }

        return validateUserAccess(createDto.getConversationId(), senderId);
    }

    private Mono<Boolean> validateUserAccess(Long conversationId, Long userId) {
        return participantRepository.existsByConversationIdAndUserId(conversationId, userId);
    }

    private Mono<MessageResponseDTO> buildMessageResponseDTO(Message message) {
        log.debug("Building message response DTO for message: id={}, expediteurId={}", message.getId(), message.getExpediteurId());
        
        MessageResponseDTO dto = new MessageResponseDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversationId());
        dto.setExpediteurId(message.getExpediteurId());
        dto.setContenu(message.getContenu());
        dto.setDateEnvoi(message.getDateEnvoi());
        dto.setLu(message.isLu());
        dto.setTypeMessage(message.getTypeMessage());
        dto.setFichierUrl(null); // Not supported in current database schema
        dto.setReplyToMessageId(null); // Not supported in current entity

        // Get sender information
        return userRepository.findById(message.getExpediteurId())
                .doOnNext(user -> log.debug("Found user: {} {} (id={})", user.getPrenom(), user.getNom(), user.getId()))
                .doOnError(error -> log.error("Error finding user {}: {}", message.getExpediteurId(), error.getMessage(), error))
                .map(user -> {
                    dto.setExpediteurNom(user.getNom());
                    dto.setExpediteurPrenom(user.getPrenom());
                    dto.setExpediteurEmail(user.getEmail());
                    return dto;
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("User not found for message sender: userId={}, messageId={}", message.getExpediteurId(), message.getId());
                    // Return DTO with placeholder values instead of failing
                    dto.setExpediteurNom("Utilisateur");
                    dto.setExpediteurPrenom("Inconnu");
                    dto.setExpediteurEmail("");
                    return Mono.just(dto);
                }))
                .flatMap(dtoWithSender -> {
                    // Reply functionality not implemented in current entity structure
                    return Mono.just(dtoWithSender);
                });
    }

    private MessageStatsDTO mapToMessageStatsDTO(MessageRepository.MessageStats stats) {
        MessageStatsDTO dto = new MessageStatsDTO();
        dto.setTotalMessages(stats.getTotalMessages());
        dto.setTextMessages(stats.getTextMessages());
        dto.setFileMessages(stats.getFileMessages());
        dto.setImageMessages(stats.getImageMessages());
        dto.setUnreadMessages(stats.getUnreadMessages());
        return dto;
    }

    // Inner DTO for message statistics
    public static class MessageStatsDTO {
        private Long totalMessages;
        private Long textMessages;
        private Long fileMessages;
        private Long imageMessages;
        private Long unreadMessages;

        // Getters and setters
        public Long getTotalMessages() { return totalMessages; }
        public void setTotalMessages(Long totalMessages) { this.totalMessages = totalMessages; }
        
        public Long getTextMessages() { return textMessages; }
        public void setTextMessages(Long textMessages) { this.textMessages = textMessages; }
        
        public Long getFileMessages() { return fileMessages; }
        public void setFileMessages(Long fileMessages) { this.fileMessages = fileMessages; }
        
        public Long getImageMessages() { return imageMessages; }
        public void setImageMessages(Long imageMessages) { this.imageMessages = imageMessages; }
        
        public Long getUnreadMessages() { return unreadMessages; }
        public void setUnreadMessages(Long unreadMessages) { this.unreadMessages = unreadMessages; }
    }
}