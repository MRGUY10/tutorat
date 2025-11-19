package com.iiil.tutoring.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request to create an admin (Super admin endpoint)")
public class CreateAdminRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Schema(description = "Admin's last name", example = "Admin")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Schema(description = "Admin's first name", example = "Super")
    private String prenom;

    @Email(message = "Format d'email invalide")
    @NotBlank(message = "L'email est obligatoire")
    @Schema(description = "Admin's email address", example = "admin@tutoring.com")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Schema(description = "Admin's password", example = "securePassword123")
    private String motDePasse;

    @Schema(description = "Admin's phone number", example = "0123456789")
    private String telephone;

    @Schema(description = "Admin's permissions", example = "READ_WRITE_ALL")
    private String permissions;

    @Schema(description = "Admin's department", example = "IT Management")
    private String departement;
}