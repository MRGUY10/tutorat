package com.iiil.tutoring.dto.auth;

import com.iiil.tutoring.enums.NiveauAcademique;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Student registration request DTO
 */
@Data
@NoArgsConstructor
public class StudentRegisterRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @Email(message = "Format d'email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String motDePasse;

    private String telephone;

    @NotBlank(message = "La filière est obligatoire")
    private String filiere;

    @Min(value = 1, message = "L'année doit être supérieure à 0")
    private int annee;

    private NiveauAcademique niveau = NiveauAcademique.DEBUTANT;

    public StudentRegisterRequest(String nom, String prenom, String email, String motDePasse,
                                 String filiere, int annee, NiveauAcademique niveau) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.filiere = filiere;
        this.annee = annee;
        this.niveau = niveau;
    }
}