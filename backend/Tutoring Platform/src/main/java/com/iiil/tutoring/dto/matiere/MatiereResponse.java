package com.iiil.tutoring.dto.matiere;

import com.iiil.tutoring.enums.NiveauAcademique;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for subject response
 */
@Data
@NoArgsConstructor
public class MatiereResponse {

    private Long id;
    private String nom;
    private String description;
    private NiveauAcademique niveau;
    private String domaine;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    public MatiereResponse(Long id, String nom, String description, NiveauAcademique niveau, 
                          String domaine, LocalDateTime createdAt, LocalDateTime updatedAt, Long version) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.niveau = niveau;
        this.domaine = domaine;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }
}