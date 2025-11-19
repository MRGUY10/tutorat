package com.iiil.tutoring.dto.session;

import com.iiil.tutoring.enums.RequestStatus;
import com.iiil.tutoring.enums.Urgence;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * DTO for updating an existing session request
 */
public class UpdateSessionRequestDTO {

    @Future(message = "La date souhaitée doit être dans le futur")
    private LocalDateTime dateVoulue;

    @Size(min = 10, max = 1000, message = "Le message doit contenir entre 10 et 1000 caractères")
    private String message;

    private Urgence urgence;

    @Min(value = 30, message = "La durée minimum est de 30 minutes")
    @Max(value = 480, message = "La durée maximum est de 8 heures (480 minutes)")
    private Integer dureeSouhaitee;

    @DecimalMin(value = "0.0", inclusive = false, message = "Le budget maximum doit être supérieur à 0")
    @DecimalMax(value = "1000.0", message = "Le budget maximum ne peut pas dépasser 1000€")
    private Double budgetMax;

    @Size(max = 500, message = "Les notes additionnelles ne peuvent pas dépasser 500 caractères")
    private String notesAdditionnelles;

    private Boolean flexibleSurDate;

    private Boolean accepteSeanceEnLigne;

    private Boolean accepteSeancePresentiel;

    // Tutor response fields
    @Size(max = 1000, message = "La réponse du tuteur ne peut pas dépasser 1000 caractères")
    private String reponseTuteur;

    private RequestStatus statut;

    // Proposed alternatives by tutor
    private LocalDateTime dateAlternative1;
    private LocalDateTime dateAlternative2;
    private LocalDateTime dateAlternative3;

    @DecimalMin(value = "0.0", message = "Le prix proposé doit être positif")
    private Double prixPropose;

    @Min(value = 30, message = "La durée proposée minimum est de 30 minutes")
    private Integer dureeProposee;

    // Constructors
    public UpdateSessionRequestDTO() {}

    // Getters and Setters
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

    public Integer getDureeSouhaitee() {
        return dureeSouhaitee;
    }

    public void setDureeSouhaitee(Integer dureeSouhaitee) {
        this.dureeSouhaitee = dureeSouhaitee;
    }

    public Double getBudgetMax() {
        return budgetMax;
    }

    public void setBudgetMax(Double budgetMax) {
        this.budgetMax = budgetMax;
    }

    public String getNotesAdditionnelles() {
        return notesAdditionnelles;
    }

    public void setNotesAdditionnelles(String notesAdditionnelles) {
        this.notesAdditionnelles = notesAdditionnelles;
    }

    public Boolean getFlexibleSurDate() {
        return flexibleSurDate;
    }

    public void setFlexibleSurDate(Boolean flexibleSurDate) {
        this.flexibleSurDate = flexibleSurDate;
    }

    public Boolean getAccepteSeanceEnLigne() {
        return accepteSeanceEnLigne;
    }

    public void setAccepteSeanceEnLigne(Boolean accepteSeanceEnLigne) {
        this.accepteSeanceEnLigne = accepteSeanceEnLigne;
    }

    public Boolean getAccepteSeancePresentiel() {
        return accepteSeancePresentiel;
    }

    public void setAccepteSeancePresentiel(Boolean accepteSeancePresentiel) {
        this.accepteSeancePresentiel = accepteSeancePresentiel;
    }

    public String getReponseTuteur() {
        return reponseTuteur;
    }

    public void setReponseTuteur(String reponseTuteur) {
        this.reponseTuteur = reponseTuteur;
    }

    public RequestStatus getStatut() {
        return statut;
    }

    public void setStatut(RequestStatus statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateAlternative1() {
        return dateAlternative1;
    }

    public void setDateAlternative1(LocalDateTime dateAlternative1) {
        this.dateAlternative1 = dateAlternative1;
    }

    public LocalDateTime getDateAlternative2() {
        return dateAlternative2;
    }

    public void setDateAlternative2(LocalDateTime dateAlternative2) {
        this.dateAlternative2 = dateAlternative2;
    }

    public LocalDateTime getDateAlternative3() {
        return dateAlternative3;
    }

    public void setDateAlternative3(LocalDateTime dateAlternative3) {
        this.dateAlternative3 = dateAlternative3;
    }

    public Double getPrixPropose() {
        return prixPropose;
    }

    public void setPrixPropose(Double prixPropose) {
        this.prixPropose = prixPropose;
    }

    public Integer getDureeProposee() {
        return dureeProposee;
    }

    public void setDureeProposee(Integer dureeProposee) {
        this.dureeProposee = dureeProposee;
    }

    @Override
    public String toString() {
        return "UpdateSessionRequestDTO{" +
                "dateVoulue=" + dateVoulue +
                ", urgence=" + urgence +
                ", statut=" + statut +
                ", prixPropose=" + prixPropose +
                '}';
    }
}