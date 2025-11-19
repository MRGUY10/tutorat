package com.iiil.tutoring.entity;

import com.iiil.tutoring.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Message entity
 */
@Table("messages")
public class Message {

    @Id
    private Long id;

    @NotNull(message = "L'ID de la conversation est obligatoire")
    @Column("conversation_id")
    private Long conversationId;

    @NotNull(message = "L'ID de l'expéditeur est obligatoire")
    @Column("expediteur_id")
    private Long expediteurId;

    @NotBlank(message = "Le contenu est obligatoire")
    @Column("contenu")
    private String contenu;

    @Column("date_envoi")
    @CreatedDate
    private LocalDateTime dateEnvoi;

    @Column("lu")
    private boolean lu = false;

    @NotNull(message = "Le type de message est obligatoire")
    @Column("type")
    private MessageType typeMessage = MessageType.TEXTE;

    // Note: Database schema doesn't have fichier_url, created_at, updated_at, version columns
    // File attachments are not supported in current schema

    // Constructors
    public Message() {}

    public Message(Long conversationId, Long expediteurId, String contenu, MessageType typeMessage) {
        this.conversationId = conversationId;
        this.expediteurId = expediteurId;
        this.contenu = contenu;
        this.typeMessage = typeMessage;
        this.dateEnvoi = LocalDateTime.now();
        this.lu = false;
    }

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

    // Business methods
    public void marquerCommeLu() {
        this.lu = true;
    }

    public boolean isTexte() {
        return typeMessage == MessageType.TEXTE;
    }

    public boolean isFichier() {
        return typeMessage == MessageType.FICHIER || typeMessage == MessageType.IMAGE;
    }

    @Override
    public String toString() {
        return "Message{" +
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