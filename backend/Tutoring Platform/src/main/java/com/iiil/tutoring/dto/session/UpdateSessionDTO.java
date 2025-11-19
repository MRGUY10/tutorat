package com.iiil.tutoring.dto.session;

import com.iiil.tutoring.enums.SessionStatus;
import com.iiil.tutoring.enums.SessionType;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * DTO for updating an existing session
 */
public class UpdateSessionDTO {

    @Future(message = "La date et heure doivent être dans le futur")
    private LocalDateTime dateHeure;

    @Min(value = 30, message = "La durée minimum est de 30 minutes")
    @Max(value = 480, message = "La durée maximum est de 8 heures (480 minutes)")
    private Integer duree;

    private SessionStatus statut;

    @DecimalMin(value = "0.0", message = "Le prix doit être positif")
    @DecimalMax(value = "1000.0", message = "Le prix ne peut pas dépasser 1000€")
    private Double prix;

    private SessionType typeSession;

    @Size(max = 500, message = "Le lien visio ne peut pas dépasser 500 caractères")
    private String lienVisio;

    @Size(max = 1000, message = "Les notes ne peuvent pas dépasser 1000 caractères")
    private String notes;

    @Size(max = 200, message = "La salle ne peut pas dépasser 200 caractères")
    private String salle;

    // Additional fields for status changes
    @Size(max = 1000, message = "La raison de modification ne peut pas dépasser 1000 caractères")
    private String raisonModification;

    private Boolean sendNotificationToStudent;
    private Boolean sendNotificationToTutor;

    // Session feedback (for completed sessions)
    @Size(max = 2000, message = "Le feedback ne peut pas dépasser 2000 caractères")
    private String feedbackTuteur;

    @Size(max = 2000, message = "Le feedback ne peut pas dépasser 2000 caractères")
    private String feedbackEtudiant;

    @Min(value = 1, message = "La note doit être entre 1 et 5")
    @Max(value = 5, message = "La note doit être entre 1 et 5")
    private Integer noteTuteur; // Note donnée par l'étudiant au tuteur

    @Min(value = 1, message = "La note doit être entre 1 et 5")
    @Max(value = 5, message = "La note doit être entre 1 et 5")
    private Integer noteEtudiant; // Note donnée par le tuteur à l'étudiant

    // Reschedule options
    private LocalDateTime nouvelleDateProposee1;
    private LocalDateTime nouvelleDateProposee2;
    private LocalDateTime nouvelleDateProposee3;

    // Session summary for completed sessions
    private String resumeSeance;
    private String objectifsAtteints;
    private String recommandations;
    private String prochaigesEtapes;

    // Constructors
    public UpdateSessionDTO() {}

    // Getters and Setters
    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public Integer getDuree() {
        return duree;
    }

    public void setDuree(Integer duree) {
        this.duree = duree;
    }

    public SessionStatus getStatut() {
        return statut;
    }

    public void setStatut(SessionStatus statut) {
        this.statut = statut;
    }

    public Double getPrix() {
        return prix;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
    }

    public SessionType getTypeSession() {
        return typeSession;
    }

    public void setTypeSession(SessionType typeSession) {
        this.typeSession = typeSession;
    }

    public String getLienVisio() {
        return lienVisio;
    }

    public void setLienVisio(String lienVisio) {
        this.lienVisio = lienVisio;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getSalle() {
        return salle;
    }

    public void setSalle(String salle) {
        this.salle = salle;
    }

    public String getRaisonModification() {
        return raisonModification;
    }

    public void setRaisonModification(String raisonModification) {
        this.raisonModification = raisonModification;
    }

    public Boolean getSendNotificationToStudent() {
        return sendNotificationToStudent;
    }

    public void setSendNotificationToStudent(Boolean sendNotificationToStudent) {
        this.sendNotificationToStudent = sendNotificationToStudent;
    }

    public Boolean getSendNotificationToTutor() {
        return sendNotificationToTutor;
    }

    public void setSendNotificationToTutor(Boolean sendNotificationToTutor) {
        this.sendNotificationToTutor = sendNotificationToTutor;
    }

    public String getFeedbackTuteur() {
        return feedbackTuteur;
    }

    public void setFeedbackTuteur(String feedbackTuteur) {
        this.feedbackTuteur = feedbackTuteur;
    }

    public String getFeedbackEtudiant() {
        return feedbackEtudiant;
    }

    public void setFeedbackEtudiant(String feedbackEtudiant) {
        this.feedbackEtudiant = feedbackEtudiant;
    }

    public Integer getNoteTuteur() {
        return noteTuteur;
    }

    public void setNoteTuteur(Integer noteTuteur) {
        this.noteTuteur = noteTuteur;
    }

    public Integer getNoteEtudiant() {
        return noteEtudiant;
    }

    public void setNoteEtudiant(Integer noteEtudiant) {
        this.noteEtudiant = noteEtudiant;
    }

    public LocalDateTime getNouvelleDateProposee1() {
        return nouvelleDateProposee1;
    }

    public void setNouvelleDateProposee1(LocalDateTime nouvelleDateProposee1) {
        this.nouvelleDateProposee1 = nouvelleDateProposee1;
    }

    public LocalDateTime getNouvelleDateProposee2() {
        return nouvelleDateProposee2;
    }

    public void setNouvelleDateProposee2(LocalDateTime nouvelleDateProposee2) {
        this.nouvelleDateProposee2 = nouvelleDateProposee2;
    }

    public LocalDateTime getNouvelleDateProposee3() {
        return nouvelleDateProposee3;
    }

    public void setNouvelleDateProposee3(LocalDateTime nouvelleDateProposee3) {
        this.nouvelleDateProposee3 = nouvelleDateProposee3;
    }

    public String getResumeSeance() {
        return resumeSeance;
    }

    public void setResumeSeance(String resumeSeance) {
        this.resumeSeance = resumeSeance;
    }

    public String getObjectifsAtteints() {
        return objectifsAtteints;
    }

    public void setObjectifsAtteints(String objectifsAtteints) {
        this.objectifsAtteints = objectifsAtteints;
    }

    public String getRecommandations() {
        return recommandations;
    }

    public void setRecommandations(String recommandations) {
        this.recommandations = recommandations;
    }

    public String getProchaigesEtapes() {
        return prochaigesEtapes;
    }

    public void setProchaigesEtapes(String prochaigesEtapes) {
        this.prochaigesEtapes = prochaigesEtapes;
    }

    @Override
    public String toString() {
        return "UpdateSessionDTO{" +
                "dateHeure=" + dateHeure +
                ", duree=" + duree +
                ", statut=" + statut +
                ", prix=" + prix +
                ", typeSession=" + typeSession +
                '}';
    }
}