package com.iiil.tutoring.dto.user;

import com.iiil.tutoring.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating user information
 */
@Schema(description = "Request DTO for updating user information")
public class UpdateUserRequest {

    @Schema(description = "First name", example = "Jean")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String prenom;

    @Schema(description = "Last name", example = "Dupont")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String nom;

    @Schema(description = "Email address", example = "jean.dupont@example.com")
    @Email(message = "Format d'email invalide")
    private String email;

    @Schema(description = "Phone number", example = "+33123456789")
    private String telephone;

    @Schema(description = "User status", example = "ACTIVE")
    private UserStatus statut;

    @Schema(description = "Profile photo URL", example = "https://example.com/photo.jpg")
    private String photo;

    // Constructors
    public UpdateUserRequest() {}

    public UpdateUserRequest(String prenom, String nom, String email, String telephone, UserStatus statut, String photo) {
        this.prenom = prenom;
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.statut = statut;
        this.photo = photo;
    }

    // Getters and Setters
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public UserStatus getStatut() { return statut; }
    public void setStatut(UserStatus statut) { this.statut = statut; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    @Override
    public String toString() {
        return "UpdateUserRequest{" +
                "prenom='" + prenom + '\'' +
                ", nom='" + nom + '\'' +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                ", statut=" + statut +
                ", photo='" + photo + '\'' +
                '}';
    }
}