package com.iiil.tutoring.dto.evaluation;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for evaluation summary with aggregated statistics
 */
public class EvaluationSummaryDTO {

    private Long userId;
    private String userNom;
    private String userPrenom;
    private String userType; // "TUTEUR" or "ETUDIANT"

    // Overall rating statistics
    private Double noteGlobale;
    private Long nombreEvaluations;
    private Integer note5Etoiles;
    private Integer note4Etoiles;
    private Integer note3Etoiles;
    private Integer note2Etoiles;
    private Integer note1Etoile;

    // Detailed criteria averages (for tutors)
    private Double moyenneQualiteEnseignement;
    private Double moyenneCommunication;
    private Double moyennePonctualite;
    private Double moyennePreparation;
    private Double moyennePatience;

    // Recommendation statistics
    private Long nombreRecommandations;
    private Double pourcentageRecommandations;

    // Recent evaluations
    private List<EvaluationResponseDTO> evaluationsRecentes;

    // Time-based statistics
    private LocalDateTime derniereEvaluation;
    private LocalDateTime premiereEvaluation;

    // Improvement trends
    private Double tendanceDernierMois;
    private Double tendanceDernierTrimestre;

    // Additional statistics
    private Long nombreSessionsEvaluees;
    private Double noteMoyenneParMatiere;

    // Constructors
    public EvaluationSummaryDTO() {}

    public EvaluationSummaryDTO(Long userId, String userNom, String userPrenom, String userType) {
        this.userId = userId;
        this.userNom = userNom;
        this.userPrenom = userPrenom;
        this.userType = userType;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserNom() {
        return userNom;
    }

    public void setUserNom(String userNom) {
        this.userNom = userNom;
    }

    public String getUserPrenom() {
        return userPrenom;
    }

    public void setUserPrenom(String userPrenom) {
        this.userPrenom = userPrenom;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Double getNoteGlobale() {
        return noteGlobale;
    }

    public void setNoteGlobale(Double noteGlobale) {
        this.noteGlobale = noteGlobale;
    }

    public Long getNombreEvaluations() {
        return nombreEvaluations;
    }

    public void setNombreEvaluations(Long nombreEvaluations) {
        this.nombreEvaluations = nombreEvaluations;
    }

    public Integer getNote5Etoiles() {
        return note5Etoiles;
    }

    public void setNote5Etoiles(Integer note5Etoiles) {
        this.note5Etoiles = note5Etoiles;
    }

    public Integer getNote4Etoiles() {
        return note4Etoiles;
    }

    public void setNote4Etoiles(Integer note4Etoiles) {
        this.note4Etoiles = note4Etoiles;
    }

    public Integer getNote3Etoiles() {
        return note3Etoiles;
    }

    public void setNote3Etoiles(Integer note3Etoiles) {
        this.note3Etoiles = note3Etoiles;
    }

    public Integer getNote2Etoiles() {
        return note2Etoiles;
    }

    public void setNote2Etoiles(Integer note2Etoiles) {
        this.note2Etoiles = note2Etoiles;
    }

    public Integer getNote1Etoile() {
        return note1Etoile;
    }

    public void setNote1Etoile(Integer note1Etoile) {
        this.note1Etoile = note1Etoile;
    }

    public Double getMoyenneQualiteEnseignement() {
        return moyenneQualiteEnseignement;
    }

    public void setMoyenneQualiteEnseignement(Double moyenneQualiteEnseignement) {
        this.moyenneQualiteEnseignement = moyenneQualiteEnseignement;
    }

    public Double getMoyenneCommunication() {
        return moyenneCommunication;
    }

    public void setMoyenneCommunication(Double moyenneCommunication) {
        this.moyenneCommunication = moyenneCommunication;
    }

    public Double getMoyennePonctualite() {
        return moyennePonctualite;
    }

    public void setMoyennePonctualite(Double moyennePonctualite) {
        this.moyennePonctualite = moyennePonctualite;
    }

    public Double getMoyennePreparation() {
        return moyennePreparation;
    }

    public void setMoyennePreparation(Double moyennePreparation) {
        this.moyennePreparation = moyennePreparation;
    }

    public Double getMoyennePatience() {
        return moyennePatience;
    }

    public void setMoyennePatience(Double moyennePatience) {
        this.moyennePatience = moyennePatience;
    }

    public Long getNombreRecommandations() {
        return nombreRecommandations;
    }

    public void setNombreRecommandations(Long nombreRecommandations) {
        this.nombreRecommandations = nombreRecommandations;
    }

    public Double getPourcentageRecommandations() {
        return pourcentageRecommandations;
    }

    public void setPourcentageRecommandations(Double pourcentageRecommandations) {
        this.pourcentageRecommandations = pourcentageRecommandations;
    }

    public List<EvaluationResponseDTO> getEvaluationsRecentes() {
        return evaluationsRecentes;
    }

    public void setEvaluationsRecentes(List<EvaluationResponseDTO> evaluationsRecentes) {
        this.evaluationsRecentes = evaluationsRecentes;
    }

    public LocalDateTime getDerniereEvaluation() {
        return derniereEvaluation;
    }

    public void setDerniereEvaluation(LocalDateTime derniereEvaluation) {
        this.derniereEvaluation = derniereEvaluation;
    }

    public LocalDateTime getPremiereEvaluation() {
        return premiereEvaluation;
    }

    public void setPremiereEvaluation(LocalDateTime premiereEvaluation) {
        this.premiereEvaluation = premiereEvaluation;
    }

    public Double getTendanceDernierMois() {
        return tendanceDernierMois;
    }

    public void setTendanceDernierMois(Double tendanceDernierMois) {
        this.tendanceDernierMois = tendanceDernierMois;
    }

    public Double getTendanceDernierTrimestre() {
        return tendanceDernierTrimestre;
    }

    public void setTendanceDernierTrimestre(Double tendanceDernierTrimestre) {
        this.tendanceDernierTrimestre = tendanceDernierTrimestre;
    }

    public Long getNombreSessionsEvaluees() {
        return nombreSessionsEvaluees;
    }

    public void setNombreSessionsEvaluees(Long nombreSessionsEvaluees) {
        this.nombreSessionsEvaluees = nombreSessionsEvaluees;
    }

    public Double getNoteMoyenneParMatiere() {
        return noteMoyenneParMatiere;
    }

    public void setNoteMoyenneParMatiere(Double noteMoyenneParMatiere) {
        this.noteMoyenneParMatiere = noteMoyenneParMatiere;
    }

    // Helper methods
    public String getUserFullName() {
        return userPrenom + " " + userNom;
    }

    public boolean isTutor() {
        return "TUTEUR".equals(userType);
    }

    public boolean isStudent() {
        return "ETUDIANT".equals(userType);
    }

    public boolean hasEvaluations() {
        return nombreEvaluations != null && nombreEvaluations > 0;
    }

    public String getRatingLevel() {
        if (noteGlobale == null) {
            return "Non évalué";
        }
        if (noteGlobale >= 4.5) {
            return "Excellent";
        } else if (noteGlobale >= 3.5) {
            return "Très bien";
        } else if (noteGlobale >= 2.5) {
            return "Bien";
        } else if (noteGlobale >= 1.5) {
            return "Moyen";
        } else {
            return "À améliorer";
        }
    }

    public double getPositiveRatingPercentage() {
        if (nombreEvaluations == null || nombreEvaluations == 0) {
            return 0.0;
        }
        int positiveRatings = (note4Etoiles != null ? note4Etoiles : 0) + 
                             (note5Etoiles != null ? note5Etoiles : 0);
        return (positiveRatings * 100.0) / nombreEvaluations;
    }

    public boolean isHighlyRated() {
        return noteGlobale != null && noteGlobale >= 4.0 && nombreEvaluations >= 5;
    }

    public boolean hasDetailedCriteria() {
        return moyenneQualiteEnseignement != null || moyenneCommunication != null ||
               moyennePonctualite != null || moyennePreparation != null || moyennePatience != null;
    }

    public String getTrendDescription() {
        if (tendanceDernierMois == null) {
            return "Tendance indisponible";
        }
        if (tendanceDernierMois > 0.1) {
            return "En amélioration";
        } else if (tendanceDernierMois < -0.1) {
            return "En baisse";
        } else {
            return "Stable";
        }
    }

    @Override
    public String toString() {
        return "EvaluationSummaryDTO{" +
                "userId=" + userId +
                ", userFullName='" + getUserFullName() + '\'' +
                ", userType='" + userType + '\'' +
                ", noteGlobale=" + noteGlobale +
                ", nombreEvaluations=" + nombreEvaluations +
                '}';
    }
}