package com.iiil.tutoring.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request to create a tutor (Admin endpoint)")
public class CreateTutorRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Schema(description = "Tutor's last name", example = "Martin")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Schema(description = "Tutor's first name", example = "Marie")
    private String prenom;

    @Email(message = "Format d'email invalide")
    @NotBlank(message = "L'email est obligatoire")
    @Schema(description = "Tutor's email address", example = "marie.martin@tutor.com")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Schema(description = "Tutor's password", example = "securePassword123")
    private String motDePasse;

    @Schema(description = "Tutor's phone number", example = "0123456789")
    private String telephone;

    @Schema(description = "Tutor's teaching experience", example = "3 ans d'expérience en tutorat")
    private String experience;

    @DecimalMin(value = "0.0", message = "Le tarif doit être positif")
    @Schema(description = "Tutor's hourly rate", example = "25.0")
    private double tarif;

    @Schema(description = "Tutor's diplomas and qualifications", example = "Master en Informatique, Université de Paris")
    private String diplomes;

    @Schema(description = "Tutor's description", example = "Tutrice spécialisée en programmation et mathématiques")
    private String description;
}