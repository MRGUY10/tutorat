package com.iiil.tutoring.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO for creating a new conversation
 */
public class CreateConversationDTO {

    @NotBlank(message = "Le sujet de la conversation ne peut pas être vide")
    @Size(max = 255, message = "Le sujet ne peut pas dépasser 255 caractères")
    private String sujet;

    @NotNull(message = "Les participants sont obligatoires")
    @Size(min = 1, message = "Une conversation doit avoir au moins 1 participant")
    private List<Long> participantIds;

    // For session-based conversations
    private Long sessionId;

    // For support or admin conversations
    private boolean isSupport = false;

    // For group conversations
    private boolean isGroup = false;

    // Initial message (optional)
    @Size(max = 2000, message = "Le message initial ne peut pas dépasser 2000 caractères")
    private String messageInitial;

    // Constructors
    public CreateConversationDTO() {}

    public CreateConversationDTO(String sujet, List<Long> participantIds) {
        this.sujet = sujet;
        this.participantIds = participantIds;
    }

    // Getters and Setters
    public String getSujet() {
        return sujet;
    }

    public void setSujet(String sujet) {
        this.sujet = sujet;
    }

    public List<Long> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<Long> participantIds) {
        this.participantIds = participantIds;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
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

    public String getMessageInitial() {
        return messageInitial;
    }

    public void setMessageInitial(String messageInitial) {
        this.messageInitial = messageInitial;
    }

    // Validation methods
    public boolean isValid() {
        return sujet != null && !sujet.trim().isEmpty() &&
               participantIds != null && participantIds.size() >= 1 &&
               (messageInitial == null || messageInitial.length() <= 2000);
    }

    public boolean isSessionConversation() {
        return sessionId != null;
    }

    public boolean hasInitialMessage() {
        return messageInitial != null && !messageInitial.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "CreateConversationDTO{" +
                "sujet='" + sujet + '\'' +
                ", participantIds=" + participantIds +
                ", sessionId=" + sessionId +
                ", isSupport=" + isSupport +
                ", isGroup=" + isGroup +
                '}';
    }
}