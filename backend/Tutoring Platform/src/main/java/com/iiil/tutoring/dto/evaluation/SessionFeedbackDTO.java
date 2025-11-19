package com.iiil.tutoring.dto.evaluation;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for session feedback with evaluation context
 */
public class SessionFeedbackDTO {

    private Long sessionId;
    private LocalDateTime sessionDate;
    private Integer sessionDuree;
    private String sessionMatiere;
    private String sessionDescription;

    // Session participants
    private Long tuteurId;
    private String tuteurNom;
    private String tuteurPrenom;
    private Long etudiantId;
    private String etudiantNom;
    private String etudiantPrenom;

    // Evaluation status
    private boolean evaluationTuteurComplete;
    private boolean evaluationEtudiantComplete;
    private LocalDateTime dateEvaluationTuteur;
    private LocalDateTime dateEvaluationEtudiant;

    // Tutor evaluation (by student)
    private Integer noteTuteur;
    private String commentaireSurTuteur;
    private Integer qualiteEnseignementTuteur;
    private Integer communicationTuteur;
    private Integer ponctualiteTuteur;
    private Integer preparationTuteur;
    private Integer patienceTuteur;
    private Boolean recommanderaisTuteur;

    // Student evaluation (by tutor)
    private Integer noteEtudiant;
    private String commentaireSurEtudiant;
    private Integer motivationEtudiant;
    private Integer participationEtudiant;
    private Integer preparationEtudiant;
    private Integer comprehensionEtudiant;
    private Boolean recommanderaisEtudiant;

    // Overall session assessment
    private Double noteGlobaleSession;
    private String commentaireGlobalSession;
    private List<String> pointsPositifs;
    private List<String> pointsAmeliorer;

    // Session outcomes
    private boolean objectifsAtteints;
    private String prochaineetapes;
    private boolean sessionSuivieRecommandee;

    // Administrative feedback
    private String problemesTechniques;
    private String suggestionsAmelioration;
    private Integer satisfactionPlateforme;

    // Constructors
    public SessionFeedbackDTO() {}

    public SessionFeedbackDTO(Long sessionId, LocalDateTime sessionDate, String sessionMatiere) {
        this.sessionId = sessionId;
        this.sessionDate = sessionDate;
        this.sessionMatiere = sessionMatiere;
    }

    // Getters and Setters
    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
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

    public String getSessionMatiere() {
        return sessionMatiere;
    }

    public void setSessionMatiere(String sessionMatiere) {
        this.sessionMatiere = sessionMatiere;
    }

    public String getSessionDescription() {
        return sessionDescription;
    }

    public void setSessionDescription(String sessionDescription) {
        this.sessionDescription = sessionDescription;
    }

    public Long getTuteurId() {
        return tuteurId;
    }

    public void setTuteurId(Long tuteurId) {
        this.tuteurId = tuteurId;
    }

    public String getTuteurNom() {
        return tuteurNom;
    }

    public void setTuteurNom(String tuteurNom) {
        this.tuteurNom = tuteurNom;
    }

    public String getTuteurPrenom() {
        return tuteurPrenom;
    }

    public void setTuteurPrenom(String tuteurPrenom) {
        this.tuteurPrenom = tuteurPrenom;
    }

    public Long getEtudiantId() {
        return etudiantId;
    }

    public void setEtudiantId(Long etudiantId) {
        this.etudiantId = etudiantId;
    }

    public String getEtudiantNom() {
        return etudiantNom;
    }

    public void setEtudiantNom(String etudiantNom) {
        this.etudiantNom = etudiantNom;
    }

    public String getEtudiantPrenom() {
        return etudiantPrenom;
    }

    public void setEtudiantPrenom(String etudiantPrenom) {
        this.etudiantPrenom = etudiantPrenom;
    }

    public boolean isEvaluationTuteurComplete() {
        return evaluationTuteurComplete;
    }

    public void setEvaluationTuteurComplete(boolean evaluationTuteurComplete) {
        this.evaluationTuteurComplete = evaluationTuteurComplete;
    }

    public boolean isEvaluationEtudiantComplete() {
        return evaluationEtudiantComplete;
    }

    public void setEvaluationEtudiantComplete(boolean evaluationEtudiantComplete) {
        this.evaluationEtudiantComplete = evaluationEtudiantComplete;
    }

    public LocalDateTime getDateEvaluationTuteur() {
        return dateEvaluationTuteur;
    }

    public void setDateEvaluationTuteur(LocalDateTime dateEvaluationTuteur) {
        this.dateEvaluationTuteur = dateEvaluationTuteur;
    }

    public LocalDateTime getDateEvaluationEtudiant() {
        return dateEvaluationEtudiant;
    }

    public void setDateEvaluationEtudiant(LocalDateTime dateEvaluationEtudiant) {
        this.dateEvaluationEtudiant = dateEvaluationEtudiant;
    }

    public Integer getNoteTuteur() {
        return noteTuteur;
    }

    public void setNoteTuteur(Integer noteTuteur) {
        this.noteTuteur = noteTuteur;
    }

    public String getCommentaireSurTuteur() {
        return commentaireSurTuteur;
    }

    public void setCommentaireSurTuteur(String commentaireSurTuteur) {
        this.commentaireSurTuteur = commentaireSurTuteur;
    }

    public Integer getQualiteEnseignementTuteur() {
        return qualiteEnseignementTuteur;
    }

    public void setQualiteEnseignementTuteur(Integer qualiteEnseignementTuteur) {
        this.qualiteEnseignementTuteur = qualiteEnseignementTuteur;
    }

    public Integer getCommunicationTuteur() {
        return communicationTuteur;
    }

    public void setCommunicationTuteur(Integer communicationTuteur) {
        this.communicationTuteur = communicationTuteur;
    }

    public Integer getPonctualiteTuteur() {
        return ponctualiteTuteur;
    }

    public void setPonctualiteTuteur(Integer ponctualiteTuteur) {
        this.ponctualiteTuteur = ponctualiteTuteur;
    }

    public Integer getPreparationTuteur() {
        return preparationTuteur;
    }

    public void setPreparationTuteur(Integer preparationTuteur) {
        this.preparationTuteur = preparationTuteur;
    }

    public Integer getPatienceTuteur() {
        return patienceTuteur;
    }

    public void setPatienceTuteur(Integer patienceTuteur) {
        this.patienceTuteur = patienceTuteur;
    }

    public Boolean getRecommanderaisTuteur() {
        return recommanderaisTuteur;
    }

    public void setRecommanderaisTuteur(Boolean recommanderaisTuteur) {
        this.recommanderaisTuteur = recommanderaisTuteur;
    }

    public Integer getNoteEtudiant() {
        return noteEtudiant;
    }

    public void setNoteEtudiant(Integer noteEtudiant) {
        this.noteEtudiant = noteEtudiant;
    }

    public String getCommentaireSurEtudiant() {
        return commentaireSurEtudiant;
    }

    public void setCommentaireSurEtudiant(String commentaireSurEtudiant) {
        this.commentaireSurEtudiant = commentaireSurEtudiant;
    }

    public Integer getMotivationEtudiant() {
        return motivationEtudiant;
    }

    public void setMotivationEtudiant(Integer motivationEtudiant) {
        this.motivationEtudiant = motivationEtudiant;
    }

    public Integer getParticipationEtudiant() {
        return participationEtudiant;
    }

    public void setParticipationEtudiant(Integer participationEtudiant) {
        this.participationEtudiant = participationEtudiant;
    }

    public Integer getPreparationEtudiant() {
        return preparationEtudiant;
    }

    public void setPreparationEtudiant(Integer preparationEtudiant) {
        this.preparationEtudiant = preparationEtudiant;
    }

    public Integer getComprehensionEtudiant() {
        return comprehensionEtudiant;
    }

    public void setComprehensionEtudiant(Integer comprehensionEtudiant) {
        this.comprehensionEtudiant = comprehensionEtudiant;
    }

    public Boolean getRecommanderaisEtudiant() {
        return recommanderaisEtudiant;
    }

    public void setRecommanderaisEtudiant(Boolean recommanderaisEtudiant) {
        this.recommanderaisEtudiant = recommanderaisEtudiant;
    }

    public Double getNoteGlobaleSession() {
        return noteGlobaleSession;
    }

    public void setNoteGlobaleSession(Double noteGlobaleSession) {
        this.noteGlobaleSession = noteGlobaleSession;
    }

    public String getCommentaireGlobalSession() {
        return commentaireGlobalSession;
    }

    public void setCommentaireGlobalSession(String commentaireGlobalSession) {
        this.commentaireGlobalSession = commentaireGlobalSession;
    }

    public List<String> getPointsPositifs() {
        return pointsPositifs;
    }

    public void setPointsPositifs(List<String> pointsPositifs) {
        this.pointsPositifs = pointsPositifs;
    }

    public List<String> getPointsAmeliorer() {
        return pointsAmeliorer;
    }

    public void setPointsAmeliorer(List<String> pointsAmeliorer) {
        this.pointsAmeliorer = pointsAmeliorer;
    }

    public boolean isObjectifsAtteints() {
        return objectifsAtteints;
    }

    public void setObjectifsAtteints(boolean objectifsAtteints) {
        this.objectifsAtteints = objectifsAtteints;
    }

    public String getProchaineetapes() {
        return prochaineetapes;
    }

    public void setProchaineetapes(String prochaineetapes) {
        this.prochaineetapes = prochaineetapes;
    }

    public boolean isSessionSuivieRecommandee() {
        return sessionSuivieRecommandee;
    }

    public void setSessionSuivieRecommandee(boolean sessionSuivieRecommandee) {
        this.sessionSuivieRecommandee = sessionSuivieRecommandee;
    }

    public String getProblemesTechniques() {
        return problemesTechniques;
    }

    public void setProblemesTechniques(String problemesTechniques) {
        this.problemesTechniques = problemesTechniques;
    }

    public String getSuggestionsAmelioration() {
        return suggestionsAmelioration;
    }

    public void setSuggestionsAmelioration(String suggestionsAmelioration) {
        this.suggestionsAmelioration = suggestionsAmelioration;
    }

    public Integer getSatisfactionPlateforme() {
        return satisfactionPlateforme;
    }

    public void setSatisfactionPlateforme(Integer satisfactionPlateforme) {
        this.satisfactionPlateforme = satisfactionPlateforme;
    }

    // Helper methods
    public String getTuteurFullName() {
        return tuteurPrenom + " " + tuteurNom;
    }

    public String getEtudiantFullName() {
        return etudiantPrenom + " " + etudiantNom;
    }

    public boolean isEvaluationComplete() {
        return evaluationTuteurComplete && evaluationEtudiantComplete;
    }

    public boolean hasEvaluationPending() {
        return !evaluationTuteurComplete || !evaluationEtudiantComplete;
    }

    public String getEvaluationStatus() {
        if (isEvaluationComplete()) {
            return "Évaluation complète";
        } else if (!evaluationTuteurComplete && !evaluationEtudiantComplete) {
            return "Évaluation en attente";
        } else if (!evaluationTuteurComplete) {
            return "Évaluation tuteur en attente";
        } else {
            return "Évaluation étudiant en attente";
        }
    }

    public Double getNoteMoyenneSession() {
        if (noteTuteur == null && noteEtudiant == null) {
            return null;
        }
        if (noteTuteur == null) {
            return noteEtudiant.doubleValue();
        }
        if (noteEtudiant == null) {
            return noteTuteur.doubleValue();
        }
        return (noteTuteur + noteEtudiant) / 2.0;
    }

    public boolean isSessionSuccessful() {
        Double moyenne = getNoteMoyenneSession();
        return moyenne != null && moyenne >= 3.5 && objectifsAtteints;
    }

    public String getSessionFeedbackSummary() {
        if (!isEvaluationComplete()) {
            return "Évaluation incomplète";
        }
        if (isSessionSuccessful()) {
            return "Session réussie";
        } else {
            return "Session à améliorer";
        }
    }

    public boolean hasTechnicalIssues() {
        return problemesTechniques != null && !problemesTechniques.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "SessionFeedbackDTO{" +
                "sessionId=" + sessionId +
                ", sessionDate=" + sessionDate +
                ", sessionMatiere='" + sessionMatiere + '\'' +
                ", evaluationStatus='" + getEvaluationStatus() + '\'' +
                ", noteMoyenne=" + getNoteMoyenneSession() +
                '}';
    }
}