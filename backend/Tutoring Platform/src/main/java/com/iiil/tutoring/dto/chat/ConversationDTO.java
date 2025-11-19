package com.iiil.tutoring.dto.chat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for conversation response with participants and summary
 */
public class ConversationDTO {

    private Long id;
    private String sujet;
    private LocalDateTime dateCreation;
    private boolean archivee;
    private Long sessionId;

    // Conversation statistics
    private int totalMessages;
    private int unreadMessages;
    private LocalDateTime lastMessageDate;
    private String lastMessageContent;
    private String lastMessageSenderName;

    // Participants information
    private List<ConversationParticipantDTO> participants;

    // Current user context
    private boolean isCurrentUserParticipant;
    private LocalDateTime currentUserLastRead;

    // Conversation type flags
    private boolean isSupport;
    private boolean isGroup;

    // Constructors
    public ConversationDTO() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSujet() {
        return sujet;
    }

    public void setSujet(String sujet) {
        this.sujet = sujet;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public boolean isArchivee() {
        return archivee;
    }

    public void setArchivee(boolean archivee) {
        this.archivee = archivee;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public int getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(int totalMessages) {
        this.totalMessages = totalMessages;
    }

    public int getUnreadMessages() {
        return unreadMessages;
    }

    public void setUnreadMessages(int unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    public LocalDateTime getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(LocalDateTime lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }

    public String getLastMessageContent() {
        return lastMessageContent;
    }

    public void setLastMessageContent(String lastMessageContent) {
        this.lastMessageContent = lastMessageContent;
    }

    public String getLastMessageSenderName() {
        return lastMessageSenderName;
    }

    public void setLastMessageSenderName(String lastMessageSenderName) {
        this.lastMessageSenderName = lastMessageSenderName;
    }

    public List<ConversationParticipantDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ConversationParticipantDTO> participants) {
        this.participants = participants;
    }

    public boolean isCurrentUserParticipant() {
        return isCurrentUserParticipant;
    }

    public void setCurrentUserParticipant(boolean currentUserParticipant) {
        isCurrentUserParticipant = currentUserParticipant;
    }

    public LocalDateTime getCurrentUserLastRead() {
        return currentUserLastRead;
    }

    public void setCurrentUserLastRead(LocalDateTime currentUserLastRead) {
        this.currentUserLastRead = currentUserLastRead;
    }

    public boolean isSupport() {
        return isSupport;
    }

    public void setSupport(boolean support) {
        isSupport = support;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    // Helper methods
    public boolean hasUnreadMessages() {
        return unreadMessages > 0;
    }

    public boolean hasMessages() {
        return totalMessages > 0;
    }

    public boolean isSessionConversation() {
        return sessionId != null;
    }

    public String getLastMessagePreview() {
        if (lastMessageContent == null) {
            return "Aucun message";
        }
        return lastMessageContent.length() > 50 
            ? lastMessageContent.substring(0, 50) + "..."
            : lastMessageContent;
    }

    public int getParticipantCount() {
        return participants != null ? participants.size() : 0;
    }

    @Override
    public String toString() {
        return "ConversationDTO{" +
                "id=" + id +
                ", sujet='" + sujet + '\'' +
                ", dateCreation=" + dateCreation +
                ", archivee=" + archivee +
                ", totalMessages=" + totalMessages +
                ", unreadMessages=" + unreadMessages +
                '}';
    }
}