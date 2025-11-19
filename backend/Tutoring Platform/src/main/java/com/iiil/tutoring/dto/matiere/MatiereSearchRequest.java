package com.iiil.tutoring.dto.matiere;

import com.iiil.tutoring.enums.NiveauAcademique;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for subject search criteria
 */
@Data
@NoArgsConstructor
public class MatiereSearchRequest {

    private String searchTerm;
    private String domaine;
    private NiveauAcademique niveau;
    private int page = 0;
    private int size = 20;

    public MatiereSearchRequest(String searchTerm, String domaine, NiveauAcademique niveau, int page, int size) {
        this.searchTerm = searchTerm;
        this.domaine = domaine;
        this.niveau = niveau;
        this.page = page;
        this.size = size;
    }
}