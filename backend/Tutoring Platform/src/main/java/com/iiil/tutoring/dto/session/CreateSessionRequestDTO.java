package com.iiil.tutoring.dto.session;

import com.iiil.tutoring.enums.Urgence;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * DTO for creating a new session request
 */
public class CreateSessionRequestDTO {

    @NotNull(message = "L'ID du tuteur est obligatoire")
    private Long tuteurId;

    @NotNull(message = "L'ID de la matière est obligatoire")
    private Long matiereId;

    @NotNull(message = "La date souhaitée est obligatoire")
    @Future(message = "La date souhaitée doit être dans le futur")
    private LocalDateTime dateVoulue;

    @NotBlank(message = "Le message est obligatoire")
    @Size(min = 10, max = 1000, message = "Le message doit contenir entre 10 et 1000 caractères")
    private String message;

    @NotNull(message = "L'urgence est obligatoire")
    private Urgence urgence;

    @Min(value = 30, message = "La durée minimum est de 30 minutes")
    @Max(value = 480, message = "La durée maximum est de 8 heures (480 minutes)")
    private int dureeSouhaitee;

    @DecimalMin(value = "0.0", inclusive = false, message = "Le budget maximum doit être supérieur à 0")
    @DecimalMax(value = "1000.0", message = "Le budget maximum ne peut pas dépasser 1000€")
    private double budgetMax;

    @Size(max = 500, message = "Les notes additionnelles ne peuvent pas dépasser 500 caractères")
    private String notesAdditionnelles;

    private boolean flexibleSurDate; // Si l'étudiant est flexible sur la date

    private boolean accepteSeanceEnLigne; // Si l'étudiant accepte les séances en ligne

    private boolean accepteSeancePresentiel; // Si l'étudiant accepte les séances en présentiel

    // Constructors
    public CreateSessionRequestDTO() {}

    public CreateSessionRequestDTO(Long tuteurId, Long matiereId, LocalDateTime dateVoulue, 
                                  String message, Urgence urgence, int dureeSouhaitee, double budgetMax) {
        this.tuteurId = tuteurId;
        this.matiereId = matiereId;
        this.dateVoulue = dateVoulue;
        this.message = message;
        this.urgence = urgence;
        this.dureeSouhaitee = dureeSouhaitee;
        this.budgetMax = budgetMax;
        this.accepteSeanceEnLigne = true;
        this.accepteSeancePresentiel = true;
    }

    // Getters and Setters
    public Long getTuteurId() {
        return tuteurId;
    }

    public void setTuteurId(Long tuteurId) {
        this.tuteurId = tuteurId;
    }

    public Long getMatiereId() {
        return matiereId;
    }

    public void setMatiereId(Long matiereId) {
        this.matiereId = matiereId;
    }

    public LocalDateTime getDateVoulue() {
        return dateVoulue;
    }

    public void setDateVoulue(LocalDateTime dateVoulue) {
        this.dateVoulue = dateVoulue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Urgence getUrgence() {
        return urgence;
    }

    public void setUrgence(Urgence urgence) {
        this.urgence = urgence;
    }

    public int getDureeSouhaitee() {
        return dureeSouhaitee;
    }

    public void setDureeSouhaitee(int dureeSouhaitee) {
        this.dureeSouhaitee = dureeSouhaitee;
    }

    public double getBudgetMax() {
        return budgetMax;
    }

    public void setBudgetMax(double budgetMax) {
        this.budgetMax = budgetMax;
    }

    public String getNotesAdditionnelles() {
        return notesAdditionnelles;
    }

    public void setNotesAdditionnelles(String notesAdditionnelles) {
        this.notesAdditionnelles = notesAdditionnelles;
    }

    public boolean isFlexibleSurDate() {
        return flexibleSurDate;
    }

    public void setFlexibleSurDate(boolean flexibleSurDate) {
        this.flexibleSurDate = flexibleSurDate;
    }

    public boolean isAccepteSeanceEnLigne() {
        return accepteSeanceEnLigne;
    }

    public void setAccepteSeanceEnLigne(boolean accepteSeanceEnLigne) {
        this.accepteSeanceEnLigne = accepteSeanceEnLigne;
    }

    public boolean isAccepteSeancePresentiel() {
        return accepteSeancePresentiel;
    }

    public void setAccepteSeancePresentiel(boolean accepteSeancePresentiel) {
        this.accepteSeancePresentiel = accepteSeancePresentiel;
    }

    @Override
    public String toString() {
        return "CreateSessionRequestDTO{" +
                "tuteurId=" + tuteurId +
                ", matiereId=" + matiereId +
                ", dateVoulue=" + dateVoulue +
                ", urgence=" + urgence +
                ", dureeSouhaitee=" + dureeSouhaitee +
                ", budgetMax=" + budgetMax +
                '}';
    }
}