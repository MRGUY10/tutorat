package com.iiil.tutoring.entity;

import com.iiil.tutoring.enums.NiveauAcademique;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entité Étudiant - contient uniquement les données spécifiques aux étudiants
 * Les données utilisateur de base sont dans la table users
 */
@Data
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("etudiants")
public class Etudiant {

    @Id
    @EqualsAndHashCode.Include
    private Long id; // Foreign key to users table

    @NotBlank(message = "La filière est obligatoire")
    @Column("filiere")
    private String filiere;

    @Min(value = 1, message = "L'année doit être supérieure à 0")
    @Column("annee")
    private int annee;

    @Column("niveau")
    private String niveau = "debutant"; // Store as String to match database

    // Constructeur personnalisé
    public Etudiant(Long id, String filiere, int annee, String niveau) {
        this.id = id;
        this.filiere = filiere;
        this.annee = annee;
        this.niveau = niveau;
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

    // Explicit getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFiliere() { return filiere; }
    public void setFiliere(String filiere) { this.filiere = filiere; }
    
    public int getAnnee() { return annee; }
    public void setAnnee(int annee) { this.annee = annee; }
    
        public String getNiveau() { return niveau; }
        public void setNiveau(String niveau) { this.niveau = niveau; }
        // Convenience setter for enum
        public void setNiveau(NiveauAcademique niveauEnum) {
            this.niveau = niveauEnum != null ? niveauEnum.getValue() : "debutant";
        }
}