package com.iiil.tutoring.dto;

import com.iiil.tutoring.enums.NiveauAcademique;
import com.iiil.tutoring.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for complete student information including user data
 */
@Data
@NoArgsConstructor
public class EtudiantCompletDto {
    
    // User fields
    private Long id;
    
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    
    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;
    
    @Email(message = "Format d'email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;
    
    private String telephone;
    private LocalDateTime dateInscription;
    private UserStatus statut = UserStatus.ACTIVE;
    private String photo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
    
    // Etudiant specific fields
    @NotBlank(message = "La filière est obligatoire")
    private String filiere;
    
    @Min(value = 1, message = "L'année doit être supérieure à 0")
    private int annee;
    
    @NotNull(message = "Le niveau est obligatoire")
    private NiveauAcademique niveau = NiveauAcademique.DEBUTANT;
    
    public EtudiantCompletDto(String nom, String prenom, String email, String motDePasse,
                             String filiere, int annee, NiveauAcademique niveau) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.filiere = filiere;
        this.annee = annee;
        this.niveau = niveau;
        this.statut = UserStatus.ACTIVE;
        this.dateInscription = LocalDateTime.now();
    }
}