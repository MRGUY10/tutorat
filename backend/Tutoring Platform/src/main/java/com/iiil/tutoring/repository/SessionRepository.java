package com.iiil.tutoring.repository;

import com.iiil.tutoring.entity.Session;
import com.iiil.tutoring.enums.SessionStatus;
import com.iiil.tutoring.enums.SessionType;
import com.iiil.tutoring.repository.result.SessionDetailsResult;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Repository for Session entity
 */
public interface SessionRepository extends R2dbcRepository<Session, Long> {

    Flux<Session> findByTuteurId(Long tuteurId);

    Flux<Session> findByEtudiantId(Long etudiantId);

    Flux<Session> findByStatut(SessionStatus statut);

    Flux<Session> findByTypeSession(SessionType typeSession);

    @Query("SELECT * FROM sessions WHERE tuteur_id = :tuteurId AND statut = :statut ORDER BY date_heure DESC")
    Flux<Session> findByTuteurIdAndStatutOrderByDateHeureDesc(Long tuteurId, SessionStatus statut);

    @Query("SELECT * FROM sessions WHERE etudiant_id = :etudiantId AND statut = :statut ORDER BY date_heure DESC")
    Flux<Session> findByEtudiantIdAndStatutOrderByDateHeureDesc(Long etudiantId, SessionStatus statut);

    @Query("SELECT * FROM sessions WHERE date_heure BETWEEN :debut AND :fin ORDER BY date_heure ASC")
    Flux<Session> findByDateHeureBetween(LocalDateTime debut, LocalDateTime fin);

    @Query("SELECT * FROM sessions WHERE tuteur_id = :tuteurId AND date_heure >= :dateDebut ORDER BY date_heure ASC")
    Flux<Session> findUpcomingSessionsByTuteur(Long tuteurId, LocalDateTime dateDebut);

    @Query("SELECT * FROM sessions WHERE etudiant_id = :etudiantId AND date_heure >= :dateDebut ORDER BY date_heure ASC")
    Flux<Session> findUpcomingSessionsByEtudiant(Long etudiantId, LocalDateTime dateDebut);

    @Query("SELECT * FROM sessions WHERE matiere_id = :matiereId AND statut = 'TERMINEE' ORDER BY date_heure DESC")
    Flux<Session> findCompletedSessionsByMatiere(Long matiereId);

    @Query("SELECT COUNT(*) FROM sessions WHERE tuteur_id = :tuteurId AND statut = 'TERMINEE'")
    Mono<Long> countCompletedSessionsByTuteur(Long tuteurId);

    @Query("SELECT COUNT(*) FROM sessions WHERE etudiant_id = :etudiantId AND statut = 'TERMINEE'")
    Mono<Long> countCompletedSessionsByEtudiant(Long etudiantId);

    @Query("SELECT SUM(prix) FROM sessions WHERE tuteur_id = :tuteurId AND statut = 'TERMINEE'")
    Mono<Double> getTotalEarningsByTuteur(Long tuteurId);

    @Query("SELECT * FROM sessions WHERE statut = 'CONFIRMEE' AND date_heure < :now")
    Flux<Session> findExpiredConfirmedSessions(LocalDateTime now);

    // For scheduling service - conflict detection
    @Query("SELECT * FROM sessions WHERE tuteur_id = :tuteurId AND " +
           "((date_heure BETWEEN :startTime AND :endTime) OR " +
           "(date_heure + INTERVAL duree MINUTE BETWEEN :startTime AND :endTime) OR " +
           "(date_heure <= :startTime AND date_heure + INTERVAL duree MINUTE >= :endTime))")
    Flux<Session> findByTuteurIdAndDateRange(Long tuteurId, LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT * FROM sessions WHERE etudiant_id = :etudiantId AND " +
           "((date_heure BETWEEN :startTime AND :endTime) OR " +
           "(date_heure + INTERVAL duree MINUTE BETWEEN :startTime AND :endTime) OR " +
           "(date_heure <= :startTime AND date_heure + INTERVAL duree MINUTE >= :endTime))")
    Flux<Session> findByEtudiantIdAndDateRange(Long etudiantId, LocalDateTime startTime, LocalDateTime endTime);

    // ===============================================
    // ENHANCED QUERIES WITH JOIN - SESSIONS
    // ===============================================

    @Query("""
        SELECT s.id, s.tuteur_id, s.etudiant_id, s.matiere_id, s.demande_session_id,
               s.date_heure, s.duree, s.statut, s.prix, s.type_session,
               s.lien_visio, s.notes, s.salle, s.created_at, s.updated_at,
               ue.nom as etudiant_nom, ue.prenom as etudiant_prenom,
               ue.email as etudiant_email, ue.telephone as etudiant_telephone, ue.status as etudiant_status,
               ut.nom as tuteur_nom, ut.prenom as tuteur_prenom,
               ut.email as tuteur_email, ut.telephone as tuteur_telephone, ut.status as tuteur_status,
               t.specialite as tuteur_specialite, t.tarif_horaire as tuteur_tarif_horaire,
               t.verifie as tuteur_verified,
               m.nom as matiere_nom, m.description as matiere_description,
               ds.statut as demande_statut, ds.created_at as demande_created_at, ds.message as demande_message
        FROM sessions s
        JOIN users ue ON s.etudiant_id = ue.id
        JOIN users ut ON s.tuteur_id = ut.id
        JOIN tutors t ON s.tuteur_id = t.id
        JOIN matieres m ON s.matiere_id = m.id
        LEFT JOIN demandes_session ds ON s.demande_session_id = ds.id
        WHERE s.id = :id
        """)
    Mono<SessionDetailsResult> findDetailedSessionById(Long id);

    @Query("""
        SELECT s.id, s.tuteur_id, s.etudiant_id, s.matiere_id, s.demande_session_id,
               s.date_heure, s.duree, s.statut, s.prix, s.type_session,
               s.lien_visio, s.notes, s.salle, s.created_at, s.updated_at,
               ue.nom as etudiant_nom, ue.prenom as etudiant_prenom,
               ue.email as etudiant_email, ue.telephone as etudiant_telephone, ue.status as etudiant_status,
               ut.nom as tuteur_nom, ut.prenom as tuteur_prenom,
               ut.email as tuteur_email, ut.telephone as tuteur_telephone, ut.status as tuteur_status,
               t.specialite as tuteur_specialite, t.tarif_horaire as tuteur_tarif_horaire,
               t.verifie as tuteur_verified,
               m.nom as matiere_nom, m.description as matiere_description,
               ds.statut as demande_statut, ds.created_at as demande_created_at, ds.message as demande_message
        FROM sessions s
        JOIN users ue ON s.etudiant_id = ue.id
        JOIN users ut ON s.tuteur_id = ut.id
        JOIN tutors t ON s.tuteur_id = t.id
        JOIN matieres m ON s.matiere_id = m.id
        LEFT JOIN demandes_session ds ON s.demande_session_id = ds.id
        WHERE s.tuteur_id = :tuteurId
        ORDER BY s.date_heure DESC
        """)
    Flux<SessionDetailsResult> findDetailedSessionsByTutor(Long tuteurId);

    @Query("""
        SELECT s.id, s.tuteur_id, s.etudiant_id, s.matiere_id, s.demande_session_id,
               s.date_heure, s.duree, s.statut, s.prix, s.type_session,
               s.lien_visio, s.notes, s.salle, s.created_at, s.updated_at,
               ue.nom as etudiant_nom, ue.prenom as etudiant_prenom,
               ue.email as etudiant_email, ue.telephone as etudiant_telephone, ue.status as etudiant_status,
               ut.nom as tuteur_nom, ut.prenom as tuteur_prenom,
               ut.email as tuteur_email, ut.telephone as tuteur_telephone, ut.status as tuteur_status,
               t.specialite as tuteur_specialite, t.tarif_horaire as tuteur_tarif_horaire,
               t.verifie as tuteur_verified,
               m.nom as matiere_nom, m.description as matiere_description,
               ds.statut as demande_statut, ds.created_at as demande_created_at, ds.message as demande_message
        FROM sessions s
        JOIN users ue ON s.etudiant_id = ue.id
        JOIN users ut ON s.tuteur_id = ut.id
        JOIN tutors t ON s.tuteur_id = t.id
        JOIN matieres m ON s.matiere_id = m.id
        LEFT JOIN demandes_session ds ON s.demande_session_id = ds.id
        WHERE s.etudiant_id = :etudiantId
        ORDER BY s.date_heure DESC
        """)
    Flux<SessionDetailsResult> findDetailedSessionsByStudent(Long etudiantId);

    @Query("""
        SELECT s.id, s.tuteur_id, s.etudiant_id, s.matiere_id, s.demande_session_id,
               s.date_heure, s.duree, s.statut, s.prix, s.type_session,
               s.lien_visio, s.notes, s.salle, s.created_at, s.updated_at,
               ue.nom as etudiant_nom, ue.prenom as etudiant_prenom,
               ue.email as etudiant_email, ue.telephone as etudiant_telephone, ue.status as etudiant_status,
               ut.nom as tuteur_nom, ut.prenom as tuteur_prenom,
               ut.email as tuteur_email, ut.telephone as tuteur_telephone, ut.status as tuteur_status,
               t.specialite as tuteur_specialite, t.tarif_horaire as tuteur_tarif_horaire,
               t.verifie as tuteur_verified,
               m.nom as matiere_nom, m.description as matiere_description,
               ds.statut as demande_statut, ds.created_at as demande_created_at, ds.message as demande_message
        FROM sessions s
        JOIN users ue ON s.etudiant_id = ue.id
        JOIN users ut ON s.tuteur_id = ut.id
        JOIN tutors t ON s.tuteur_id = t.id
        JOIN matieres m ON s.matiere_id = m.id
        LEFT JOIN demandes_session ds ON s.demande_session_id = ds.id
        WHERE s.statut = :statut
        ORDER BY s.date_heure DESC
        """)
    Flux<SessionDetailsResult> findDetailedSessionsByStatus(SessionStatus statut);

    @Query("""
        SELECT s.id, s.tuteur_id, s.etudiant_id, s.matiere_id, s.demande_session_id,
               s.date_heure, s.duree, s.statut, s.prix, s.type_session,
               s.lien_visio, s.notes, s.salle, s.created_at, s.updated_at,
               ue.nom as etudiant_nom, ue.prenom as etudiant_prenom,
               ue.email as etudiant_email, ue.telephone as etudiant_telephone, ue.status as etudiant_status,
               ut.nom as tuteur_nom, ut.prenom as tuteur_prenom,
               ut.email as tuteur_email, ut.telephone as tuteur_telephone, ut.status as tuteur_status,
               t.specialite as tuteur_specialite, t.tarif_horaire as tuteur_tarif_horaire,
               t.verifie as tuteur_verified,
               m.nom as matiere_nom, m.description as matiere_description,
               ds.statut as demande_statut, ds.created_at as demande_created_at, ds.message as demande_message
        FROM sessions s
        JOIN users ue ON s.etudiant_id = ue.id
        JOIN users ut ON s.tuteur_id = ut.id
        JOIN tutors t ON s.tuteur_id = t.id
        JOIN matieres m ON s.matiere_id = m.id
        LEFT JOIN demandes_session ds ON s.demande_session_id = ds.id
        WHERE s.date_heure BETWEEN :dateDebut AND :dateFin
        AND (:statut IS NULL OR s.statut = :statut)
        ORDER BY s.date_heure ASC
        """)
    Flux<SessionDetailsResult> findDetailedSessionsByDateRange(LocalDateTime dateDebut, LocalDateTime dateFin, SessionStatus statut);

    @Query("""
        SELECT s.id, s.tuteur_id, s.etudiant_id, s.matiere_id, s.demande_session_id,
               s.date_heure, s.duree, s.statut, s.prix, s.type_session,
               s.lien_visio, s.notes, s.salle, s.created_at, s.updated_at,
               ue.nom as etudiant_nom, ue.prenom as etudiant_prenom,
               ue.email as etudiant_email, ue.telephone as etudiant_telephone, ue.status as etudiant_status,
               ut.nom as tuteur_nom, ut.prenom as tuteur_prenom,
               ut.email as tuteur_email, ut.telephone as tuteur_telephone, ut.status as tuteur_status,
               t.specialite as tuteur_specialite, t.tarif_horaire as tuteur_tarif_horaire,
               t.verifie as tuteur_verified,
               m.nom as matiere_nom, m.description as matiere_description,
               ds.statut as demande_statut, ds.created_at as demande_created_at, ds.message as demande_message
        FROM sessions s
        JOIN users ue ON s.etudiant_id = ue.id
        JOIN users ut ON s.tuteur_id = ut.id
        JOIN tutors t ON s.tuteur_id = t.id
        JOIN matieres m ON s.matiere_id = m.id
        LEFT JOIN demandes_session ds ON s.demande_session_id = ds.id
        WHERE s.matiere_id = :matiereId
        AND (:statut IS NULL OR s.statut = :statut)
        ORDER BY s.date_heure DESC
        """)
    Flux<SessionDetailsResult> findDetailedSessionsBySubject(Long matiereId, SessionStatus statut);

    @Query("""
        SELECT s.id, s.tuteur_id, s.etudiant_id, s.matiere_id, s.demande_session_id,
               s.date_heure, s.duree, s.statut, s.prix, s.type_session,
               s.lien_visio, s.notes, s.salle, s.created_at, s.updated_at,
               ue.nom as etudiant_nom, ue.prenom as etudiant_prenom,
               ue.email as etudiant_email, ue.telephone as etudiant_telephone, ue.status as etudiant_status,
               ut.nom as tuteur_nom, ut.prenom as tuteur_prenom,
               ut.email as tuteur_email, ut.telephone as tuteur_telephone, ut.status as tuteur_status,
               t.specialite as tuteur_specialite, t.tarif_horaire as tuteur_tarif_horaire,
               t.verifie as tuteur_verified,
               m.nom as matiere_nom, m.description as matiere_description,
               ds.statut as demande_statut, ds.created_at as demande_created_at, ds.message as demande_message
        FROM sessions s
        JOIN users ue ON s.etudiant_id = ue.id
        JOIN users ut ON s.tuteur_id = ut.id
        JOIN tutors t ON s.tuteur_id = t.id
        JOIN matieres m ON s.matiere_id = m.id
        LEFT JOIN demandes_session ds ON s.demande_session_id = ds.id
        WHERE s.date_heure >= :dateDebut
        AND (:tuteurId IS NULL OR s.tuteur_id = :tuteurId)
        AND (:etudiantId IS NULL OR s.etudiant_id = :etudiantId)
        ORDER BY s.date_heure ASC
        """)
    Flux<SessionDetailsResult> findUpcomingDetailedSessions(LocalDateTime dateDebut, Long tuteurId, Long etudiantId);

    @Query("""
        SELECT s.id, s.tuteur_id, s.etudiant_id, s.matiere_id, s.demande_session_id,
               s.date_heure, s.duree, s.statut, s.prix, s.type_session,
               s.lien_visio, s.notes, s.salle, s.created_at, s.updated_at,
               ue.nom as etudiant_nom, ue.prenom as etudiant_prenom,
               ue.email as etudiant_email, ue.telephone as etudiant_telephone, ue.status as etudiant_status,
               ut.nom as tuteur_nom, ut.prenom as tuteur_prenom,
               ut.email as tuteur_email, ut.telephone as tuteur_telephone, ut.status as tuteur_status,
               t.specialite as tuteur_specialite, t.tarif_horaire as tuteur_tarif_horaire,
               t.verifie as tuteur_verified,
               m.nom as matiere_nom, m.description as matiere_description,
               ds.statut as demande_statut, ds.created_at as demande_created_at, ds.message as demande_message
        FROM sessions s
        JOIN users ue ON s.etudiant_id = ue.id
        JOIN users ut ON s.tuteur_id = ut.id
        JOIN tutors t ON s.tuteur_id = t.id
        JOIN matieres m ON s.matiere_id = m.id
        LEFT JOIN demandes_session ds ON s.demande_session_id = ds.id
        WHERE (LOWER(s.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
               OR LOWER(m.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
               OR LOWER(ue.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
               OR LOWER(ue.prenom) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
               OR LOWER(ut.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
               OR LOWER(ut.prenom) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        AND (:statut IS NULL OR s.statut = :statut)
        ORDER BY s.date_heure DESC
        """)
    Flux<SessionDetailsResult> searchDetailedSessions(String searchTerm, SessionStatus statut);

    // Advanced filtering query
    @Query("""
        SELECT s.id, s.tuteur_id, s.etudiant_id, s.matiere_id, s.demande_session_id,
               s.date_heure, s.duree, s.statut, s.prix, s.type_session,
               s.lien_visio, s.notes, s.salle, s.created_at, s.updated_at,
               ue.nom as etudiant_nom, ue.prenom as etudiant_prenom,
               ue.email as etudiant_email, ue.telephone as etudiant_telephone, ue.status as etudiant_status,
               ut.nom as tuteur_nom, ut.prenom as tuteur_prenom,
               ut.email as tuteur_email, ut.telephone as tuteur_telephone, ut.status as tuteur_status,
               t.specialite as tuteur_specialite, t.tarif_horaire as tuteur_tarif_horaire,
               t.verifie as tuteur_verified,
               m.nom as matiere_nom, m.description as matiere_description,
               ds.statut as demande_statut, ds.created_at as demande_created_at, ds.message as demande_message
        FROM sessions s
        JOIN users ue ON s.etudiant_id = ue.id
        JOIN users ut ON s.tuteur_id = ut.id
        JOIN tutors t ON s.tuteur_id = t.id
        JOIN matieres m ON s.matiere_id = m.id
        LEFT JOIN demandes_session ds ON s.demande_session_id = ds.id
        WHERE (:tuteurId IS NULL OR s.tuteur_id = :tuteurId)
        AND (:etudiantId IS NULL OR s.etudiant_id = :etudiantId)
        AND (:matiereId IS NULL OR s.matiere_id = :matiereId)
        AND (:statut IS NULL OR s.statut = :statut)
        AND (:typeSession IS NULL OR s.type_session = :typeSession)
        AND (:dateDebut IS NULL OR s.date_heure >= :dateDebut)
        AND (:dateFin IS NULL OR s.date_heure <= :dateFin)
        AND (:prixMin IS NULL OR s.prix >= :prixMin)
        AND (:prixMax IS NULL OR s.prix <= :prixMax)
        ORDER BY s.date_heure DESC
        """)
    Flux<SessionDetailsResult> findDetailedSessionsWithFilters(
        Long tuteurId, Long etudiantId, Long matiereId, SessionStatus statut, SessionType typeSession,
        LocalDateTime dateDebut, LocalDateTime dateFin, Double prixMin, Double prixMax);

    // Today's sessions
    @Query("""
        SELECT s.id, s.tuteur_id, s.etudiant_id, s.matiere_id, s.demande_session_id,
               s.date_heure, s.duree, s.statut, s.prix, s.type_session,
               s.lien_visio, s.notes, s.salle, s.created_at, s.updated_at,
               ue.nom as etudiant_nom, ue.prenom as etudiant_prenom,
               ue.email as etudiant_email, ue.telephone as etudiant_telephone, ue.status as etudiant_status,
               ut.nom as tuteur_nom, ut.prenom as tuteur_prenom,
               ut.email as tuteur_email, ut.telephone as tuteur_telephone, ut.status as tuteur_status,
               t.specialite as tuteur_specialite, t.tarif_horaire as tuteur_tarif_horaire,
               t.verifie as tuteur_verified,
               m.nom as matiere_nom, m.description as matiere_description,
               ds.statut as demande_statut, ds.created_at as demande_created_at, ds.message as demande_message
        FROM sessions s
        JOIN users ue ON s.etudiant_id = ue.id
        JOIN users ut ON s.tuteur_id = ut.id
        JOIN tutors t ON s.tuteur_id = t.id
        JOIN matieres m ON s.matiere_id = m.id
        LEFT JOIN demandes_session ds ON s.demande_session_id = ds.id
        WHERE DATE(s.date_heure) = CURRENT_DATE
        AND (:tuteurId IS NULL OR s.tuteur_id = :tuteurId)
        AND (:etudiantId IS NULL OR s.etudiant_id = :etudiantId)
        ORDER BY s.date_heure ASC
        """)
    Flux<SessionDetailsResult> findTodaysDetailedSessions(Long tuteurId, Long etudiantId);
}
