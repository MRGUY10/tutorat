package com.iiil.tutoring.dto.chat;

import java.util.List;

/**
 * DTO for updating conversation read status and other operations
 */
public class ConversationUpdateDTO {

    private Long conversationId;

    // For marking messages as read
    private boolean markAsRead;
    private List<Long> messageIds; // Specific messages to mark as read

    // For archiving/unarchiving conversation
    private Boolean archived;

    // For updating conversation subject
    private String newSubject;

    // For adding participants (group conversations)
    private List<Long> addParticipants;

    // For removing participants (group conversations)
    private List<Long> removeParticipants;

    // For leaving conversation
    private boolean leaveConversation;

    // For typing status
    private Boolean isTyping;

    // Constructors
    public ConversationUpdateDTO() {}

    public ConversationUpdateDTO(Long conversationId) {
        this.conversationId = conversationId;
    }

    // Static factory methods for common operations
    public static ConversationUpdateDTO markAsRead(Long conversationId) {
        ConversationUpdateDTO dto = new ConversationUpdateDTO(conversationId);
        dto.setMarkAsRead(true);
        return dto;
    }

    public static ConversationUpdateDTO markMessagesAsRead(Long conversationId, List<Long> messageIds) {
        ConversationUpdateDTO dto = new ConversationUpdateDTO(conversationId);
        dto.setMarkAsRead(true);
        dto.setMessageIds(messageIds);
        return dto;
    }

    public static ConversationUpdateDTO archive(Long conversationId, boolean archived) {
        ConversationUpdateDTO dto = new ConversationUpdateDTO(conversationId);
        dto.setArchived(archived);
        return dto;
    }

    public static ConversationUpdateDTO updateSubject(Long conversationId, String newSubject) {
        ConversationUpdateDTO dto = new ConversationUpdateDTO(conversationId);
        dto.setNewSubject(newSubject);
        return dto;
    }

    public static ConversationUpdateDTO setTyping(Long conversationId, boolean isTyping) {
        ConversationUpdateDTO dto = new ConversationUpdateDTO(conversationId);
        dto.setIsTyping(isTyping);
        return dto;
    }

    // Getters and Setters
    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public boolean isMarkAsRead() {
        return markAsRead;
    }

    public void setMarkAsRead(boolean markAsRead) {
        this.markAsRead = markAsRead;
    }

    public List<Long> getMessageIds() {
        return messageIds;
    }

    public void setMessageIds(List<Long> messageIds) {
        this.messageIds = messageIds;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public String getNewSubject() {
        return newSubject;
    }

    public void setNewSubject(String newSubject) {
        this.newSubject = newSubject;
    }

    public List<Long> getAddParticipants() {
        return addParticipants;
    }

    public void setAddParticipants(List<Long> addParticipants) {
        this.addParticipants = addParticipants;
    }

    public List<Long> getRemoveParticipants() {
        return removeParticipants;
    }

    public void setRemoveParticipants(List<Long> removeParticipants) {
        this.removeParticipants = removeParticipants;
    }

    public boolean isLeaveConversation() {
        return leaveConversation;
    }

    public void setLeaveConversation(boolean leaveConversation) {
        this.leaveConversation = leaveConversation;
    }

    public Boolean getIsTyping() {
        return isTyping;
    }

    public void setIsTyping(Boolean isTyping) {
        this.isTyping = isTyping;
    }

    // Validation and helper methods
    public boolean isValid() {
        return conversationId != null;
    }

    public boolean hasReadOperation() {
        return markAsRead;
    }

    public boolean hasArchiveOperation() {
        return archived != null;
    }

    public boolean hasSubjectUpdate() {
        return newSubject != null && !newSubject.trim().isEmpty();
    }

    public boolean hasParticipantChanges() {
        return (addParticipants != null && !addParticipants.isEmpty()) ||
               (removeParticipants != null && !removeParticipants.isEmpty());
    }

    public boolean hasTypingUpdate() {
        return isTyping != null;
    }

    public boolean hasSpecificMessages() {
        return messageIds != null && !messageIds.isEmpty();
    }

    @Override
    public String toString() {
        return "ConversationUpdateDTO{" +
                "conversationId=" + conversationId +
                ", markAsRead=" + markAsRead +
                ", archived=" + archived +
                ", newSubject='" + newSubject + '\'' +
                ", leaveConversation=" + leaveConversation +
                ", isTyping=" + isTyping +
                '}';
    }
}