package com.iiil.tutoring.dto.tutor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for tutor summary information (used in lists and search results)
 */
@Data
@NoArgsConstructor
public class TutorSummaryResponse {

    private Long id;
    private String fullName;
    private String specialites; // Comma-separated specialty names
    private BigDecimal tarifHoraire;
    private BigDecimal noteMoyenne;
    private Integer nombreEvaluations;
    private Boolean verifie;
    private Boolean disponible;
    private Boolean coursEnLigne;
    private Boolean coursPresentiel;
    private String ville;
    private String pays;
    private String description;

    /**
     * Constructor from Tutor and User entities
     */
    public TutorSummaryResponse(com.iiil.tutoring.entity.Tutor tutor, com.iiil.tutoring.entity.User user) {
        this.id = tutor.getId();
        this.fullName = user.getPrenom() + " " + user.getNom();
        // specialites will be set separately by the service layer
        this.tarifHoraire = tutor.getTarifHoraire();
        this.noteMoyenne = tutor.getNoteMoyenne();
        this.nombreEvaluations = tutor.getNombreEvaluations();
        this.verifie = tutor.getVerifie();
        this.disponible = tutor.getDisponible();
        this.coursEnLigne = tutor.getCoursEnLigne();
        this.coursPresentiel = tutor.getCoursPresentiel();
        this.ville = tutor.getVille();
        this.pays = tutor.getPays();
        this.description = tutor.getDescription();
    }
}