package com.iiil.tutoring.dto.tutor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for tutor registration requests
 */
@Data
@NoArgsConstructor
public class TutorRegistrationRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
    private String prenom;

    @Email(message = "Format d'email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", 
             message = "Le mot de passe doit contenir au moins une minuscule, une majuscule et un chiffre")
    private String motDePasse;

    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{8,15}$", message = "Format de téléphone invalide")
    private String telephone;

    @Size(max = 2000, message = "L'expérience ne peut pas dépasser 2000 caractères")
    private String experience;

    @NotNull(message = "Le tarif horaire est obligatoire")
    @DecimalMin(value = "5.0", message = "Le tarif horaire minimum est de 5€")
    @DecimalMax(value = "999.99", message = "Le tarif horaire ne peut pas dépasser 999.99€")
    private BigDecimal tarifHoraire;

    @Size(max = 1000, message = "Les diplômes ne peuvent pas dépasser 1000 caractères")
    private String diplomes;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    @NotEmpty(message = "Au moins une spécialité doit être sélectionnée")
    @Schema(description = "List of matière IDs representing tutor specialties. Use GET /api/matieres to get available IDs.", 
            example = "[1, 3, 5]")
    private List<Long> specialiteIds;

    @Size(max = 100, message = "La ville ne peut pas dépasser 100 caractères")
    private String ville;

    @Size(max = 100, message = "Le pays ne peut pas dépasser 100 caractères")
    private String pays;

    private Boolean coursEnLigne = true;

    private Boolean coursPresentiel = false;
}