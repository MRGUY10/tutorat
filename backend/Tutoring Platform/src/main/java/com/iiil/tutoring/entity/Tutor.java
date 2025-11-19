package com.iiil.tutoring.entity;

import com.iiil.tutoring.enums.UserStatus;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Tutor entity representing a tutor in the platform
 * This entity contains tutor-specific information.
 * Basic user information (nom, prenom, email, etc.) is stored in the users table.
 * The id field is a foreign key to users.id.
 */
@Data
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("tutors")
public class Tutor {

    @Id
    @EqualsAndHashCode.Include
    private Long id; // Foreign key to users table - same ID as the corresponding user

    // === TUTOR SPECIFIC INFORMATION ===
    // Note: Basic user info (nom, prenom, email, telephone, statut) is in the users table

    @Size(max = 2000, message = "L'expérience ne peut pas dépasser 2000 caractères")
    @Column("experience")
    private String experience;

    @NotNull(message = "Le tarif est obligatoire")
    @DecimalMin(value = "0.0", message = "Le tarif doit être positif")
    @DecimalMax(value = "999.99", message = "Le tarif ne peut pas dépasser 999.99")
    @Column("tarif_horaire")
    private BigDecimal tarifHoraire;

    @Size(max = 1000, message = "Les diplômes ne peuvent pas dépasser 1000 caractères")
    @Column("diplomes")
    private String diplomes;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    @Column("description")
    private String description;

    // === RATING AND VERIFICATION ===

    @DecimalMin(value = "0.0", message = "La note doit être entre 0 et 5")
    @DecimalMax(value = "5.0", message = "La note doit être entre 0 et 5")
    @Column("note_moyenne")
    private BigDecimal noteMoyenne = BigDecimal.ZERO;

    @Min(value = 0, message = "Le nombre d'évaluations doit être positif")
    @Column("nombre_evaluations")
    private Integer nombreEvaluations = 0;

    @Column("verifie")
    private Boolean verifie = false;

    @Column("date_verification")
    private LocalDateTime dateVerification;

    // === AVAILABILITY AND PREFERENCES ===

    @Column("disponible")
    private Boolean disponible = true;

    @Column("cours_en_ligne")
    private Boolean coursEnLigne = true;

    @Column("cours_presentiel")
    private Boolean coursPresentiel = false;

    @Size(max = 100, message = "La ville ne peut pas dépasser 100 caractères")
    @Column("ville")
    private String ville;

    @Size(max = 100, message = "Le pays ne peut pas dépasser 100 caractères")
    @Column("pays")
    private String pays;

    // === AUDIT FIELDS ===

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // === CONSTRUCTORS ===

    /**
     * Constructor for creating a new tutor with minimal required fields
     * Note: This creates only the tutor-specific data. User data must be created separately.
     */
    public Tutor(BigDecimal tarifHoraire) {
        this.tarifHoraire = tarifHoraire;
        this.noteMoyenne = BigDecimal.ZERO;
        this.nombreEvaluations = 0;
        this.verifie = false;
        this.disponible = true;
        this.coursEnLigne = true;
        this.coursPresentiel = false;
    }

    // === BUSINESS METHODS ===

    /**
     * Check if tutor is available for booking
     */
    public boolean isAvailableForBooking() {
        return disponible && (coursEnLigne || coursPresentiel);
    }

    /**
     * Check if tutor is verified
     */
    public boolean isVerified() {
        return Boolean.TRUE.equals(verifie);
    }

    /**
     * Update tutor rating with new evaluation
     */
    public void updateRating(BigDecimal newRating) {
        if (newRating == null || newRating.compareTo(BigDecimal.ZERO) < 0 || newRating.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new IllegalArgumentException("La note doit être entre 0 et 5");
        }
        
        BigDecimal totalPoints = noteMoyenne.multiply(BigDecimal.valueOf(nombreEvaluations));
        totalPoints = totalPoints.add(newRating);
        nombreEvaluations++;
        noteMoyenne = totalPoints.divide(BigDecimal.valueOf(nombreEvaluations), 2, RoundingMode.HALF_UP);
    }

    /**
     * Verify tutor profile
     */
    public void verify() {
        this.verifie = true;
        this.dateVerification = LocalDateTime.now();
    }

    /**
     * Unverify tutor profile
     */
    public void unverify() {
        this.verifie = false;
        this.dateVerification = null;
    }

    /**
     * Set availability status
     */
    public void setAvailability(boolean disponible) {
        this.disponible = disponible;
    }

    /**
     * Update location
     */
    public void updateLocation(String ville, String pays) {
        this.ville = ville;
        this.pays = pays;
    }

    /**
     * Update teaching preferences
     */
    public void updateTeachingPreferences(boolean coursEnLigne, boolean coursPresentiel) {
        this.coursEnLigne = coursEnLigne;
        this.coursPresentiel = coursPresentiel;
        
        // At least one teaching mode must be enabled
        if (!coursEnLigne && !coursPresentiel) {
            throw new IllegalArgumentException("Au moins un mode d'enseignement doit être activé");
        }
    }

    /**
     * Calculate profile completion percentage
     * Note: User fields (nom, prenom, email, telephone) are checked in the users table
     */
    public int calculateProfileCompletion() {
        int completedFields = 0;
        int totalFields = 5; // Only tutor-specific fields

        if (experience != null && !experience.trim().isEmpty()) completedFields++;
        if (diplomes != null && !diplomes.trim().isEmpty()) completedFields++;
        if (description != null && !description.trim().isEmpty()) completedFields++;
        if (tarifHoraire != null && tarifHoraire.compareTo(BigDecimal.ZERO) > 0) completedFields++;
        if (ville != null && !ville.trim().isEmpty()) completedFields++;

        return (completedFields * 100) / totalFields;
    }

    /**
     * Check if profile is complete (>= 80%)
     */
    public boolean isProfileComplete() {
        return calculateProfileCompletion() >= 80;
    }
}