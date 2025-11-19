package com.iiil.tutoring.dto.chat;

import com.iiil.tutoring.enums.MessageType;

import java.time.LocalDateTime;

/**
 * DTO for message response with basic information
 */
public class MessageResponseDTO {

    private Long id;
    private Long conversationId;
    private Long expediteurId;
    private String contenu;
    private LocalDateTime dateEnvoi;
    private boolean lu;
    private MessageType typeMessage;
    private String fichierUrl;
    private Long replyToMessageId;

    // Sender information
    private String expediteurNom;
    private String expediteurPrenom;
    private String expediteurEmail;

    // Reply message information (if applicable)
    private String replyToContent;
    private String replyToSenderName;

    // Constructors
    public MessageResponseDTO() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getExpediteurId() {
        return expediteurId;
    }

    public void setExpediteurId(Long expediteurId) {
        this.expediteurId = expediteurId;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(LocalDateTime dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public boolean isLu() {
        return lu;
    }

    public void setLu(boolean lu) {
        this.lu = lu;
    }

    public MessageType getTypeMessage() {
        return typeMessage;
    }

    public void setTypeMessage(MessageType typeMessage) {
        this.typeMessage = typeMessage;
    }

    public String getFichierUrl() {
        return fichierUrl;
    }

    public void setFichierUrl(String fichierUrl) {
        this.fichierUrl = fichierUrl;
    }

    public Long getReplyToMessageId() {
        return replyToMessageId;
    }

    public void setReplyToMessageId(Long replyToMessageId) {
        this.replyToMessageId = replyToMessageId;
    }

    public String getExpediteurNom() {
        return expediteurNom;
    }

    public void setExpediteurNom(String expediteurNom) {
        this.expediteurNom = expediteurNom;
    }

    public String getExpediteurPrenom() {
        return expediteurPrenom;
    }

    public void setExpediteurPrenom(String expediteurPrenom) {
        this.expediteurPrenom = expediteurPrenom;
    }

    public String getExpediteurEmail() {
        return expediteurEmail;
    }

    public void setExpediteurEmail(String expediteurEmail) {
        this.expediteurEmail = expediteurEmail;
    }

    public String getReplyToContent() {
        return replyToContent;
    }

    public void setReplyToContent(String replyToContent) {
        this.replyToContent = replyToContent;
    }

    public String getReplyToSenderName() {
        return replyToSenderName;
    }

    public void setReplyToSenderName(String replyToSenderName) {
        this.replyToSenderName = replyToSenderName;
    }

    // Helper methods
    public String getExpediteurFullName() {
        return expediteurPrenom + " " + expediteurNom;
    }

    public boolean isTextMessage() {
        return typeMessage == MessageType.TEXTE;
    }

    public boolean isFileMessage() {
        return typeMessage == MessageType.FICHIER || typeMessage == MessageType.IMAGE;
    }

    public boolean hasReply() {
        return replyToMessageId != null;
    }

    @Override
    public String toString() {
        return "MessageResponseDTO{" +
                "id=" + id +
                ", conversationId=" + conversationId +
                ", expediteurId=" + expediteurId +
                ", contenu='" + contenu + '\'' +
                ", dateEnvoi=" + dateEnvoi +
                ", lu=" + lu +
                ", typeMessage=" + typeMessage +
                '}';
    }
}