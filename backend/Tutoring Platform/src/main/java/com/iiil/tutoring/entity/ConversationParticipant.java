package com.iiil.tutoring.entity;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Junction table for Conversation-User many-to-many relationship
 */
@Table("conversation_participants")
public class ConversationParticipant {

    @Id
    private Long id;

    @NotNull(message = "L'ID de la conversation est obligatoire")
    @Column("conversation_id")
    private Long conversationId;

    @NotNull(message = "L'ID de l'utilisateur est obligatoire")
    @Column("user_id")
    private Long userId;

    @Column("role")
    private String role = "PARTICIPANT";

    @Column("date_adhesion")
    @CreatedDate
    private LocalDateTime dateAdhesion;

    @Column("active")
    private Boolean active = true;

    // Constructors
    public ConversationParticipant() {}

    public ConversationParticipant(Long conversationId, Long userId) {
        this.conversationId = conversationId;
        this.userId = userId;
        this.dateAdhesion = LocalDateTime.now();
        this.active = true;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getDateAdhesion() {
        return dateAdhesion;
    }

    public void setDateAdhesion(LocalDateTime dateAdhesion) {
        this.dateAdhesion = dateAdhesion;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active != null && active;
    }

    // Business methods
    public void quitter() {
        this.active = false;
    }

    public void rejoindre() {
        this.active = true;
    }

    @Override
    public String toString() {
        return "ConversationParticipant{" +
                "id=" + id +
                ", conversationId=" + conversationId +
                ", userId=" + userId +
                ", role='" + role + '\'' +
                ", dateAdhesion=" + dateAdhesion +
                ", active=" + active +
                '}';
    }
}