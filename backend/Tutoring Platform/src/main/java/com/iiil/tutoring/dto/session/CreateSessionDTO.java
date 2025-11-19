package com.iiil.tutoring.dto.session;

import com.iiil.tutoring.enums.SessionType;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * DTO for creating a new session
 */
public class CreateSessionDTO {

    @NotNull(message = "L'ID du tuteur est obligatoire")
    private Long tuteurId;

    @NotNull(message = "L'ID de l'étudiant est obligatoire")
    private Long etudiantId;

    @NotNull(message = "L'ID de la matière est obligatoire")
    private Long matiereId;

    private Long demandeSessionId; // Optional - if session is created from a request

    @NotNull(message = "La date et heure sont obligatoires")
    @Future(message = "La date et heure doivent être dans le futur")
    private LocalDateTime dateHeure;

    @Min(value = 30, message = "La durée minimum est de 30 minutes")
    @Max(value = 480, message = "La durée maximum est de 8 heures (480 minutes)")
    private int duree;

    @NotNull(message = "Le type de session est obligatoire")
    private SessionType typeSession;

    @DecimalMin(value = "0.0", message = "Le prix doit être positif")
    @DecimalMax(value = "1000.0", message = "Le prix ne peut pas dépasser 1000€")
    private double prix;

    @Size(max = 500, message = "Le lien visio ne peut pas dépasser 500 caractères")
    private String lienVisio;

    @Size(max = 1000, message = "Les notes ne peuvent pas dépasser 1000 caractères")
    private String notes;

    @Size(max = 200, message = "La salle ne peut pas dépasser 200 caractères")
    private String salle;

    // Additional fields for enhanced functionality
    private boolean requiresConfirmation = true;
    private boolean sendNotifications = true;
    private String sessionDescription;
    private String preparationNotes;

    // Recurring session options
    private boolean isRecurring = false;
    private Integer recurrenceWeeks; // Number of weeks to repeat
    private String recurrencePattern; // e.g., "WEEKLY", "BIWEEKLY"

    // Constructors
    public CreateSessionDTO() {}

    public CreateSessionDTO(Long tuteurId, Long etudiantId, Long matiereId, 
                           LocalDateTime dateHeure, int duree, SessionType typeSession, double prix) {
        this.tuteurId = tuteurId;
        this.etudiantId = etudiantId;
        this.matiereId = matiereId;
        this.dateHeure = dateHeure;
        this.duree = duree;
        this.typeSession = typeSession;
        this.prix = prix;
    }

    // Getters and Setters
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

    public SessionType getTypeSession() {
        return typeSession;
    }

    public void setTypeSession(SessionType typeSession) {
        this.typeSession = typeSession;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
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

    public boolean isRequiresConfirmation() {
        return requiresConfirmation;
    }

    public void setRequiresConfirmation(boolean requiresConfirmation) {
        this.requiresConfirmation = requiresConfirmation;
    }

    public boolean isSendNotifications() {
        return sendNotifications;
    }

    public void setSendNotifications(boolean sendNotifications) {
        this.sendNotifications = sendNotifications;
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

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public Integer getRecurrenceWeeks() {
        return recurrenceWeeks;
    }

    public void setRecurrenceWeeks(Integer recurrenceWeeks) {
        this.recurrenceWeeks = recurrenceWeeks;
    }

    public String getRecurrencePattern() {
        return recurrencePattern;
    }

    public void setRecurrencePattern(String recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }

    @Override
    public String toString() {
        return "CreateSessionDTO{" +
                "tuteurId=" + tuteurId +
                ", etudiantId=" + etudiantId +
                ", matiereId=" + matiereId +
                ", dateHeure=" + dateHeure +
                ", duree=" + duree +
                ", typeSession=" + typeSession +
                ", prix=" + prix +
                '}';
    }
}