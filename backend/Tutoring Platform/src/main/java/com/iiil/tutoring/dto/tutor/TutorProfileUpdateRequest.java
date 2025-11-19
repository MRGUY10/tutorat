package com.iiil.tutoring.dto.tutor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for updating tutor profile information
 */
@Data
@NoArgsConstructor
public class TutorProfileUpdateRequest {

    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    private String nom;

    @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
    private String prenom;

    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{8,15}$", message = "Format de téléphone invalide")
    private String telephone;

    @Size(max = 2000, message = "L'expérience ne peut pas dépasser 2000 caractères")
    private String experience;

    @DecimalMin(value = "5.0", message = "Le tarif horaire minimum est de 5€")
    @DecimalMax(value = "999.99", message = "Le tarif horaire ne peut pas dépasser 999.99€")
    private BigDecimal tarifHoraire;

    @Size(max = 1000, message = "Les diplômes ne peuvent pas dépasser 1000 caractères")
    private String diplomes;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    @Schema(description = "List of matière IDs representing tutor specialties. Use GET /api/matieres to get available IDs. If provided, will replace all existing specialties.", 
            example = "[1, 3, 5]")
    private List<Long> specialiteIds;

    @Size(max = 100, message = "La ville ne peut pas dépasser 100 caractères")
    private String ville;

    @Size(max = 100, message = "Le pays ne peut pas dépasser 100 caractères")
    private String pays;

    private Boolean coursEnLigne;

    private Boolean coursPresentiel;
}