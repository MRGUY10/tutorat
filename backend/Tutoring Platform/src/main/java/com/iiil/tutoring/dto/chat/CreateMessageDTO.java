package com.iiil.tutoring.dto.chat;

import com.iiil.tutoring.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new message
 */
public class CreateMessageDTO {

    @NotNull(message = "L'ID de la conversation est obligatoire")
    private Long conversationId;

    @NotBlank(message = "Le contenu ne peut pas être vide")
    @Size(max = 2000, message = "Le message ne peut pas dépasser 2000 caractères")
    private String contenu;

    private MessageType typeMessage = MessageType.TEXTE;

    private String fichierUrl; // For future file support

    // Reply information (optional)
    private Long replyToMessageId;

    // Constructors
    public CreateMessageDTO() {}

    public CreateMessageDTO(Long conversationId, String contenu) {
        this.conversationId = conversationId;
        this.contenu = contenu;
        this.typeMessage = MessageType.TEXTE;
    }

    public CreateMessageDTO(Long conversationId, String contenu, MessageType typeMessage) {
        this.conversationId = conversationId;
        this.contenu = contenu;
        this.typeMessage = typeMessage;
    }

    // Getters and Setters
    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
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

    // Validation methods
    public boolean isValidTextMessage() {
        return typeMessage == MessageType.TEXTE && contenu != null && !contenu.trim().isEmpty();
    }

    public boolean isValidFileMessage() {
        return (typeMessage == MessageType.FICHIER || typeMessage == MessageType.IMAGE) 
               && fichierUrl != null && !fichierUrl.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "CreateMessageDTO{" +
                "conversationId=" + conversationId +
                ", contenu='" + contenu + '\'' +
                ", typeMessage=" + typeMessage +
                ", replyToMessageId=" + replyToMessageId +
                '}';
    }
}