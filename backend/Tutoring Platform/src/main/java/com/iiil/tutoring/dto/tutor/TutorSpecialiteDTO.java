package com.iiil.tutoring.dto.tutor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for tutor specialty information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutorSpecialiteDTO {
    private Long id;
    private Long tutorId;
    private Long matiereId;
    private String matiereNom;
    private String matiereDescription;
}
