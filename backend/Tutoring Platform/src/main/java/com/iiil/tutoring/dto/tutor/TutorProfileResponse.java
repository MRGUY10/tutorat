package com.iiil.tutoring.dto.tutor;

import com.iiil.tutoring.enums.UserStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for tutor profile responses
 * Contains all tutor information for display purposes
 */
@Data
@NoArgsConstructor
public class TutorProfileResponse {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private UserStatus statut;
    private LocalDateTime dateInscription;

    // Tutor-specific fields
    private String experience;
    private BigDecimal tarifHoraire;
    private String diplomes;
    private String description;
    private java.util.List<TutorSpecialiteDTO> specialites; // List of tutor specialties

    // Rating and verification
    private BigDecimal noteMoyenne;
    private Integer nombreEvaluations;
    private Boolean verifie;
    private LocalDateTime dateVerification;

    // Availability and preferences
    private Boolean disponible;
    private Boolean coursEnLigne;
    private Boolean coursPresentiel;
    private String ville;
    private String pays;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed fields
    private String fullName;
    private Boolean availableForBooking;
    private Integer profileCompletion;

    /**
     * Constructor from Tutor and User entities
     * Note: User information comes from the users table, tutor-specific info from tutors table
     */
    public TutorProfileResponse(com.iiil.tutoring.entity.Tutor tutor, com.iiil.tutoring.entity.User user) {
        this.id = tutor.getId();
        
        // User information from users table
        this.nom = user.getNom();
        this.prenom = user.getPrenom();
        this.email = user.getEmail();
        this.telephone = user.getTelephone();
        this.statut = user.getStatut();
        this.dateInscription = user.getDateInscription();

        // Tutor-specific information from tutors table
        this.experience = tutor.getExperience();
        this.tarifHoraire = tutor.getTarifHoraire();
        this.diplomes = tutor.getDiplomes();
        this.description = tutor.getDescription();
        // specialites will be set separately by the service layer

        this.noteMoyenne = tutor.getNoteMoyenne();
        this.nombreEvaluations = tutor.getNombreEvaluations();
        this.verifie = tutor.getVerifie();
        this.dateVerification = tutor.getDateVerification();

        this.disponible = tutor.getDisponible();
        this.coursEnLigne = tutor.getCoursEnLigne();
        this.coursPresentiel = tutor.getCoursPresentiel();
        this.ville = tutor.getVille();
        this.pays = tutor.getPays();

        this.createdAt = tutor.getCreatedAt();
        this.updatedAt = tutor.getUpdatedAt();

        // Computed fields
        this.fullName = user.getPrenom() + " " + user.getNom();
        this.availableForBooking = tutor.isAvailableForBooking();
        this.profileCompletion = tutor.calculateProfileCompletion();
    }
}