package com.iiil.tutoring.entity;

import com.iiil.tutoring.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Classe de base pour tous les utilisateurs de la plateforme de tutorat
 */
@Data
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("users")
public class User {

    @Id
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Column("nom")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Column("prenom")
    private String prenom;

    @Email(message = "Format d'email invalide")
    @NotBlank(message = "L'email est obligatoire")
    @Column("email")
    @EqualsAndHashCode.Include
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Column("mot_de_passe")
    @ToString.Exclude
    private String motDePasse;

    @Column("telephone")
    private String telephone;

    @Column("date_inscription")
    @CreatedDate
    private LocalDateTime dateInscription;

    @NotNull(message = "Le statut est obligatoire")
    @Column("statut")
    private UserStatus statut = UserStatus.ACTIVE;

    @Column("photo")
    private String photo;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Constructeur par défaut
    public User() {}

    // Constructeur public pour l'héritage et l'utilisation directe
    public User(String nom, String prenom, String email, String motDePasse) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.statut = UserStatus.ACTIVE;
        this.dateInscription = LocalDateTime.now();
    }

    // Explicit getters and setters to ensure compilation works
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    
    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }
    
    public UserStatus getStatut() { return statut; }
    public void setStatut(UserStatus statut) { this.statut = statut; }
    
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    // Méthodes utilitaires
    public String getFullName() {
        return prenom + " " + nom;
    }

    public boolean isActive() {
        return statut == UserStatus.ACTIVE;
    }
}