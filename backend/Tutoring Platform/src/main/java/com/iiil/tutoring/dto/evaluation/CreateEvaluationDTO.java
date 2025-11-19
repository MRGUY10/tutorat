package com.iiil.tutoring.dto.evaluation;

import com.iiil.tutoring.enums.EvaluationType;
import jakarta.validation.constraints.*;

/**
 * DTO for creating a new evaluation
 */
public class CreateEvaluationDTO {

    @NotNull(message = "L'ID de la session est obligatoire")
    private Long sessionId;

    @NotNull(message = "L'ID de l'évaluateur est obligatoire")
    private Long evaluateurId;

    @NotNull(message = "L'ID de l'évalué est obligatoire")
    private Long evalueId;

    @Min(value = 1, message = "La note doit être comprise entre 1 et 5")
    @Max(value = 5, message = "La note doit être comprise entre 1 et 5")
    @NotNull(message = "La note est obligatoire")
    private Integer note;

    @Size(max = 1000, message = "Le commentaire ne peut pas dépasser 1000 caractères")
    private String commentaire;

    @NotNull(message = "Le type d'évaluation est obligatoire")
    private EvaluationType typeEvaluation;

    // Critères d'évaluation détaillés
    @Min(value = 1, message = "La note de qualité d'enseignement doit être comprise entre 1 et 5")
    @Max(value = 5, message = "La note de qualité d'enseignement doit être comprise entre 1 et 5")
    private Integer qualiteEnseignement;

    @Min(value = 1, message = "La note de communication doit être comprise entre 1 et 5")
    @Max(value = 5, message = "La note de communication doit être comprise entre 1 et 5")
    private Integer communication;

    @Min(value = 1, message = "La note de ponctualité doit être comprise entre 1 et 5")
    @Max(value = 5, message = "La note de ponctualité doit être comprise entre 1 et 5")
    private Integer ponctualite;

    @Min(value = 1, message = "La note de préparation doit être comprise entre 1 et 5")
    @Max(value = 5, message = "La note de préparation doit être comprise entre 1 et 5")
    private Integer preparation;

    @Min(value = 1, message = "La note de patience doit être comprise entre 1 et 5")
    @Max(value = 5, message = "La note de patience doit être comprise entre 1 et 5")
    private Integer patience;

    // Recommandation
    private Boolean recommanderais;

    // Constructors
    public CreateEvaluationDTO() {}

    public CreateEvaluationDTO(Long sessionId, Long evaluateurId, Long evalueId, Integer note, 
                              String commentaire, EvaluationType typeEvaluation) {
        this.sessionId = sessionId;
        this.evaluateurId = evaluateurId;
        this.evalueId = evalueId;
        this.note = note;
        this.commentaire = commentaire;
        this.typeEvaluation = typeEvaluation;
    }

    // Getters and Setters
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

    public Integer getNote() {
        return note;
    }

    public void setNote(Integer note) {
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public EvaluationType getTypeEvaluation() {
        return typeEvaluation;
    }

    public void setTypeEvaluation(EvaluationType typeEvaluation) {
        this.typeEvaluation = typeEvaluation;
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

    // Validation methods
    public boolean isValid() {
        return sessionId != null && 
               evalueId != null && 
               note != null && note >= 1 && note <= 5 &&
               typeEvaluation != null &&
               (commentaire == null || commentaire.length() <= 1000);
    }

    public boolean isForTutor() {
        return typeEvaluation == EvaluationType.ETUDIANT_VERS_TUTEUR;
    }

    public boolean isForStudent() {
        return typeEvaluation == EvaluationType.TUTEUR_VERS_ETUDIANT;
    }

    public boolean hasDetailedCriteria() {
        return qualiteEnseignement != null || communication != null || 
               ponctualite != null || preparation != null || patience != null;
    }

    public boolean hasComment() {
        return commentaire != null && !commentaire.trim().isEmpty();
    }

    /**
     * Calculate overall note from detailed criteria if available
     */
    public Double calculateOverallFromCriteria() {
        if (!hasDetailedCriteria()) {
            return null;
        }

        int count = 0;
        int sum = 0;

        if (qualiteEnseignement != null) { sum += qualiteEnseignement; count++; }
        if (communication != null) { sum += communication; count++; }
        if (ponctualite != null) { sum += ponctualite; count++; }
        if (preparation != null) { sum += preparation; count++; }
        if (patience != null) { sum += patience; count++; }

        return count > 0 ? (double) sum / count : null;
    }

    @Override
    public String toString() {
        return "CreateEvaluationDTO{" +
                "sessionId=" + sessionId +
                ", evalueId=" + evalueId +
                ", note=" + note +
                ", typeEvaluation=" + typeEvaluation +
                ", recommanderais=" + recommanderais +
                '}';
    }
}