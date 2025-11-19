package com.iiil.tutoring.entity;

import com.iiil.tutoring.enums.SessionStatus;
import com.iiil.tutoring.enums.SessionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Tutoring session entity
 */
@Table("sessions")
public class Session {

    @Id
    private Long id;

    @NotNull(message = "L'ID du tuteur est obligatoire")
    @Column("tuteur_id")
    private Long tuteurId;

    @NotNull(message = "L'ID de l'étudiant est obligatoire")
    @Column("etudiant_id")
    private Long etudiantId;

    @NotNull(message = "L'ID de la matière est obligatoire")
    @Column("matiere_id")
    private Long matiereId;

    @Column("demande_session_id")
    private Long demandeSessionId;

    @NotNull(message = "La date et heure sont obligatoires")
    @Column("date_heure")
    private LocalDateTime dateHeure;

    @Min(value = 1, message = "La durée doit être d'au moins 1 minute")
    @Column("duree")
    private int duree; // en minutes

    @NotNull(message = "Le statut est obligatoire")
    @Column("statut")
    private SessionStatus statut = SessionStatus.DEMANDEE;

    @DecimalMin(value = "0.0", message = "Le prix doit être positif")
    @Column("prix")
    private double prix;

    @NotNull(message = "Le type de session est obligatoire")
    @Column("type_session")
    private SessionType typeSession;

    @Column("lien_visio")
    private String lienVisio;

    @Column("notes")
    private String notes;

    @Column("salle")
    private String salle; // Pour les sessions en présentiel

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Constructors
    public Session() {}

    public Session(Long tuteurId, Long etudiantId, Long matiereId, LocalDateTime dateHeure,
                   int duree, SessionType typeSession, double prix) {
        this.tuteurId = tuteurId;
        this.etudiantId = etudiantId;
        this.matiereId = matiereId;
        this.dateHeure = dateHeure;
        this.duree = duree;
        this.typeSession = typeSession;
        this.prix = prix;
        this.statut = SessionStatus.DEMANDEE;
    }

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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // Business methods
    public LocalDateTime getDateFin() {
        return dateHeure.plusMinutes(duree);
    }

    public boolean isEnLigne() {
        return typeSession == SessionType.EN_LIGNE;
    }

    public boolean isConfirmee() {
        return statut == SessionStatus.CONFIRMEE;
    }

    @Override
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", tuteurId=" + tuteurId +
                ", etudiantId=" + etudiantId +
                ", matiereId=" + matiereId +
                ", dateHeure=" + dateHeure +
                ", duree=" + duree +
                ", statut=" + statut +
                ", typeSession=" + typeSession +
                '}';
    }
}