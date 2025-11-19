package com.iiil.tutoring.entity;

import com.iiil.tutoring.enums.RequestStatus;
import com.iiil.tutoring.enums.Urgence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Session request entity
 */
@Table("demande_sessions")
public class DemandeSession {

    @Id
    private Long id;

    @NotNull(message = "L'ID de l'étudiant est obligatoire")
    @Column("etudiant_id")
    private Long etudiantId;

    @NotNull(message = "L'ID du tuteur est obligatoire")
    @Column("tuteur_id")
    private Long tuteurId;

    @NotNull(message = "L'ID de la matière est obligatoire")
    @Column("matiere_id")
    private Long matiereId;

    @Column("date_creation")
    @CreatedDate
    private LocalDateTime dateCreation;

    @NotNull(message = "La date voulue est obligatoire")
    @Column("date_heure_souhaitee")
    private LocalDateTime dateVoulue;

    @NotBlank(message = "Le message est obligatoire")
    @Column("description")
    private String message;

    @NotNull(message = "Le statut est obligatoire")
    @Column("statut")
    private RequestStatus statut = RequestStatus.EN_ATTENTE;

    @NotNull(message = "L'urgence est obligatoire")
    @Column("urgence")
    private Urgence urgence = Urgence.NORMALE;

    @Column("duree")
    private int dureeSouhaitee; // en minutes

    @Column("tarif_propose")
    private double budgetMax;

    @Column("commentaire_tuteur")
    private String reponseTuteur;

    @Column("date_reponse")
    private LocalDateTime dateReponse;

    // Note: Using date_creation from schema instead of created_at/updated_at
    // Version column not present in current schema

    // Constructors
    public DemandeSession() {}

    public DemandeSession(Long etudiantId, Long tuteurId, Long matiereId, 
                         LocalDateTime dateVoulue, String message, Urgence urgence) {
        this.etudiantId = etudiantId;
        this.tuteurId = tuteurId;
        this.matiereId = matiereId;
        this.dateVoulue = dateVoulue;
        this.message = message;
        this.urgence = urgence;
        this.statut = RequestStatus.EN_ATTENTE;
        this.dateCreation = LocalDateTime.now();
    }

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



    // Business methods
    public void accepter(String reponse) {
        this.statut = RequestStatus.ACCEPTEE;
        this.reponseTuteur = reponse;
        this.dateReponse = LocalDateTime.now();
    }

    public void refuser(String reponse) {
        this.statut = RequestStatus.REFUSEE;
        this.reponseTuteur = reponse;
        this.dateReponse = LocalDateTime.now();
    }

    public boolean isEnAttente() {
        return statut == RequestStatus.EN_ATTENTE;
    }

    @Override
    public String toString() {
        return "DemandeSession{" +
                "id=" + id +
                ", etudiantId=" + etudiantId +
                ", tuteurId=" + tuteurId +
                ", matiereId=" + matiereId +
                ", dateVoulue=" + dateVoulue +
                ", statut=" + statut +
                ", urgence=" + urgence +
                '}';
    }
}