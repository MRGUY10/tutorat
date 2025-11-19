package com.iiil.tutoring.repository;

import com.iiil.tutoring.entity.TutorSpecialite;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository for tutor specialties (junction table between tutors and matières)
 */
@Repository
public interface TutorSpecialiteRepository extends R2dbcRepository<TutorSpecialite, Long> {

    /**
     * Find all specialties for a specific tutor
     */
    Flux<TutorSpecialite> findByTutorId(Long tutorId);

    /**
     * Find all tutors that specialize in a specific matière
     */
    Flux<TutorSpecialite> findByMatiereId(Long matiereId);

    /**
     * Delete all specialties for a tutor
     */
    Mono<Void> deleteByTutorId(Long tutorId);

    /**
     * Check if a tutor has a specific specialty
     */
    Mono<Boolean> existsByTutorIdAndMatiereId(Long tutorId, Long matiereId);

    /**
     * Get matière details for a tutor's specialties
     */
    @Query("SELECT m.* FROM matieres m " +
           "INNER JOIN tutor_specialites ts ON m.id = ts.matiere_id " +
           "WHERE ts.tutor_id = :tutorId")
    Flux<Object> findMatieresForTutor(Long tutorId);
}
