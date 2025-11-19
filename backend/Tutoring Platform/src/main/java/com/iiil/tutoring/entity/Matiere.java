package com.iiil.tutoring.entity;

import com.iiil.tutoring.enums.NiveauAcademique;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entité Matière
 */
@Data
@NoArgsConstructor
@Table("matieres")
public class Matiere {

    @Id
    private Long id;

    @NotBlank(message = "Le nom de la matière est obligatoire")
    @Column("nom")
    private String nom;

    @Column("description")
    private String description;

    @Column("niveau")
    private String niveau; // Store as String to match database constraints

    @Column("domaine")
    private String domaine; // mathématiques, informatique, langues, etc.

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Constructeur personnalisé
    public Matiere(String nom, String description, String niveau, String domaine) {
        this.nom = nom;
        this.description = description;
        this.niveau = niveau;
        this.domaine = domaine;
    }

    // Helper methods for enum conversion
    public NiveauAcademique getNiveauEnum() {
        if (niveau == null) return NiveauAcademique.DEBUTANT;
        return switch (niveau.toLowerCase()) {
            case "intermediaire" -> NiveauAcademique.INTERMEDIAIRE;
            case "avance" -> NiveauAcademique.AVANCE;
            default -> NiveauAcademique.DEBUTANT;
        };
    }

    public void setNiveauEnum(NiveauAcademique niveauEnum) {
        this.niveau = niveauEnum != null ? niveauEnum.getValue() : "debutant";
    }
}