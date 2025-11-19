package com.iiil.tutoring.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Junction table entity linking tutors with their specialty subjects (matières)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("tutor_specialites")
public class TutorSpecialite {

    @Id
    @EqualsAndHashCode.Include
    private Long id;

    @Column("tutor_id")
    @EqualsAndHashCode.Include
    private Long tutorId;

    @Column("matiere_id")
    @EqualsAndHashCode.Include
    private Long matiereId;

    /**
     * Constructor for creating new tutor specialty
     */
    public TutorSpecialite(Long tutorId, Long matiereId) {
        this.tutorId = tutorId;
        this.matiereId = matiereId;
    }
}
