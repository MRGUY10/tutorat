package com.iiil.tutoring.service.chat;

import com.iiil.tutoring.dto.chat.*;
import com.iiil.tutoring.entity.Conversation;
import com.iiil.tutoring.entity.ConversationParticipant;
import com.iiil.tutoring.repository.ConversationRepository;
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
import java.util.List;

/**
 * Service for conversation management operations
 */
@Service
@Transactional
public class ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationService.class);

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationParticipantRepository participantRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new conversation
     */
    public Mono<ConversationDTO> createConversation(CreateConversationDTO createDto, Long creatorId) {
        log.info("Creating conversation - Subject: '{}', CreatorId: {}, ParticipantIds: {}", 
                createDto.getSujet(), creatorId, createDto.getParticipantIds());
        
        return validateConversationCreation(createDto, creatorId)
                .doOnSuccess(valid -> log.debug("Validation successful"))
                .doOnError(error -> log.error("Validation failed: {}", error.getMessage(), error))
                .flatMap(valid -> {
                    log.debug("Creating conversation entity");
                    // Create the conversation entity
                    Conversation conversation = new Conversation();
                    conversation.setSujet(createDto.getSujet());
                    conversation.setDateCreation(LocalDateTime.now());
                    conversation.setArchivee(false);

                    return conversationRepository.save(conversation)
                            .doOnSuccess(saved -> log.info("Conversation saved with ID: {}", saved.getId()))
                            .doOnError(error -> log.error("Failed to save conversation: {}", error.getMessage(), error));
                })
                .flatMap(savedConversation -> {
                    // Add all participants including creator
                    List<Long> allParticipants = createDto.getParticipantIds();
                    if (!allParticipants.contains(creatorId)) {
                        allParticipants.add(creatorId);
                    }
                    
                    log.debug("Adding {} participants to conversation {}", allParticipants.size(), savedConversation.getId());

                    return Flux.fromIterable(allParticipants)
                            .flatMap(userId -> addParticipantToConversation(savedConversation.getId(), userId)
                                    .doOnSuccess(p -> log.debug("Added participant {} to conversation {}", userId, savedConversation.getId()))
                                    .doOnError(error -> log.error("Failed to add participant {} to conversation {}: {}", 
                                            userId, savedConversation.getId(), error.getMessage(), error)))
                            .then(Mono.just(savedConversation))
                            .doOnError(error -> log.error("Failed to add participants: {}", error.getMessage(), error));
                })
                .flatMap(conversation -> {
                    // Send initial message if provided
                    if (createDto.hasInitialMessage()) {
                        log.debug("Sending initial message for conversation {}", conversation.getId());
                        return sendInitialMessage(conversation.getId(), creatorId, createDto.getMessageInitial())
                                .then(Mono.just(conversation))
                                .doOnError(error -> log.error("Failed to send initial message: {}", error.getMessage(), error));
                    }
                    return Mono.just(conversation);
                })
                .flatMap(conversation -> {
                    log.debug("Building conversation DTO for conversation {}", conversation.getId());
                    return buildConversationDTO(conversation, creatorId)
                            .doOnSuccess(dto -> log.info("Successfully created conversation {}", dto.getId()))
                            .doOnError(error -> log.error("Failed to build conversation DTO: {}", error.getMessage(), error));
                })
                .doOnError(error -> log.error("Failed to create conversation: {}", error.getMessage(), error));
    }

    /**
     * Get conversation details with statistics
     */
    public Mono<ConversationDTO> getConversation(Long conversationId, Long userId) {
        return validateUserAccess(conversationId, userId)
                .flatMap(hasAccess -> {
                    if (!hasAccess) {
                        return Mono.error(new IllegalArgumentException("Accès non autorisé à cette conversation"));
                    }
                    return conversationRepository.findById(conversationId);
                })
                .flatMap(conversation -> buildConversationDTO(conversation, userId));
    }

    /**
     * Get all conversations for a user
     */
    public Flux<ConversationDTO> getUserConversations(Long userId, boolean includeArchived) {
        Flux<Conversation> conversations = includeArchived 
            ? conversationRepository.findAllByParticipantUserId(userId)
            : conversationRepository.findByParticipantUserId(userId);

        return conversations
                .flatMap(conversation -> buildConversationDTO(conversation, userId))
                .sort((c1, c2) -> {
                    // Sort by last message date, then by creation date
                    LocalDateTime date1 = c1.getLastMessageDate() != null 
                        ? c1.getLastMessageDate() : c1.getDateCreation();
                    LocalDateTime date2 = c2.getLastMessageDate() != null 
                        ? c2.getLastMessageDate() : c2.getDateCreation();
                    return date2.compareTo(date1); // Descending order
                });
    }

    /**
     * Get conversations with unread messages
     */
    public Flux<ConversationDTO> getConversationsWithUnreadMessages(Long userId) {
        return conversationRepository.findConversationsWithUnreadMessages(userId)
                .flatMap(conversation -> buildConversationDTO(conversation, userId));
    }

    /**
     * Update conversation (archive, subject, etc.)
     */
    public Mono<ConversationDTO> updateConversation(ConversationUpdateDTO updateDto, Long userId) {
        return validateUserAccess(updateDto.getConversationId(), userId)
                .flatMap(hasAccess -> {
                    if (!hasAccess) {
                        return Mono.error(new IllegalArgumentException("Accès non autorisé"));
                    }
                    return processConversationUpdate(updateDto, userId);
                });
    }

    /**
     * Archive/unarchive conversation
     */
    public Mono<Void> archiveConversation(Long conversationId, Long userId, boolean archived) {
        return validateUserAccess(conversationId, userId)
                .flatMap(hasAccess -> {
                    if (!hasAccess) {
                        return Mono.error(new IllegalArgumentException("Accès non autorisé"));
                    }
                    return conversationRepository.updateArchiveStatus(conversationId, archived);
                });
    }

    /**
     * Add participant to conversation
     */
    public Mono<ConversationParticipantDTO> addParticipant(Long conversationId, Long userIdToAdd, Long requesterId) {
        return validateUserAccess(conversationId, requesterId)
                .flatMap(hasAccess -> {
                    if (!hasAccess) {
                        return Mono.error(new IllegalArgumentException("Accès non autorisé"));
                    }
                    return addParticipantToConversation(conversationId, userIdToAdd);
                })
                .flatMap(participant -> buildParticipantDTO(participant));
    }

    /**
     * Remove participant from conversation
     */
    public Mono<Void> removeParticipant(Long conversationId, Long userIdToRemove, Long requesterId) {
        return validateUserAccess(conversationId, requesterId)
                .flatMap(hasAccess -> {
                    if (!hasAccess) {
                        return Mono.error(new IllegalArgumentException("Accès non autorisé"));
                    }
                    return participantRepository.removeParticipant(conversationId, userIdToRemove);
                });
    }

    /**
     * Leave conversation
     */
    public Mono<Void> leaveConversation(Long conversationId, Long userId) {
        return validateUserAccess(conversationId, userId)
                .flatMap(hasAccess -> {
                    if (!hasAccess) {
                        return Mono.error(new IllegalArgumentException("Accès non autorisé"));
                    }
                    return participantRepository.removeParticipant(conversationId, userId);
                });
    }

    /**
     * Get conversation participants
     */
    public Flux<ConversationParticipantDTO> getConversationParticipants(Long conversationId, Long userId) {
        return validateUserAccess(conversationId, userId)
                .flatMapMany(hasAccess -> {
                    if (!hasAccess) {
                        return Flux.error(new IllegalArgumentException("Accès non autorisé"));
                    }
                    return participantRepository.findByConversationId(conversationId)
                            .flatMap(this::buildParticipantDTO);
                });
    }

    /**
     * Search conversations
     */
    public Flux<ConversationDTO> searchConversations(Long userId, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getUserConversations(userId, false);
        }

        return conversationRepository.searchBySubject(userId, searchTerm.trim())
                .flatMap(conversation -> buildConversationDTO(conversation, userId));
    }

    /**
     * Get conversation statistics
     */
    public Mono<ConversationStatsDTO> getConversationStatistics(Long userId) {
        return conversationRepository.getConversationStats(userId)
                .flatMap(stats -> {
                    ConversationStatsDTO dto = new ConversationStatsDTO();
                    dto.setTotalConversations(stats.getTotalConversations());
                    dto.setActiveConversations(stats.getActiveConversations());
                    dto.setArchivedConversations(stats.getArchivedConversations());
                    
                    return conversationRepository.countUnreadConversations(userId)
                            .map(unreadCount -> {
                                dto.setUnreadConversations(unreadCount);
                                return dto;
                            });
                });
    }

    /**
     * Update user's last visit to conversation
     */
    public Mono<Void> updateLastVisit(Long conversationId, Long userId) {
        // Since the current entity doesn't have derniere_visite field,
        // we'll skip this implementation for now
        return Mono.empty();
    }

    // Private helper methods

    private Mono<Boolean> validateConversationCreation(CreateConversationDTO createDto, Long creatorId) {
        log.debug("Validating conversation creation - DTO valid: {}, ParticipantIds: {}", 
                createDto.isValid(), createDto.getParticipantIds());
        
        if (!createDto.isValid()) {
            log.error("Invalid conversation DTO");
            return Mono.error(new IllegalArgumentException("Données de conversation invalides"));
        }

        // Verify all participants exist
        return Flux.fromIterable(createDto.getParticipantIds())
                .flatMap(userId -> userRepository.existsById(userId)
                        .doOnNext(exists -> log.debug("User {} exists: {}", userId, exists)))
                .all(exists -> exists)
                .flatMap(allExist -> {
                    if (!allExist) {
                        log.error("Some participants don't exist");
                        return Mono.error(new IllegalArgumentException("Certains participants n'existent pas"));
                    }
                    log.debug("All participants validated successfully");
                    return Mono.just(true);
                });
    }

    private Mono<Boolean> validateUserAccess(Long conversationId, Long userId) {
        return participantRepository.existsByConversationIdAndUserId(conversationId, userId);
    }

    private Mono<ConversationParticipant> addParticipantToConversation(Long conversationId, Long userId) {
        // Check if already participant
        return participantRepository.findByConversationIdAndUserId(conversationId, userId)
                .switchIfEmpty(Mono.defer(() -> {
                    ConversationParticipant participant = new ConversationParticipant();
                    participant.setConversationId(conversationId);
                    participant.setUserId(userId);
                    participant.setDateAdhesion(LocalDateTime.now());
                    return participantRepository.save(participant);
                }));
    }

    private Mono<Void> sendInitialMessage(Long conversationId, Long senderId, String content) {
        // This would typically call MessageService, but for now we'll skip
        // Will be implemented when MessageService is created
        return Mono.empty();
    }

    private Mono<ConversationDTO> processConversationUpdate(ConversationUpdateDTO updateDto, Long userId) {
        Mono<Void> updateOperation = Mono.empty();

        if (updateDto.hasArchiveOperation()) {
            updateOperation = updateOperation.then(
                conversationRepository.updateArchiveStatus(updateDto.getConversationId(), updateDto.getArchived())
            );
        }

        if (updateDto.hasSubjectUpdate()) {
            updateOperation = updateOperation.then(
                conversationRepository.updateSubject(updateDto.getConversationId(), updateDto.getNewSubject())
            );
        }

        // No read operation update since last visit is not tracked

        return updateOperation
                .then(conversationRepository.findById(updateDto.getConversationId()))
                .flatMap(conversation -> buildConversationDTO(conversation, userId));
    }

    private Mono<ConversationDTO> buildConversationDTO(Conversation conversation, Long userId) {
        log.debug("Building DTO for conversation {} for user {}", conversation.getId(), userId);
        
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setSujet(conversation.getSujet());
        dto.setDateCreation(conversation.getDateCreation());
        dto.setArchivee(conversation.isArchivee());
        dto.setSupport(conversation.getSujet().toLowerCase().contains("support"));
        dto.setCurrentUserParticipant(true); // Already validated access

    // Get participant information (fetch participants, then enrich with user info)
    return participantRepository.findByConversationId(conversation.getId())
        .doOnNext(p -> log.debug("Found participant: userId={}, conversationId={}", p.getUserId(), p.getConversationId()))
        .flatMap(participant -> buildParticipantDTO(participant)
                .doOnError(error -> log.error("Error building participant DTO for user {}: {}", 
                        participant.getUserId(), error.getMessage(), error)))
        .collectList()
                .doOnNext(participants -> log.debug("Found {} participants for conversation {}", participants.size(), conversation.getId()))
                .map(participants -> {
                    dto.setParticipants(participants);
                    dto.setGroup(participants.size() > 2);
                    return dto;
                })
                .flatMap(dtoWithParticipants -> {
                    // Get message statistics
                    log.debug("Getting message count for conversation {}", conversation.getId());
                    return conversationRepository.countMessages(conversation.getId())
                            .doOnNext(count -> log.debug("Message count: {}", count))
                            .doOnError(error -> log.error("Error counting messages: {}", error.getMessage(), error))
                            .map(count -> {
                                dtoWithParticipants.setTotalMessages(count.intValue());
                                return dtoWithParticipants;
                            });
                })
                .flatMap(dtoWithStats -> {
                    // Get last message date
                    log.debug("Getting last message date for conversation {}", conversation.getId());
                    return conversationRepository.getLastMessageDate(conversation.getId())
                            .doOnNext(date -> log.debug("Last message date: {}", date))
                            .doOnError(error -> log.error("Error getting last message date: {}", error.getMessage(), error))
                            .map(lastDate -> {
                                dtoWithStats.setLastMessageDate(lastDate);
                                return dtoWithStats;
                            })
                            .defaultIfEmpty(dtoWithStats);
                });
    }

    private Mono<ConversationParticipantDTO> buildParticipantDTO(ConversationParticipant participant) {
        log.debug("Building participant DTO for userId: {}", participant.getUserId());
        
        return userRepository.findById(participant.getUserId())
                .doOnNext(user -> log.debug("Found user: {} {}", user.getPrenom(), user.getNom()))
                .doOnError(error -> log.error("Error finding user {}: {}", participant.getUserId(), error.getMessage(), error))
                .map(user -> {
                    ConversationParticipantDTO dto = new ConversationParticipantDTO();
                    dto.setId(participant.getId());
                    dto.setConversationId(participant.getConversationId());
                    dto.setUserId(participant.getUserId());
                    dto.setDateRejoint(participant.getDateAdhesion());
                    dto.setDerniereVisite(null); // Not tracked in current entity
                    dto.setNom(user.getNom());
                    dto.setPrenom(user.getPrenom());
                    dto.setEmail(user.getEmail());
                    
                    // Online status not available without derniere_visite field
                    dto.setOnline(false);
                    
                    return dto;
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("User not found for participant: userId={}", participant.getUserId());
                    return Mono.error(new IllegalStateException("User not found: " + participant.getUserId()));
                }));
    }

    private ConversationParticipantDTO mapToParticipantDTO(ConversationParticipant participant) {
        ConversationParticipantDTO dto = new ConversationParticipantDTO();
        dto.setId(participant.getId());
        dto.setConversationId(participant.getConversationId());
        dto.setUserId(participant.getUserId());
        dto.setDateRejoint(participant.getDateAdhesion());
        dto.setDerniereVisite(null); // Not tracked in current entity

        // Enrich with user info if available
        userRepository.findById(participant.getUserId()).subscribe(user -> {
            dto.setNom(user.getNom());
            dto.setPrenom(user.getPrenom());
            dto.setEmail(user.getEmail());
        });

        dto.setRoleUtilisateur(participant.getRole());
        dto.setOnline(false);

        return dto;
    }

    // Inner DTO for statistics
    public static class ConversationStatsDTO {
        private Long totalConversations;
        private Long activeConversations;
        private Long archivedConversations;
        private Long unreadConversations;

        // Getters and setters
        public Long getTotalConversations() { return totalConversations; }
        public void setTotalConversations(Long totalConversations) { this.totalConversations = totalConversations; }
        
        public Long getActiveConversations() { return activeConversations; }
        public void setActiveConversations(Long activeConversations) { this.activeConversations = activeConversations; }
        
        public Long getArchivedConversations() { return archivedConversations; }
        public void setArchivedConversations(Long archivedConversations) { this.archivedConversations = archivedConversations; }
        
        public Long getUnreadConversations() { return unreadConversations; }
        public void setUnreadConversations(Long unreadConversations) { this.unreadConversations = unreadConversations; }
    }
}