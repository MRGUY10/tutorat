package com.iiil.tutoring.entity;

import com.iiil.tutoring.enums.PaymentMethod;
import com.iiil.tutoring.enums.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Payment entity
 */
@Table("paiements")
public class Paiement {

    @Id
    private Long id;

    @NotNull(message = "L'ID de la session est obligatoire")
    @Column("session_id")
    private Long sessionId;

    @NotNull(message = "L'ID de l'utilisateur payeur est obligatoire")
    @Column("user_id")
    private Long userId; // L'utilisateur qui effectue le paiement (généralement l'étudiant)

    @DecimalMin(value = "0.0", message = "Le montant doit être positif")
    @NotNull(message = "Le montant est obligatoire")
    @Column("montant")
    private double montant;

    @Column("date_paiement")
    private LocalDateTime datePaiement;

    @NotNull(message = "La méthode de paiement est obligatoire")
    @Column("methode_paiement")
    private PaymentMethod methodePaiement;

    @NotNull(message = "Le statut est obligatoire")
    @Column("statut")
    private PaymentStatus statut = PaymentStatus.EN_ATTENTE;

    @NotBlank(message = "La référence de transaction est obligatoire")
    @Column("reference_transaction")
    private String referenceTransaction;

    @Column("description")
    private String description;

    @Column("commission")
    private double commission = 0.0; // Commission de la plateforme

    @Column("montant_tuteur")
    private double montantTuteur; // Montant réellement versé au tuteur

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Constructors
    public Paiement() {}

    public Paiement(Long sessionId, Long userId, double montant, PaymentMethod methodePaiement, 
                   String referenceTransaction) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.montant = montant;
        this.methodePaiement = methodePaiement;
        this.referenceTransaction = referenceTransaction;
        this.statut = PaymentStatus.EN_ATTENTE;
        this.datePaiement = LocalDateTime.now();
        calculateMontantTuteur();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
        calculateMontantTuteur();
    }

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDateTime datePaiement) {
        this.datePaiement = datePaiement;
    }

    public PaymentMethod getMethodePaiement() {
        return methodePaiement;
    }

    public void setMethodePaiement(PaymentMethod methodePaiement) {
        this.methodePaiement = methodePaiement;
    }

    public PaymentStatus getStatut() {
        return statut;
    }

    public void setStatut(PaymentStatus statut) {
        this.statut = statut;
    }

    public String getReferenceTransaction() {
        return referenceTransaction;
    }

    public void setReferenceTransaction(String referenceTransaction) {
        this.referenceTransaction = referenceTransaction;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCommission() {
        return commission;
    }

    public void setCommission(double commission) {
        this.commission = commission;
        calculateMontantTuteur();
    }

    public double getMontantTuteur() {
        return montantTuteur;
    }

    public void setMontantTuteur(double montantTuteur) {
        this.montantTuteur = montantTuteur;
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
    private void calculateMontantTuteur() {
        this.montantTuteur = this.montant - this.commission;
    }

    public void completer() {
        this.statut = PaymentStatus.COMPLETE;
        this.datePaiement = LocalDateTime.now();
    }

    public void echouer() {
        this.statut = PaymentStatus.ECHOUE;
    }

    public void rembourser() {
        this.statut = PaymentStatus.REMBOURSE;
    }

    public boolean isComplete() {
        return statut == PaymentStatus.COMPLETE;
    }

    public boolean isEnAttente() {
        return statut == PaymentStatus.EN_ATTENTE;
    }

    @Override
    public String toString() {
        return "Paiement{" +
                "id=" + id +
                ", sessionId=" + sessionId +
                ", userId=" + userId +
                ", montant=" + montant +
                ", methodePaiement=" + methodePaiement +
                ", statut=" + statut +
                ", referenceTransaction='" + referenceTransaction + '\'' +
                '}';
    }
}