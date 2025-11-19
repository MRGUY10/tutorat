package com.iiil.tutoring.entity;

import com.iiil.tutoring.enums.EvaluationType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Evaluation entity for rating sessions and users
 */
@Table("evaluations")
public class Evaluation {

    @Id
    private Long id;

    @NotNull(message = "L'ID de la session est obligatoire")
    @Column("session_id")
    private Long sessionId;

    @NotNull(message = "L'ID de l'évaluateur est obligatoire")
    @Column("evaluateur_id")
    private Long evaluateurId; // Celui qui donne l'évaluation

    @NotNull(message = "L'ID de l'évalué est obligatoire")
    @Column("evalue_id")
    private Long evalueId; // Celui qui reçoit l'évaluation

    @Min(value = 1, message = "La note doit être comprise entre 1 et 5")
    @Max(value = 5, message = "La note doit être comprise entre 1 et 5")
    @NotNull(message = "La note est obligatoire")
    @Column("note")
    private int note;

    @Column("commentaire")
    private String commentaire;

    @Column("date")
    @CreatedDate
    private LocalDateTime date;

    @NotNull(message = "Le type d'évaluation est obligatoire")
    @Column("type_evaluation")
    private EvaluationType typeEvaluation;

    // Detailed criteria (optional)
    @Column("qualite_enseignement")
    private Integer qualiteEnseignement;

    @Column("communication")
    private Integer communication;

    @Column("ponctualite")
    private Integer ponctualite;

    @Column("preparation")
    private Integer preparation;

    @Column("patience")
    private Integer patience;

    @Column("recommanderais")
    private Boolean recommanderais;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Constructors
    public Evaluation() {}

    public Evaluation(Long sessionId, Long evaluateurId, Long evalueId, int note, 
                     String commentaire, EvaluationType typeEvaluation) {
        this.sessionId = sessionId;
        this.evaluateurId = evaluateurId;
        this.evalueId = evalueId;
        this.note = note;
        this.commentaire = commentaire;
        this.typeEvaluation = typeEvaluation;
        this.date = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getEvaluateurId() {
        return evaluateurId;
    }

    public void setEvaluateurId(Long evaluateurId) {
        this.evaluateurId = evaluateurId;
    }

    public Long getEvalueId() {
        return evalueId;
    }

    public void setEvalueId(Long evalueId) {
        this.evalueId = evalueId;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public EvaluationType getTypeEvaluation() {
        return typeEvaluation;
    }

    public void setTypeEvaluation(EvaluationType typeEvaluation) {
        this.typeEvaluation = typeEvaluation;
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

    public Integer getQualiteEnseignement() {
        return qualiteEnseignement;
    }

    public void setQualiteEnseignement(Integer qualiteEnseignement) {
        this.qualiteEnseignement = qualiteEnseignement;
    }

    public Integer getCommunication() {
        return communication;
    }

    public void setCommunication(Integer communication) {
        this.communication = communication;
    }

    public Integer getPonctualite() {
        return ponctualite;
    }

    public void setPonctualite(Integer ponctualite) {
        this.ponctualite = ponctualite;
    }

    public Integer getPreparation() {
        return preparation;
    }

    public void setPreparation(Integer preparation) {
        this.preparation = preparation;
    }

    public Integer getPatience() {
        return patience;
    }

    public void setPatience(Integer patience) {
        this.patience = patience;
    }

    public Boolean getRecommanderais() {
        return recommanderais;
    }

    public void setRecommanderais(Boolean recommanderais) {
        this.recommanderais = recommanderais;
    }

    // Business methods
    public boolean isPositive() {
        return note >= 4;
    }

    public boolean isNegative() {
        return note <= 2;
    }

    @Override
    public String toString() {
        return "Evaluation{" +
                "id=" + id +
                ", sessionId=" + sessionId +
                ", evaluateurId=" + evaluateurId +
                ", evalueId=" + evalueId +
                ", note=" + note +
                ", typeEvaluation=" + typeEvaluation +
                ", date=" + date +
                '}';
    }
}