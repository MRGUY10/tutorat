package com.iiil.tutoring.dto.session;

import com.iiil.tutoring.enums.RequestStatus;
import com.iiil.tutoring.enums.Urgence;

import java.time.LocalDateTime;

/**
 * DTO for session request response with basic information
 */
public class SessionRequestResponseDTO {

    private Long id;
    private Long etudiantId;
    private Long tuteurId;
    private Long matiereId;
    private LocalDateTime dateCreation;
    private LocalDateTime dateVoulue;
    private String message;
    private RequestStatus statut;
    private Urgence urgence;
    private int dureeSouhaitee;
    private double budgetMax;
    private String reponseTuteur;
    private LocalDateTime dateReponse;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // New fields for enhanced functionality
    private String notesAdditionnelles;
    private boolean flexibleSurDate;
    private boolean accepteSeanceEnLigne;
    private boolean accepteSeancePresentiel;
    private LocalDateTime dateAlternative1;
    private LocalDateTime dateAlternative2;
    private LocalDateTime dateAlternative3;
    private Double prixPropose;
    private Integer dureeProposee;

    // Calculated fields
    private boolean canBeModified;
    private boolean isExpired;
    private long hoursUntilDesiredDate;

    // Constructors
    public SessionRequestResponseDTO() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEtudiantId() {
        return etudiantId;
    }

    public void setEtudiantId(Long etudiantId) {
        this.etudiantId = etudiantId;
    }

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

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
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

    public RequestStatus getStatut() {
        return statut;
    }

    public void setStatut(RequestStatus statut) {
        this.statut = statut;
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

    public String getReponseTuteur() {
        return reponseTuteur;
    }

    public void setReponseTuteur(String reponseTuteur) {
        this.reponseTuteur = reponseTuteur;
    }

    public LocalDateTime getDateReponse() {
        return dateReponse;
    }

    public void setDateReponse(LocalDateTime dateReponse) {
        this.dateReponse = dateReponse;
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

    public boolean isCanBeModified() {
        return canBeModified;
    }

    public void setCanBeModified(boolean canBeModified) {
        this.canBeModified = canBeModified;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    public long getHoursUntilDesiredDate() {
        return hoursUntilDesiredDate;
    }

    public void setHoursUntilDesiredDate(long hoursUntilDesiredDate) {
        this.hoursUntilDesiredDate = hoursUntilDesiredDate;
    }

    @Override
    public String toString() {
        return "SessionRequestResponseDTO{" +
                "id=" + id +
                ", etudiantId=" + etudiantId +
                ", tuteurId=" + tuteurId +
                ", statut=" + statut +
                ", urgence=" + urgence +
                ", dateVoulue=" + dateVoulue +
                '}';
    }
}