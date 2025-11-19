package com.iiil.tutoring.dto.session;

import com.iiil.tutoring.enums.RequestStatus;
import com.iiil.tutoring.enums.Urgence;
import com.iiil.tutoring.enums.UserStatus;

import java.time.LocalDateTime;

/**
 * DTO for detailed session request information with user details
 */
public class SessionRequestDetailsDTO {

    // Session request details
    private Long id;
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

    // Enhanced request fields
    private String notesAdditionnelles;
    private boolean flexibleSurDate;
    private boolean accepteSeanceEnLigne;
    private boolean accepteSeancePresentiel;
    private LocalDateTime dateAlternative1;
    private LocalDateTime dateAlternative2;
    private LocalDateTime dateAlternative3;
    private Double prixPropose;
    private Integer dureeProposee;

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

    // Calculated fields
    private boolean canBeModified;
    private boolean isExpired;
    private boolean isUrgent;
    private long hoursUntilDesiredDate;
    private double estimatedPrice;
    private String statusDescription;

    // Related session (if created)
    private Long sessionId;
    private LocalDateTime sessionDateHeure;
    private String sessionStatut;

    // Constructors
    public SessionRequestDetailsDTO() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isUrgent() {
        return isUrgent;
    }

    public void setUrgent(boolean urgent) {
        isUrgent = urgent;
    }

    public long getHoursUntilDesiredDate() {
        return hoursUntilDesiredDate;
    }

    public void setHoursUntilDesiredDate(long hoursUntilDesiredDate) {
        this.hoursUntilDesiredDate = hoursUntilDesiredDate;
    }

    public double getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(double estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDateTime getSessionDateHeure() {
        return sessionDateHeure;
    }

    public void setSessionDateHeure(LocalDateTime sessionDateHeure) {
        this.sessionDateHeure = sessionDateHeure;
    }

    public String getSessionStatut() {
        return sessionStatut;
    }

    public void setSessionStatut(String sessionStatut) {
        this.sessionStatut = sessionStatut;
    }

    @Override
    public String toString() {
        return "SessionRequestDetailsDTO{" +
                "id=" + id +
                ", etudiantNom='" + etudiantNom + '\'' +
                ", etudiantPrenom='" + etudiantPrenom + '\'' +
                ", tuteurNom='" + tuteurNom + '\'' +
                ", tuteurPrenom='" + tuteurPrenom + '\'' +
                ", matiereNom='" + matiereNom + '\'' +
                ", statut=" + statut +
                ", urgence=" + urgence +
                ", dateVoulue=" + dateVoulue +
                '}';
    }
}