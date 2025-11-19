package com.iiil.tutoring.repository;

import com.iiil.tutoring.entity.Tutor;
import com.iiil.tutoring.enums.UserStatus;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Repository interface for Tutor entity operations
 * Provides reactive database access for tutor management
 */
@Repository
public interface TutorRepository extends R2dbcRepository<Tutor, Long> {

    // === BASIC QUERIES ===
    // Note: Email and status queries are now in UserRepository since Tutor references User

    /**
     * Find active tutors (JOINs with users table)
     */
    @Query("SELECT t.* FROM tutors t JOIN users u ON t.id = u.id WHERE u.statut = 'ACTIVE' ORDER BY t.created_at DESC")
    Flux<Tutor> findActiveTutors();

    // === SEARCH AND FILTERING ===

    /**
     * Find available tutors for booking
     */
    @Query("SELECT t.* FROM tutors t JOIN users u ON t.id = u.id WHERE u.statut = 'ACTIVE' AND t.disponible = true AND (t.cours_en_ligne = true OR t.cours_presentiel = true) ORDER BY t.note_moyenne DESC, t.nombre_evaluations DESC")
    Flux<Tutor> findAvailableTutors();

    /**
     * Find verified tutors
     */
    @Query("SELECT t.* FROM tutors t JOIN users u ON t.id = u.id WHERE u.statut = 'ACTIVE' AND t.verifie = true ORDER BY t.note_moyenne DESC")
    Flux<Tutor> findVerifiedTutors();

    /**
     * Find tutors by location
     */
    @Query("SELECT t.* FROM tutors t JOIN users u ON t.id = u.id WHERE u.statut = 'ACTIVE' AND LOWER(t.ville) = LOWER(:ville) ORDER BY t.note_moyenne DESC")
    Flux<Tutor> findByVille(String ville);

    /**
     * Find tutors by specialty (using tutor_specialites junction table)
     */
    @Query("SELECT DISTINCT t.* FROM tutors t " +
           "JOIN users u ON t.id = u.id " +
           "JOIN tutor_specialites ts ON t.id = ts.tutor_id " +
           "JOIN matieres m ON ts.matiere_id = m.id " +
           "WHERE u.statut = 'ACTIVE' AND m.id = :matiereId " +
           "ORDER BY t.note_moyenne DESC")
    Flux<Tutor> findByMatiereId(Long matiereId);

    /**
     * Search tutors by keywords (name, description)
     */
    @Query("SELECT DISTINCT t.* FROM tutors t JOIN users u ON t.id = u.id WHERE u.statut = 'ACTIVE' AND (" +
           "LOWER(u.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.prenom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
           ") ORDER BY t.note_moyenne DESC")
    Flux<Tutor> searchByKeyword(String keyword);

    /**
     * Find tutors by hourly rate range
     */
    @Query("SELECT t.* FROM tutors t JOIN users u ON t.id = u.id WHERE u.statut = 'ACTIVE' AND t.tarif_horaire BETWEEN :minTarif AND :maxTarif ORDER BY t.tarif_horaire ASC")
    Flux<Tutor> findByTarifHoraireBetween(BigDecimal minTarif, BigDecimal maxTarif);

    /**
     * Find tutors by minimum rating
     */
    @Query("SELECT t.* FROM tutors t JOIN users u ON t.id = u.id WHERE u.statut = 'ACTIVE' AND t.note_moyenne >= :minRating AND t.nombre_evaluations >= :minEvaluations ORDER BY t.note_moyenne DESC")
    Flux<Tutor> findByMinimumRating(BigDecimal minRating, Integer minEvaluations);

    /**
     * Find tutors offering online courses
     */
    @Query("SELECT t.* FROM tutors t JOIN users u ON t.id = u.id WHERE u.statut = 'ACTIVE' AND t.cours_en_ligne = true ORDER BY t.note_moyenne DESC")
    Flux<Tutor> findOnlineTutors();

    /**
     * Find tutors offering in-person courses
     */
    @Query("SELECT t.* FROM tutors t JOIN users u ON t.id = u.id WHERE u.statut = 'ACTIVE' AND t.cours_presentiel = true ORDER BY t.note_moyenne DESC")
    Flux<Tutor> findInPersonTutors();

    /**
     * Advanced search with multiple filters
     */
    @Query("SELECT DISTINCT t.* FROM tutors t " +
           "JOIN users u ON t.id = u.id " +
           "LEFT JOIN tutor_specialites ts ON t.id = ts.tutor_id " +
           "WHERE u.statut = 'ACTIVE' " +
           "AND (:matiereId IS NULL OR ts.matiere_id = :matiereId) " +
           "AND (:ville IS NULL OR LOWER(t.ville) = LOWER(:ville)) " +
           "AND (:minTarif IS NULL OR t.tarif_horaire >= :minTarif) " +
           "AND (:maxTarif IS NULL OR t.tarif_horaire <= :maxTarif) " +
           "AND (:minRating IS NULL OR t.note_moyenne >= :minRating) " +
           "AND (:verifiedOnly = false OR t.verifie = true) " +
           "AND (:onlineOnly = false OR t.cours_en_ligne = true) " +
           "AND (:inPersonOnly = false OR t.cours_presentiel = true) " +
           "ORDER BY t.note_moyenne DESC, t.nombre_evaluations DESC")
    Flux<Tutor> findWithFilters(Long matiereId, String ville, BigDecimal minTarif, BigDecimal maxTarif, 
                               BigDecimal minRating, Boolean verifiedOnly, Boolean onlineOnly, Boolean inPersonOnly);

    // === STATISTICS AND ANALYTICS ===

    /**
     * Count active tutors
     */
    @Query("SELECT COUNT(*) FROM tutors t JOIN users u ON t.id = u.id WHERE u.statut = 'ACTIVE'")
    Mono<Long> countActiveTutors();

    /**
     * Count available tutors
     */
    @Query("SELECT COUNT(*) FROM tutors t JOIN users u ON t.id = u.id WHERE u.statut = 'ACTIVE' AND t.disponible = true")
    Mono<Long> countAvailableTutors();

    /**
     * Count verified tutors
     */
    @Query("SELECT COUNT(*) FROM tutors t JOIN users u ON t.id = u.id WHERE u.statut = 'ACTIVE' AND t.verifie = true")
    Mono<Long> countVerifiedTutors();

    /**
     * Get average rating of all tutors
     */
    @Query("SELECT AVG(t.note_moyenne) FROM tutors t JOIN users u ON t.id = u.id WHERE u.statut = 'ACTIVE' AND t.nombre_evaluations > 0")
    Mono<BigDecimal> getAverageRating();

    /**
     * Get top rated tutors (minimum 5 evaluations)
     */
    @Query("SELECT t.* FROM tutors t JOIN users u ON t.id = u.id WHERE u.statut = 'ACTIVE' AND t.nombre_evaluations >= 5 ORDER BY t.note_moyenne DESC LIMIT :limit")
    Flux<Tutor> getTopRatedTutors(int limit);

    // === PROFILE MANAGEMENT ===

    /**
     * Update tutor rating
     */
    @Modifying
    @Query("UPDATE tutors SET note_moyenne = :noteMoyenne, nombre_evaluations = :nombreEvaluations, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
    Mono<Integer> updateRating(Long id, BigDecimal noteMoyenne, Integer nombreEvaluations);

    /**
     * Update verification status
     */
    @Modifying
    @Query("UPDATE tutors SET verifie = :verifie, date_verification = :dateVerification, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
    Mono<Integer> updateVerificationStatus(Long id, Boolean verifie, java.time.LocalDateTime dateVerification);

    /**
     * Update availability status
     */
    @Modifying
    @Query("UPDATE tutors SET disponible = :disponible, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
    Mono<Integer> updateAvailability(Long id, Boolean disponible);

    /**
     * Update tutor location
     */
    @Modifying
    @Query("UPDATE tutors SET ville = :ville, pays = :pays, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
    Mono<Integer> updateLocation(Long id, String ville, String pays);

    /**
     * Update teaching preferences
     */
    @Modifying
    @Query("UPDATE tutors SET cours_en_ligne = :coursEnLigne, cours_presentiel = :coursPresentiel, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
    Mono<Integer> updateTeachingPreferences(Long id, Boolean coursEnLigne, Boolean coursPresentiel);

    // === REPORTING ===

    /**
     * Get tutors registered in the last N days
     */
    @Query("SELECT * FROM tutors WHERE created_at >= CURRENT_TIMESTAMP - INTERVAL :days DAY ORDER BY created_at DESC")
    Flux<Tutor> findRecentTutors(int days);

    /**
     * Get tutors by registration month
     */
    @Query("SELECT * FROM tutors WHERE EXTRACT(YEAR FROM created_at) = :year AND EXTRACT(MONTH FROM created_at) = :month ORDER BY created_at DESC")
    Flux<Tutor> findTutorsByMonth(int year, int month);

    /**
     * Get distinct cities where tutors are located
     */
    @Query("SELECT DISTINCT t.ville FROM tutors t JOIN users u ON t.id = u.id WHERE u.statut = 'ACTIVE' AND t.ville IS NOT NULL ORDER BY t.ville")
    Flux<String> getDistinctCities();

    /**
     * Get distinct matieres taught by tutors
     */
    @Query("SELECT DISTINCT m.nom FROM tutors t " +
           "JOIN users u ON t.id = u.id " +
           "JOIN tutor_specialites ts ON t.id = ts.tutor_id " +
           "JOIN matieres m ON ts.matiere_id = m.id " +
           "WHERE u.statut = 'ACTIVE' " +
           "ORDER BY m.nom")
    Flux<String> getDistinctMatieres();
}