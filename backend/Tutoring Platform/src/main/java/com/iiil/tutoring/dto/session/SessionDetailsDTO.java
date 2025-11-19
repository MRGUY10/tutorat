package com.iiil.tutoring.dto.session;

import com.iiil.tutoring.enums.SessionStatus;
import com.iiil.tutoring.enums.SessionType;
import com.iiil.tutoring.enums.UserStatus;
import com.iiil.tutoring.enums.RequestStatus;

import java.time.LocalDateTime;

/**
 * DTO for detailed session information with all related data
 */
public class SessionDetailsDTO {

    // Session details
    private Long id;
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

    // Student details
    private Long etudiantId;
    private String etudiantNom;
    private String etudiantPrenom;
    private String etudiantEmail;
    private String etudiantTelephone;
    private UserStatus etudiantStatus;

    // Tutor details
    private Long tuteurId;
    private String tuteurNom;
    private String tuteurPrenom;
    private String tuteurEmail;
    private String tuteurTelephone;
    private UserStatus tuteurStatus;
    private String tuteurSpecialite;
    private double tuteurTarifHoraire;
    private boolean tuteurVerified;

    // Subject details
    private Long matiereId;
    private String matiereNom;
    private String matiereDescription;

    // Related session request details (if applicable)
    private Long demandeSessionId;
    private RequestStatus demandeStatut;
    private LocalDateTime demandeCreatedAt;
    private String demandeMessage;

    // Session feedback and ratings
    private String feedbackTuteur;
    private String feedbackEtudiant;
    private Integer noteTuteur;
    private Integer noteEtudiant;

    // Session summary (for completed sessions)
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
    private boolean isInProgress;
    private long minutesUntilStart;
    private long minutesRemaining;
    private String statusDescription;

    // Reschedule proposals
    private LocalDateTime nouvelleDateProposee1;
    private LocalDateTime nouvelleDateProposee2;
    private LocalDateTime nouvelleDateProposee3;

    // Recurring session information
    private boolean isRecurring;
    private Long parentSessionId;
    private Integer sessionSequence;
    private Integer totalRecurringSessions;

    // Payment information
    private boolean isPaid;
    private LocalDateTime paymentDate;
    private String paymentMethod;
    private String paymentStatus;

    // Additional metadata
    private String lastModifiedBy;
    private LocalDateTime lastStatusChange;
    private int rescheduledCount;
    private boolean hasReminders;

    // Constructors
    public SessionDetailsDTO() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getEtudiantEmail() {
        return etudiantEmail;
    }

    public void setEtudiantEmail(String etudiantEmail) {
        this.etudiantEmail = etudiantEmail;
    }

    public String getEtudiantTelephone() {
        return etudiantTelephone;
    }

    public void setEtudiantTelephone(String etudiantTelephone) {
        this.etudiantTelephone = etudiantTelephone;
    }

    public UserStatus getEtudiantStatus() {
        return etudiantStatus;
    }

    public void setEtudiantStatus(UserStatus etudiantStatus) {
        this.etudiantStatus = etudiantStatus;
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

    public String getTuteurEmail() {
        return tuteurEmail;
    }

    public void setTuteurEmail(String tuteurEmail) {
        this.tuteurEmail = tuteurEmail;
    }

    public String getTuteurTelephone() {
        return tuteurTelephone;
    }

    public void setTuteurTelephone(String tuteurTelephone) {
        this.tuteurTelephone = tuteurTelephone;
    }

    public UserStatus getTuteurStatus() {
        return tuteurStatus;
    }

    public void setTuteurStatus(UserStatus tuteurStatus) {
        this.tuteurStatus = tuteurStatus;
    }

    public String getTuteurSpecialite() {
        return tuteurSpecialite;
    }

    public void setTuteurSpecialite(String tuteurSpecialite) {
        this.tuteurSpecialite = tuteurSpecialite;
    }

    public double getTuteurTarifHoraire() {
        return tuteurTarifHoraire;
    }

    public void setTuteurTarifHoraire(double tuteurTarifHoraire) {
        this.tuteurTarifHoraire = tuteurTarifHoraire;
    }

    public boolean isTuteurVerified() {
        return tuteurVerified;
    }

    public void setTuteurVerified(boolean tuteurVerified) {
        this.tuteurVerified = tuteurVerified;
    }

    public Long getMatiereId() {
        return matiereId;
    }

    public void setMatiereId(Long matiereId) {
        this.matiereId = matiereId;
    }

    public String getMatiereNom() {
        return matiereNom;
    }

    public void setMatiereNom(String matiereNom) {
        this.matiereNom = matiereNom;
    }

    public String getMatiereDescription() {
        return matiereDescription;
    }

    public void setMatiereDescription(String matiereDescription) {
        this.matiereDescription = matiereDescription;
    }

    public Long getDemandeSessionId() {
        return demandeSessionId;
    }

    public void setDemandeSessionId(Long demandeSessionId) {
        this.demandeSessionId = demandeSessionId;
    }

    public RequestStatus getDemandeStatut() {
        return demandeStatut;
    }

    public void setDemandeStatut(RequestStatus demandeStatut) {
        this.demandeStatut = demandeStatut;
    }

    public LocalDateTime getDemandeCreatedAt() {
        return demandeCreatedAt;
    }

    public void setDemandeCreatedAt(LocalDateTime demandeCreatedAt) {
        this.demandeCreatedAt = demandeCreatedAt;
    }

    public String getDemandeMessage() {
        return demandeMessage;
    }

    public void setDemandeMessage(String demandeMessage) {
        this.demandeMessage = demandeMessage;
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

    public boolean isInProgress() {
        return isInProgress;
    }

    public void setInProgress(boolean inProgress) {
        isInProgress = inProgress;
    }

    public long getMinutesUntilStart() {
        return minutesUntilStart;
    }

    public void setMinutesUntilStart(long minutesUntilStart) {
        this.minutesUntilStart = minutesUntilStart;
    }

    public long getMinutesRemaining() {
        return minutesRemaining;
    }

    public void setMinutesRemaining(long minutesRemaining) {
        this.minutesRemaining = minutesRemaining;
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

    public Integer getTotalRecurringSessions() {
        return totalRecurringSessions;
    }

    public void setTotalRecurringSessions(Integer totalRecurringSessions) {
        this.totalRecurringSessions = totalRecurringSessions;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public LocalDateTime getLastStatusChange() {
        return lastStatusChange;
    }

    public void setLastStatusChange(LocalDateTime lastStatusChange) {
        this.lastStatusChange = lastStatusChange;
    }

    public int getRescheduledCount() {
        return rescheduledCount;
    }

    public void setRescheduledCount(int rescheduledCount) {
        this.rescheduledCount = rescheduledCount;
    }

    public boolean isHasReminders() {
        return hasReminders;
    }

    public void setHasReminders(boolean hasReminders) {
        this.hasReminders = hasReminders;
    }

    @Override
    public String toString() {
        return "SessionDetailsDTO{" +
                "id=" + id +
                ", etudiantNom='" + etudiantNom + '\'' +
                ", etudiantPrenom='" + etudiantPrenom + '\'' +
                ", tuteurNom='" + tuteurNom + '\'' +
                ", tuteurPrenom='" + tuteurPrenom + '\'' +
                ", matiereNom='" + matiereNom + '\'' +
                ", dateHeure=" + dateHeure +
                ", duree=" + duree +
                ", statut=" + statut +
                ", typeSession=" + typeSession +
                ", prix=" + prix +
                '}';
    }
}