package com.iiil.tutoring.dto.evaluation;

import com.iiil.tutoring.enums.EvaluationType;

import java.time.LocalDateTime;

/**
 * DTO for evaluation response with complete information
 */
public class EvaluationResponseDTO {

    private Long id;
    private Long sessionId;
    private Long evaluateurId;
    private Long evalueId;
    private Integer note;
    private String commentaire;
    private LocalDateTime date;
    private EvaluationType typeEvaluation;

    // Evaluateur information
    private String evaluateurNom;
    private String evaluateurPrenom;
    private String evaluateurEmail;

    // Evalué information
    private String evalueNom;
    private String evaluePrenom;
    private String evalueEmail;

    // Session information
    private String sessionMatiere;
    private LocalDateTime sessionDate;
    private Integer sessionDuree;

    // Detailed criteria (if available)
    private Integer qualiteEnseignement;
    private Integer communication;
    private Integer ponctualite;
    private Integer preparation;
    private Integer patience;
    private Boolean recommanderais;

    // Constructors
    public EvaluationResponseDTO() {}

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

    public String getEvaluateurNom() {
        return evaluateurNom;
    }

    public void setEvaluateurNom(String evaluateurNom) {
        this.evaluateurNom = evaluateurNom;
    }

    public String getEvaluateurPrenom() {
        return evaluateurPrenom;
    }

    public void setEvaluateurPrenom(String evaluateurPrenom) {
        this.evaluateurPrenom = evaluateurPrenom;
    }

    public String getEvaluateurEmail() {
        return evaluateurEmail;
    }

    public void setEvaluateurEmail(String evaluateurEmail) {
        this.evaluateurEmail = evaluateurEmail;
    }

    public String getEvalueNom() {
        return evalueNom;
    }

    public void setEvalueNom(String evalueNom) {
        this.evalueNom = evalueNom;
    }

    public String getEvaluePrenom() {
        return evaluePrenom;
    }

    public void setEvaluePrenom(String evaluePrenom) {
        this.evaluePrenom = evaluePrenom;
    }

    public String getEvalueEmail() {
        return evalueEmail;
    }

    public void setEvalueEmail(String evalueEmail) {
        this.evalueEmail = evalueEmail;
    }

    public String getSessionMatiere() {
        return sessionMatiere;
    }

    public void setSessionMatiere(String sessionMatiere) {
        this.sessionMatiere = sessionMatiere;
    }

    public LocalDateTime getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDateTime sessionDate) {
        this.sessionDate = sessionDate;
    }

    public Integer getSessionDuree() {
        return sessionDuree;
    }

    public void setSessionDuree(Integer sessionDuree) {
        this.sessionDuree = sessionDuree;
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

    // Helper methods
    public String getEvaluateurFullName() {
        return evaluateurPrenom + " " + evaluateurNom;
    }

    public String getEvalueFullName() {
        return evaluePrenom + " " + evalueNom;
    }

    public boolean isPositive() {
        return note != null && note >= 4;
    }

    public boolean isNegative() {
        return note != null && note <= 2;
    }

    public boolean isForTutor() {
        return typeEvaluation == EvaluationType.ETUDIANT_VERS_TUTEUR;
    }

    public boolean isForStudent() {
        return typeEvaluation == EvaluationType.TUTEUR_VERS_ETUDIANT;
    }

    public boolean hasComment() {
        return commentaire != null && !commentaire.trim().isEmpty();
    }

    public boolean hasDetailedCriteria() {
        return qualiteEnseignement != null || communication != null || 
               ponctualite != null || preparation != null || patience != null;
    }

    public String getCommentairePreview() {
        if (commentaire == null) {
            return "Aucun commentaire";
        }
        return commentaire.length() > 100 ? commentaire.substring(0, 100) + "..." : commentaire;
    }

    @Override
    public String toString() {
        return "EvaluationResponseDTO{" +
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