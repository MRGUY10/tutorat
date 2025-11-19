package com.iiil.tutoring.dto.session;

import com.iiil.tutoring.enums.SessionStatus;
import com.iiil.tutoring.enums.SessionType;

import java.time.LocalDateTime;

/**
 * DTO for session response with basic information
 */
public class SessionResponseDTO {

    private Long id;
    private Long tuteurId;
    private Long etudiantId;
    private Long matiereId;
    private Long demandeSessionId;
    private LocalDateTime dateHeure;
    private int duree;
    private SessionStatus statut;
    private double prix;
    private SessionType typeSession;
    private String lienVisio;
    private String notes;
    private String salle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Enhanced session information
    private String sessionDescription;
    private String preparationNotes;
    private String raisonModification;

    // Session feedback
    private String feedbackTuteur;
    private String feedbackEtudiant;
    private Integer noteTuteur;
    private Integer noteEtudiant;

    // Session summary
    private String resumeSeance;
    private String objectifsAtteints;
    private String recommandations;
    private String prochaigesEtapes;

    // Calculated fields
    private LocalDateTime dateFin;
    private boolean canBeModified;
    private boolean canBeCancelled;
    private boolean canBeCompleted;
    private boolean isUpcoming;
    private boolean isToday;
    private long minutesUntilStart;
    private String statusDescription;

    // Reschedule proposals
    private LocalDateTime nouvelleDateProposee1;
    private LocalDateTime nouvelleDateProposee2;
    private LocalDateTime nouvelleDateProposee3;

    // Recurring session info
    private boolean isRecurring;
    private Long parentSessionId;
    private Integer sessionSequence;

    // Constructors
    public SessionResponseDTO() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTuteurId() {
        return tuteurId;
    }

    public void setTuteurId(Long tuteurId) {
        this.tuteurId = tuteurId;
    }

    public Long getEtudiantId() {
        return etudiantId;
    }

    public void setEtudiantId(Long etudiantId) {
        this.etudiantId = etudiantId;
    }

    public Long getMatiereId() {
        return matiereId;
    }

    public void setMatiereId(Long matiereId) {
        this.matiereId = matiereId;
    }

    public Long getDemandeSessionId() {
        return demandeSessionId;
    }

    public void setDemandeSessionId(Long demandeSessionId) {
        this.demandeSessionId = demandeSessionId;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public SessionStatus getStatut() {
        return statut;
    }

    public void setStatut(SessionStatus statut) {
        this.statut = statut;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
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

    public String getSessionDescription() {
        return sessionDescription;
    }

    public void setSessionDescription(String sessionDescription) {
        this.sessionDescription = sessionDescription;
    }

    public String getPreparationNotes() {
        return preparationNotes;
    }

    public void setPreparationNotes(String preparationNotes) {
        this.preparationNotes = preparationNotes;
    }

    public String getRaisonModification() {
        return raisonModification;
    }

    public void setRaisonModification(String raisonModification) {
        this.raisonModification = raisonModification;
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

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public boolean isCanBeModified() {
        return canBeModified;
    }

    public void setCanBeModified(boolean canBeModified) {
        this.canBeModified = canBeModified;
    }

    public boolean isCanBeCancelled() {
        return canBeCancelled;
    }

    public void setCanBeCancelled(boolean canBeCancelled) {
        this.canBeCancelled = canBeCancelled;
    }

    public boolean isCanBeCompleted() {
        return canBeCompleted;
    }

    public void setCanBeCompleted(boolean canBeCompleted) {
        this.canBeCompleted = canBeCompleted;
    }

    public boolean isUpcoming() {
        return isUpcoming;
    }

    public void setUpcoming(boolean upcoming) {
        isUpcoming = upcoming;
    }

    public boolean isToday() {
        return isToday;
    }

    public void setToday(boolean today) {
        isToday = today;
    }

    public long getMinutesUntilStart() {
        return minutesUntilStart;
    }

    public void setMinutesUntilStart(long minutesUntilStart) {
        this.minutesUntilStart = minutesUntilStart;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
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

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public Long getParentSessionId() {
        return parentSessionId;
    }

    public void setParentSessionId(Long parentSessionId) {
        this.parentSessionId = parentSessionId;
    }

    public Integer getSessionSequence() {
        return sessionSequence;
    }

    public void setSessionSequence(Integer sessionSequence) {
        this.sessionSequence = sessionSequence;
    }

    @Override
    public String toString() {
        return "SessionResponseDTO{" +
                "id=" + id +
                ", tuteurId=" + tuteurId +
                ", etudiantId=" + etudiantId +
                ", dateHeure=" + dateHeure +
                ", duree=" + duree +
                ", statut=" + statut +
                ", typeSession=" + typeSession +
                ", prix=" + prix +
                '}';
    }
}