package com.iiil.tutoring.repository;

import com.iiil.tutoring.entity.Evaluation;
import com.iiil.tutoring.enums.EvaluationType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Repository for Evaluation entity with comprehensive analytics capabilities
 */
public interface EvaluationRepository extends R2dbcRepository<Evaluation, Long> {

    // Basic queries
    Flux<Evaluation> findBySessionId(Long sessionId);
    Flux<Evaluation> findByEvaluateurId(Long evaluateurId);
    Flux<Evaluation> findByEvalueId(Long evalueId);
    Flux<Evaluation> findByTypeEvaluation(EvaluationType typeEvaluation);

    @Query("SELECT * FROM evaluations WHERE evalue_id = :evalueId AND type_evaluation = :type ORDER BY date DESC")
    Flux<Evaluation> findByEvalueIdAndTypeOrderByDateDesc(Long evalueId, EvaluationType type);

    @Query("SELECT * FROM evaluations WHERE evaluateur_id = :evaluateurId ORDER BY date DESC")
    Flux<Evaluation> findByEvaluateurIdOrderByDateDesc(Long evaluateurId);

    @Query("SELECT * FROM evaluations WHERE session_id = :sessionId AND type_evaluation = :type")
    Mono<Evaluation> findBySessionIdAndType(Long sessionId, EvaluationType type);

    // Rating calculations
    @Query("SELECT AVG(note) FROM evaluations WHERE evalue_id = :evalueId AND type_evaluation = :type")
    Mono<Double> getAverageRatingByEvalueAndType(Long evalueId, EvaluationType type);

    @Query("SELECT AVG(note) FROM evaluations WHERE evalue_id = :evalueId AND type_evaluation = :type AND date >= :dateFrom")
    Mono<Double> getAverageRatingByEvalueAndTypeAfterDate(Long evalueId, EvaluationType type, LocalDateTime dateFrom);

    @Query("SELECT MIN(note), MAX(note) FROM evaluations WHERE evalue_id = :evalueId AND type_evaluation = :type")
    Mono<Object[]> getMinMaxRatingByEvalueAndType(Long evalueId, EvaluationType type);

    // Count queries
    @Query("SELECT COUNT(*) FROM evaluations WHERE evalue_id = :evalueId AND type_evaluation = :type")
    Mono<Long> countByEvalueIdAndType(Long evalueId, EvaluationType type);

    @Query("SELECT COUNT(*) FROM evaluations WHERE evalue_id = :evalueId AND note >= :noteMin")
    Mono<Long> countPositiveEvaluations(Long evalueId, int noteMin);

    @Query("SELECT COUNT(*) FROM evaluations WHERE evalue_id = :evalueId AND note <= :noteMax")
    Mono<Long> countNegativeEvaluations(Long evalueId, int noteMax);

    @Query("SELECT COUNT(*) FROM evaluations WHERE evalue_id = :evalueId AND type_evaluation = :type AND date >= :dateFrom")
    Mono<Long> countEvaluationsByEvalueAndTypeAfterDate(Long evalueId, EvaluationType type, LocalDateTime dateFrom);

    // Rating distribution
    @Query("SELECT note, COUNT(*) as count FROM evaluations WHERE evalue_id = :evalueId AND type_evaluation = :type GROUP BY note ORDER BY note")
    Flux<Object[]> getRatingDistributionByEvalueAndType(Long evalueId, EvaluationType type);

    @Query("SELECT note, COUNT(*) as count FROM evaluations WHERE type_evaluation = :type GROUP BY note ORDER BY note")
    Flux<Object[]> getGlobalRatingDistributionByType(EvaluationType type);

    // Time-based analytics
    @Query("SELECT DATE_TRUNC('month', date) as month, AVG(note) as average FROM evaluations WHERE evalue_id = :evalueId AND type_evaluation = :type GROUP BY month ORDER BY month")
    Flux<Object[]> getMonthlyAverageRatingByEvalueAndType(Long evalueId, EvaluationType type);

    @Query("SELECT * FROM evaluations WHERE evalue_id = :evalueId AND type_evaluation = :type AND date >= :dateFrom AND date <= :dateTo ORDER BY date")
    Flux<Evaluation> findByEvalueAndTypeBetweenDates(Long evalueId, EvaluationType type, LocalDateTime dateFrom, LocalDateTime dateTo);

    @Query("SELECT * FROM evaluations WHERE evalue_id = :evalueId AND type_evaluation = :type ORDER BY date DESC LIMIT :limit")
    Flux<Evaluation> findRecentEvaluationsByEvalueAndType(Long evalueId, EvaluationType type, int limit);

    // Advanced statistics
    @Query("SELECT AVG(note) FROM evaluations WHERE type_evaluation = :type")
    Mono<Double> getGlobalAverageRatingByType(EvaluationType type);

    @Query("""
        SELECT evalue_id, AVG(note) as average_rating, COUNT(*) as total_evaluations 
        FROM evaluations 
        WHERE type_evaluation = :type 
        GROUP BY evalue_id 
        HAVING COUNT(*) >= :minEvaluations 
        ORDER BY average_rating DESC 
        LIMIT :limit
        """)
    Flux<Object[]> getTopRatedUsersByType(EvaluationType type, int minEvaluations, int limit);

    @Query("""
        SELECT evalue_id, AVG(note) as average_rating, COUNT(*) as total_evaluations 
        FROM evaluations 
        WHERE type_evaluation = :type AND date >= :dateFrom
        GROUP BY evalue_id 
        ORDER BY average_rating DESC 
        LIMIT :limit
        """)
    Flux<Object[]> getTopRatedUsersByTypeAfterDate(EvaluationType type, LocalDateTime dateFrom, int limit);

    // Subject-based analytics (requires join with sessions table)
    @Query("""
        SELECT m.nom as matiere, AVG(e.note) as average_rating, COUNT(*) as total_evaluations
        FROM evaluations e
        JOIN sessions s ON e.session_id = s.id
        JOIN matieres m ON s.matiere_id = m.id
        WHERE e.evalue_id = :evalueId AND e.type_evaluation = :type
        GROUP BY m.nom
        ORDER BY average_rating DESC
        """)
    Flux<Object[]> getRatingBySubjectForUser(Long evalueId, EvaluationType type);

    @Query("""
        SELECT s.matiere_id, AVG(e.note) as average_rating
        FROM evaluations e
        JOIN sessions s ON e.session_id = s.id
        WHERE e.evalue_id = :evalueId AND e.type_evaluation = :type
        GROUP BY s.matiere_id
        ORDER BY average_rating DESC
        LIMIT 1
        """)
    Mono<Object[]> getBestSubjectForUser(Long evalueId, EvaluationType type);

    // Comment analysis
    @Query("SELECT * FROM evaluations WHERE evalue_id = :evalueId AND type_evaluation = :type AND commentaire IS NOT NULL AND commentaire != '' ORDER BY date DESC")
    Flux<Evaluation> findEvaluationsWithCommentsByEvalueAndType(Long evalueId, EvaluationType type);

    @Query("SELECT COUNT(*) FROM evaluations WHERE evalue_id = :evalueId AND type_evaluation = :type AND commentaire IS NOT NULL AND commentaire != ''")
    Mono<Long> countEvaluationsWithCommentsByEvalueAndType(Long evalueId, EvaluationType type);

    // Comparative analytics
    @Query("""
        SELECT 
            COUNT(CASE WHEN note >= 4 THEN 1 END) as positive_count,
            COUNT(CASE WHEN note <= 2 THEN 1 END) as negative_count,
            COUNT(*) as total_count
        FROM evaluations 
        WHERE evalue_id = :evalueId AND type_evaluation = :type
        """)
    Mono<Object[]> getEvaluationSentimentByEvalueAndType(Long evalueId, EvaluationType type);

    @Query("""
        SELECT 
            e1.evalue_id,
            AVG(e1.note) as user_average,
            (SELECT AVG(note) FROM evaluations WHERE type_evaluation = e1.type_evaluation) as global_average
        FROM evaluations e1
        WHERE e1.evalue_id = :evalueId AND e1.type_evaluation = :type
        GROUP BY e1.evalue_id
        """)
    Mono<Object[]> getUserVsGlobalAverageComparison(Long evalueId, EvaluationType type);

    // Trend analysis
    @Query("""
        WITH monthly_ratings AS (
            SELECT 
                DATE_TRUNC('month', date) as month,
                AVG(note) as avg_rating
            FROM evaluations 
            WHERE evalue_id = :evalueId AND type_evaluation = :type 
            GROUP BY month
            ORDER BY month
        )
        SELECT 
            month,
            avg_rating,
            LAG(avg_rating) OVER (ORDER BY month) as previous_rating
        FROM monthly_ratings
        ORDER BY month DESC
        LIMIT :months
        """)
    Flux<Object[]> getMonthlyRatingTrend(Long evalueId, EvaluationType type, int months);

    // Session completion rate with evaluations
    @Query("""
        SELECT 
            COUNT(DISTINCT s.id) as total_sessions,
            COUNT(DISTINCT e.session_id) as evaluated_sessions
        FROM sessions s
        LEFT JOIN evaluations e ON s.id = e.session_id AND e.type_evaluation = :type
        WHERE (s.tuteur_id = :userId OR s.etudiant_id = :userId)
        AND s.statut = 'TERMINEE'
        """)
    Mono<Object[]> getSessionEvaluationCompletionRate(Long userId, EvaluationType type);

    // Latest evaluations for dashboard
    @Query("SELECT * FROM evaluations WHERE note >= :noteMin ORDER BY date DESC LIMIT :limit")
    Flux<Evaluation> findRecentPositiveEvaluations(int noteMin, int limit);

    @Query("SELECT * FROM evaluations WHERE evalue_id = :evalueId ORDER BY date DESC LIMIT :limit")
    Flux<Evaluation> findRecentEvaluationsForUser(Long evalueId, int limit);

    // User activity analytics
    @Query("SELECT MIN(date), MAX(date) FROM evaluations WHERE evalue_id = :evalueId AND type_evaluation = :type")
    Mono<Object[]> getEvaluationDateRangeByEvalueAndType(Long evalueId, EvaluationType type);

    @Query("""
        SELECT 
            DATE_TRUNC('week', date) as week,
            COUNT(*) as evaluation_count
        FROM evaluations 
        WHERE evalue_id = :evalueId AND type_evaluation = :type
        AND date >= :dateFrom
        GROUP BY week
        ORDER BY week
        """)
    Flux<Object[]> getWeeklyEvaluationActivity(Long evalueId, EvaluationType type, LocalDateTime dateFrom);
}