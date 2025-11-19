package com.iiil.tutoring.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Available time slot entity
 */
@Table("creneaux_disponibles")
public class CreneauDisponible {

    @Id
    private Long id;

    @NotNull(message = "L'ID du planning est obligatoire")
    @Column("planning_id")
    private Long planningId;

    @NotBlank(message = "Le jour de la semaine est obligatoire")
    @Column("jour_semaine")
    private String jourSemaine; // LUNDI, MARDI, etc.

    @NotNull(message = "L'heure de début est obligatoire")
    @Column("heure_debut")
    private LocalTime heureDebut;

    @NotNull(message = "L'heure de fin est obligatoire")
    @Column("heure_fin")
    private LocalTime heureFin;

    @Column("disponible")
    private boolean disponible = true;

    @Column("recurrent")
    private boolean recurrent = true; // Si le créneau se répète chaque semaine

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Constructors
    public CreneauDisponible() {}

    public CreneauDisponible(Long planningId, String jourSemaine, LocalTime heureDebut, 
                           LocalTime heureFin, boolean disponible) {
        this.planningId = planningId;
        this.jourSemaine = jourSemaine;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.disponible = disponible;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlanningId() {
        return planningId;
    }

    public void setPlanningId(Long planningId) {
        this.planningId = planningId;
    }

    public String getJourSemaine() {
        return jourSemaine;
    }

    public void setJourSemaine(String jourSemaine) {
        this.jourSemaine = jourSemaine;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public boolean isRecurrent() {
        return recurrent;
    }

    public void setRecurrent(boolean recurrent) {
        this.recurrent = recurrent;
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
    public int getDureeEnMinutes() {
        return (int) java.time.Duration.between(heureDebut, heureFin).toMinutes();
    }

    @Override
    public String toString() {
        return "CreneauDisponible{" +
                "id=" + id +
                ", planningId=" + planningId +
                ", jourSemaine='" + jourSemaine + '\'' +
                ", heureDebut=" + heureDebut +
                ", heureFin=" + heureFin +
                ", disponible=" + disponible +
                '}';
    }
}