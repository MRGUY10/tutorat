package com.iiil.tutoring.entity;

import com.iiil.tutoring.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Notification entity
 */
@Table("notifications")
public class Notification {

    @Id
    private Long id;

    @NotNull(message = "L'ID de l'utilisateur est obligatoire")
    @Column("user_id")
    private Long userId;

    @NotBlank(message = "Le titre est obligatoire")
    @Column("titre")
    private String titre;

    @NotBlank(message = "Le contenu est obligatoire")
    @Column("contenu")
    private String contenu;

    @Column("date_creation")
    @CreatedDate
    private LocalDateTime dateCreation;

    @Column("lue")
    private boolean lue = false;

    @NotNull(message = "Le type est obligatoire")
    @Column("type")
    private NotificationType type;

    @Column("entity_id")
    private Long entityId; // ID de l'entité liée (session, message, etc.)

    @Column("action_url")
    private String actionUrl; // URL vers laquelle rediriger l'utilisateur

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Constructors
    public Notification() {}

    public Notification(Long userId, String titre, String contenu, NotificationType type) {
        this.userId = userId;
        this.titre = titre;
        this.contenu = contenu;
        this.type = type;
        this.dateCreation = LocalDateTime.now();
        this.lue = false;
    }

    public Notification(Long userId, String titre, String contenu, NotificationType type, 
                       Long entityId, String actionUrl) {
        this(userId, titre, contenu, type);
        this.entityId = entityId;
        this.actionUrl = actionUrl;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public boolean isLue() {
        return lue;
    }

    public void setLue(boolean lue) {
        this.lue = lue;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // Business methods
    public void marquerCommeLue() {
        this.lue = true;
    }

    public boolean isNonLue() {
        return !lue;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", userId=" + userId +
                ", titre='" + titre + '\'' +
                ", contenu='" + contenu + '\'' +
                ", dateCreation=" + dateCreation +
                ", lue=" + lue +
                ", type=" + type +
                '}';
    }
}