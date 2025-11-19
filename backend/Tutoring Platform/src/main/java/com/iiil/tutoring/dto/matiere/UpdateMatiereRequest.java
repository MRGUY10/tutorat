package com.iiil.tutoring.dto.matiere;

import com.iiil.tutoring.enums.NiveauAcademique;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a subject
 */
@Data
@NoArgsConstructor
public class UpdateMatiereRequest {

    @NotBlank(message = "Le nom de la matière est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String nom;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    private NiveauAcademique niveau;

    @Size(max = 100, message = "Le domaine ne peut pas dépasser 100 caractères")
    private String domaine;

    public UpdateMatiereRequest(String nom, String description, NiveauAcademique niveau, String domaine) {
        this.nom = nom;
        this.description = description;
        this.niveau = niveau;
        this.domaine = domaine;
    }
}