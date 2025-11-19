package com.iiil.tutoring.entity;

import com.iiil.tutoring.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entité Role pour la gestion des rôles utilisateur
 */
@Data
@NoArgsConstructor
@Table("roles")
public class Role {

    @Id
    private Long id;

    @NotNull(message = "Le nom du rôle est obligatoire")
    @Column("nom")
    private UserRole nom;

    @Column("description")
    private String description;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Constructeur personnalisé
    public Role(UserRole nom, String description) {
        this.nom = nom;
        this.description = description;
    }
}