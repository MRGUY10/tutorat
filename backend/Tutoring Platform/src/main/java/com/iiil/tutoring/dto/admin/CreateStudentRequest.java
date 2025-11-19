package com.iiil.tutoring.dto.admin;

import com.iiil.tutoring.enums.NiveauAcademique;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request to create a student (Admin endpoint)")
public class CreateStudentRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Schema(description = "Student's last name", example = "Dupont")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Schema(description = "Student's first name", example = "Jean")
    private String prenom;

    @Email(message = "Format d'email invalide")
    @NotBlank(message = "L'email est obligatoire")
    @Schema(description = "Student's email address", example = "jean.dupont@student.com")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Schema(description = "Student's password", example = "securePassword123")
    private String motDePasse;

    @Schema(description = "Student's phone number", example = "0123456789")
    private String telephone;

    @NotBlank(message = "La filière est obligatoire")
    @Schema(description = "Student's field of study", example = "Informatique")
    private String filiere;

    @Min(value = 1, message = "L'année doit être supérieure à 0")
    @Schema(description = "Student's academic year", example = "2")
    private int annee;

    @Schema(description = "Student's academic level", example = "INTERMEDIAIRE")
    private NiveauAcademique niveau = NiveauAcademique.DEBUTANT;
}