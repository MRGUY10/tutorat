package com.iiil.tutoring.dto.user;

import com.iiil.tutoring.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for user information with roles
 */
@Schema(description = "User information with assigned roles")
public class UserWithRolesDTO {

    @Schema(description = "User ID", example = "123")
    private Long id;

    @Schema(description = "Last name", example = "Dupont")
    private String nom;

    @Schema(description = "First name", example = "Jean")
    private String prenom;

    @Schema(description = "Email address", example = "jean.dupont@example.com")
    private String email;

    @Schema(description = "Phone number", example = "0123456789")
    private String telephone;

    @Schema(description = "User status", example = "ACTIVE")
    private UserStatus statut;

    @Schema(description = "Profile photo URL", example = "https://example.com/photo.jpg")
    private String photo;

    @Schema(description = "Registration date", example = "2023-01-15T10:30:00")
    private LocalDateTime dateInscription;

    @Schema(description = "List of user roles", example = "[\"STUDENT\", \"TUTOR\"]")
    private List<String> roles;

    @Schema(description = "Primary role (for backward compatibility)", example = "STUDENT")
    private String primaryRole;

    @Schema(description = "User type (Etudiant, Tuteur, Admin)", example = "Etudiant")
    private String userType;

    @Schema(description = "Account creation date", example = "2023-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update date", example = "2023-01-15T10:30:00")
    private LocalDateTime updatedAt;

    // Constructors
    public UserWithRolesDTO() {}

    public UserWithRolesDTO(Long id, String nom, String prenom, String email, String telephone, 
                           UserStatus statut, String photo, LocalDateTime dateInscription, 
                           List<String> roles, String primaryRole, String userType, 
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.statut = statut;
        this.photo = photo;
        this.dateInscription = dateInscription;
        this.roles = roles;
        this.primaryRole = primaryRole;
        this.userType = userType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public UserStatus getStatut() { return statut; }
    public void setStatut(UserStatus statut) { this.statut = statut; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public String getPrimaryRole() { return primaryRole; }
    public void setPrimaryRole(String primaryRole) { this.primaryRole = primaryRole; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}