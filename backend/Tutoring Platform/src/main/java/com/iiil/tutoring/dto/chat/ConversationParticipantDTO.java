package com.iiil.tutoring.dto.chat;

import java.time.LocalDateTime;

/**
 * DTO for conversation participant information
 */
public class ConversationParticipantDTO {

    private Long id;
    private Long conversationId;
    private Long userId;
    private LocalDateTime dateRejoint;
    private LocalDateTime derniereVisite;

    // User information
    private String nom;
    private String prenom;
    private String email;
    private String roleUtilisateur; // "TUTEUR", "ETUDIANT", "ADMIN"

    // Status flags
    private boolean isOnline;
    private boolean isTyping;
    private boolean hasLeft; // For group conversations

    // Constructors
    public ConversationParticipantDTO() {}

    public ConversationParticipantDTO(Long userId, String nom, String prenom, String email) {
        this.userId = userId;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
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

    public LocalDateTime getDateRejoint() {
        return dateRejoint;
    }

    public void setDateRejoint(LocalDateTime dateRejoint) {
        this.dateRejoint = dateRejoint;
    }

    public LocalDateTime getDerniereVisite() {
        return derniereVisite;
    }

    public void setDerniereVisite(LocalDateTime derniereVisite) {
        this.derniereVisite = derniereVisite;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoleUtilisateur() {
        return roleUtilisateur;
    }

    public void setRoleUtilisateur(String roleUtilisateur) {
        this.roleUtilisateur = roleUtilisateur;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public void setTyping(boolean typing) {
        isTyping = typing;
    }

    public boolean isHasLeft() {
        return hasLeft;
    }

    public void setHasLeft(boolean hasLeft) {
        this.hasLeft = hasLeft;
    }

    // Helper methods
    public String getFullName() {
        return prenom + " " + nom;
    }

    public boolean isActive() {
        return !hasLeft;
    }

    public boolean isTuteur() {
        return "TUTEUR".equals(roleUtilisateur);
    }

    public boolean isEtudiant() {
        return "ETUDIANT".equals(roleUtilisateur);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(roleUtilisateur);
    }

    public String getDisplayName() {
        String fullName = getFullName();
        if (isTuteur()) {
            return fullName + " (Tuteur)";
        } else if (isAdmin()) {
            return fullName + " (Admin)";
        }
        return fullName;
    }

    @Override
    public String toString() {
        return "ConversationParticipantDTO{" +
                "id=" + id +
                ", conversationId=" + conversationId +
                ", userId=" + userId +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", roleUtilisateur='" + roleUtilisateur + '\'' +
                ", isOnline=" + isOnline +
                '}';
    }
}