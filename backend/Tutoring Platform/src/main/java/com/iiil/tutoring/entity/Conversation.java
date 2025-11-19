package com.iiil.tutoring.entity;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Conversation entity
 */
@Table("conversations")
public class Conversation {

    @Id
    private Long id;

    @NotBlank(message = "Le sujet est obligatoire")
    @Column("nom")
    private String sujet;

    @Column("date_creation")
    @CreatedDate
    private LocalDateTime dateCreation;

    @Column("archivee")
    private boolean archivee = false;

    // Note: Database schema doesn't have created_at, updated_at, version columns
    // Using date_creation from schema

    // Constructors
    public Conversation() {}

    public Conversation(String sujet) {
        this.sujet = sujet;
        this.dateCreation = LocalDateTime.now();
        this.archivee = false;
    }

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

    // Business methods
    public void archiver() {
        this.archivee = true;
    }

    public void desarchivers() {
        this.archivee = false;
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                ", sujet='" + sujet + '\'' +
                ", dateCreation=" + dateCreation +
                ", archivee=" + archivee +
                '}';
    }
}