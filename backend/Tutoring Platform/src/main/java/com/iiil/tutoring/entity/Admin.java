package com.iiil.tutoring.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entité Administrateur - contient uniquement les données spécifiques aux admins
 * Les données utilisateur de base sont dans la table users
 */
@Data
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("admins")
public class Admin {

    @Id
    @EqualsAndHashCode.Include
    private Long id; // Foreign key to users table

    @Column("permissions")
    private String permissions;

    @Column("departement")
    private String departement;

    // Constructeur personnalisé
    public Admin(Long id, String permissions, String departement) {
        this.id = id;
        this.permissions = permissions;
        this.departement = departement;
    }

    // Explicit getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }
    
    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }
}