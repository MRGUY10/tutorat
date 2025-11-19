package com.iiil.tutoring.repository;

import com.iiil.tutoring.entity.DemandeSession;
import com.iiil.tutoring.enums.RequestStatus;
import com.iiil.tutoring.enums.Urgence;
import com.iiil.tutoring.repository.result.SessionRequestDetailsResult;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Repository for DemandeSession entity with enhanced queries
 */
public interface DemandeSessionRepository extends R2dbcRepository<DemandeSession, Long> {

    // Basic queries
    Flux<DemandeSession> findByEtudiantId(Long etudiantId);

    Flux<DemandeSession> findByTuteurId(Long tuteurId);

    Flux<DemandeSession> findByStatut(RequestStatus statut);

    Flux<DemandeSession> findByUrgence(Urgence urgence);

    @Query("SELECT * FROM demande_sessions WHERE tuteur_id = :tuteurId AND statut = :statut ORDER BY date_creation DESC")
    Flux<DemandeSession> findByTuteurIdAndStatutOrderByDateCreationDesc(Long tuteurId, RequestStatus statut);

    @Query("SELECT * FROM demande_sessions WHERE etudiant_id = :etudiantId AND statut = :statut ORDER BY date_creation DESC")
    Flux<DemandeSession> findByEtudiantIdAndStatutOrderByDateCreationDesc(Long etudiantId, RequestStatus statut);

    @Query("SELECT * FROM demande_sessions WHERE statut = 'EN_ATTENTE' AND urgence = :urgence ORDER BY date_creation ASC")
    Flux<DemandeSession> findPendingRequestsByUrgence(Urgence urgence);

    @Query("SELECT * FROM demande_sessions WHERE matiere_id = :matiereId AND statut = 'EN_ATTENTE' ORDER BY urgence DESC, date_creation ASC")
    Flux<DemandeSession> findPendingRequestsByMatiere(Long matiereId);

    @Query("SELECT * FROM demande_sessions WHERE date_voulue BETWEEN :debut AND :fin AND statut = 'EN_ATTENTE'")
    Flux<DemandeSession> findPendingRequestsByDateRange(LocalDateTime debut, LocalDateTime fin);

    @Query("SELECT COUNT(*) FROM demande_sessions WHERE tuteur_id = :tuteurId AND statut = :statut")
    Mono<Long> countByTuteurIdAndStatut(Long tuteurId, RequestStatus statut);

    @Query("SELECT COUNT(*) FROM demande_sessions WHERE etudiant_id = :etudiantId AND statut = :statut")
    Mono<Long> countByEtudiantIdAndStatut(Long etudiantId, RequestStatus statut);

    @Query("SELECT * FROM demande_sessions WHERE statut = 'EN_ATTENTE' AND date_creation < :dateLimit")
    Flux<DemandeSession> findOldPendingRequests(LocalDateTime dateLimit);

    // ===============================================
    // ENHANCED QUERIES WITH JOIN - SESSION REQUESTS
    // ===============================================

    @Query("""
       SELECT
    ds.id,
    ds.etudiant_id,
    ds.tuteur_id,
    ds.matiere_id,
    ds.date_creation,
    ds.date_heure_souhaitee,
    ds.description,
    ds.statut,
    ds.urgence,
    ds.duree,
    ds.tarif_propose,
    ds.commentaire_tuteur,
    ds.date_reponse,
    ue.nom AS etudiant_nom,
    ue.prenom AS etudiant_prenom,
    ue.email AS etudiant_email,
    ue.telephone AS etudiant_telephone,
    ue.statut AS etudiant_statut,
    ut.nom AS tuteur_nom,
    ut.prenom AS tuteur_prenom,
    ut.email AS tuteur_email,
    ut.telephone AS tuteur_telephone,
    ut.statut AS tuteur_statut,
    t.tarif_horaire AS tuteur_tarif_horaire,
    t.verifie AS tuteur_verifie,
    m.nom AS matiere_nom,
    m.description AS matiere_description,
    s.id AS session_id,
    s.date_heure AS session_date_heure,
    s.statut AS session_statut
FROM demande_sessions ds
         JOIN users ue ON ds.etudiant_id = ue.id
         LEFT JOIN users ut ON ds.tuteur_id = ut.id
         LEFT JOIN tutors t ON ds.tuteur_id = t.id
         JOIN matieres m ON ds.matiere_id = m.id
         LEFT JOIN sessions s ON ds.id = s.demande_session_id
WHERE ds.id = :id;
        """)
    Mono<SessionRequestDetailsResult> findDetailedRequestById(Long id);

    @Query("""
        SELECT ds.id, ds.etudiant_id, ds.tuteur_id, ds.matiere_id, ds.date_creation, 
               ds.date_voulue, ds.message, ds.statut, ds.urgence, ds.duree_souhaitee,
               ds.budget_max, ds.reponse_tuteur, ds.date_reponse, ds.created_at, ds.updated_at,
               ue.nom as etudiant_nom, ue.prenom as etudiant_prenom, 
               ue.email as etudiant_email, ue.telephone as etudiant_telephone, ue.status as etudiant_status,
               ut.nom as tuteur_nom, ut.prenom as tuteur_prenom,
               ut.email as tuteur_email, ut.telephone as tuteur_telephone, ut.status as tuteur_status,
               t.specialite as tuteur_specialite, t.tarif_horaire as tuteur_tarif_horaire, 
               t.verified as tuteur_verified,
               m.nom as matiere_nom, m.description as matiere_description,
               s.id as session_id, s.date_heure as session_date_heure, s.statut as session_statut
        FROM demande_sessions ds
        JOIN users ue ON ds.etudiant_id = ue.id
        JOIN users ut ON ds.tuteur_id = ut.id
        JOIN tutors t ON ds.tuteur_id = t.user_id
        JOIN matieres m ON ds.matiere_id = m.id
        LEFT JOIN sessions s ON ds.id = s.demande_session_id
        WHERE ds.etudiant_id = :etudiantId
        ORDER BY ds.date_creation DESC
        """)
    Flux<SessionRequestDetailsResult> findDetailedRequestsByStudent(Long etudiantId);

    @Query("""
        SELECT ds.id, ds.etudiant_id, ds.tuteur_id, ds.matiere_id, ds.date_creation, 
               ds.date_voulue, ds.message, ds.statut, ds.urgence, ds.duree_souhaitee,
               ds.budget_max, ds.reponse_tuteur, ds.date_reponse, ds.created_at, ds.updated_at,
               ue.nom as etudiant_nom, ue.prenom as etudiant_prenom, 
               ue.email as etudiant_email, ue.telephone as etudiant_telephone, ue.status as etudiant_status,
               ut.nom as tuteur_nom, ut.prenom as tuteur_prenom,
               ut.email as tuteur_email, ut.telephone as tuteur_telephone, ut.status as tuteur_status,
               t.specialite as tuteur_specialite, t.tarif_horaire as tuteur_tarif_horaire, 
               t.verified as tuteur_verified,
               m.nom as matiere_nom, m.description as matiere_description,
               s.id as session_id, s.date_heure as session_date_heure, s.statut as session_statut
        FROM demande_sessions ds
        JOIN users ue ON ds.etudiant_id = ue.id
        JOIN users ut ON ds.tuteur_id = ut.id
        JOIN tutors t ON ds.tuteur_id = t.user_id
        JOIN matieres m ON ds.matiere_id = m.id
        LEFT JOIN sessions s ON ds.id = s.demande_session_id
        WHERE ds.tuteur_id = :tuteurId
        ORDER BY ds.date_creation DESC
        """)
    Flux<SessionRequestDetailsResult> findDetailedRequestsByTutor(Long tuteurId);

    @Query("""
        SELECT ds.id, ds.etudiant_id, ds.tuteur_id, ds.matiere_id, ds.date_creation, 
               ds.date_voulue, ds.message, ds.statut, ds.urgence, ds.duree_souhaitee,
               ds.budget_max, ds.reponse_tuteur, ds.date_reponse, ds.created_at, ds.updated_at,
               ue.nom as etudiant_nom, ue.prenom as etudiant_prenom, 
               ue.email as etudiant_email, ue.telephone as etudiant_telephone, ue.status as etudiant_status,
               ut.nom as tuteur_nom, ut.prenom as tuteur_prenom,
               ut.email as tuteur_email, ut.telephone as tuteur_telephone, ut.status as tuteur_status,
               t.description as tuteur_specialite, t.tarif_horaire as tuteur_tarif_horaire, 
               t.verifie as tuteur_verified,
               m.nom as matiere_nom, m.description as matiere_description,
               s.id as session_id, s.date_heure as session_date_heure, s.statut as session_statut
        FROM demande_sessions ds
        JOIN users ue ON ds.etudiant_id = ue.id
        JOIN users ut ON ds.tuteur_id = ut.id
        JOIN tutors t ON ds.tuteur_id = t.id
        JOIN matieres m ON ds.matiere_id = m.id
        LEFT JOIN sessions s ON ds.id = s.demande_session_id
        WHERE ds.statut = :statut
        ORDER BY 
            CASE ds.urgence 
                WHEN 'HAUTE' THEN 1 
                WHEN 'MOYENNE' THEN 2 
                WHEN 'BASSE' THEN 3 
            END, ds.date_creation ASC
        """)
    Flux<SessionRequestDetailsResult> findDetailedRequestsByStatus(RequestStatus statut);

    @Query("""
        SELECT ds.id, ds.etudiant_id, ds.tuteur_id, ds.matiere_id, ds.date_creation, 
               ds.date_voulue, ds.message, ds.statut, ds.urgence, ds.duree_souhaitee,
               ds.budget_max, ds.reponse_tuteur, ds.date_reponse, ds.created_at, ds.updated_at,
               ue.nom as etudiant_nom, ue.prenom as etudiant_prenom, 
               ue.email as etudiant_email, ue.telephone as etudiant_telephone, ue.status as etudiant_status,
               ut.nom as tuteur_nom, ut.prenom as tuteur_prenom,
               ut.email as tuteur_email, ut.telephone as tuteur_telephone, ut.status as tuteur_status,
               t.description as tuteur_specialite, t.tarif_horaire as tuteur_tarif_horaire, 
               t.verifie as tuteur_verified,
               m.nom as matiere_nom, m.description as matiere_description,
               s.id as session_id, s.date_heure as session_date_heure, s.statut as session_statut
        FROM demande_sessions ds
        JOIN users ue ON ds.etudiant_id = ue.id
        JOIN users ut ON ds.tuteur_id = ut.id
        JOIN tutors t ON ds.tuteur_id = t.id
        JOIN matieres m ON ds.matiere_id = m.id
        LEFT JOIN sessions s ON ds.id = s.demande_session_id
        WHERE ds.matiere_id = :matiereId 
        AND (:statut IS NULL OR ds.statut = :statut)
        ORDER BY ds.date_creation DESC
        """)
    Flux<SessionRequestDetailsResult> findDetailedRequestsBySubject(Long matiereId, RequestStatus statut);

    @Query("""
        SELECT ds.id, ds.etudiant_id, ds.tuteur_id, ds.matiere_id, ds.date_creation, 
               ds.date_voulue, ds.message, ds.statut, ds.urgence, ds.duree_souhaitee,
               ds.budget_max, ds.reponse_tuteur, ds.date_reponse, ds.created_at, ds.updated_at,
               ue.nom as etudiant_nom, ue.prenom as etudiant_prenom, 
               ue.email as etudiant_email, ue.telephone as etudiant_telephone, ue.status as etudiant_status,
               ut.nom as tuteur_nom, ut.prenom as tuteur_prenom,
               ut.email as tuteur_email, ut.telephone as tuteur_telephone, ut.status as tuteur_status,
               t.description as tuteur_specialite, t.tarif_horaire as tuteur_tarif_horaire, 
               t.verifie as tuteur_verified,
               m.nom as matiere_nom, m.description as matiere_description,
               s.id as session_id, s.date_heure as session_date_heure, s.statut as session_statut
        FROM demande_sessions ds
        JOIN users ue ON ds.etudiant_id = ue.id
        JOIN users ut ON ds.tuteur_id = ut.id
        JOIN tutors t ON ds.tuteur_id = t.id
        JOIN matieres m ON ds.matiere_id = m.id
        LEFT JOIN sessions s ON ds.id = s.demande_session_id
        WHERE ds.date_voulue BETWEEN :dateDebut AND :dateFin
        AND (:statut IS NULL OR ds.statut = :statut)
        ORDER BY ds.date_voulue ASC
        """)
    Flux<SessionRequestDetailsResult> findDetailedRequestsByDateRange(LocalDateTime dateDebut, LocalDateTime dateFin, RequestStatus statut);

    @Query("""
        SELECT ds.id, ds.etudiant_id, ds.tuteur_id, ds.matiere_id, ds.date_creation, 
               ds.date_voulue, ds.message, ds.statut, ds.urgence, ds.duree_souhaitee,
               ds.budget_max, ds.reponse_tuteur, ds.date_reponse, ds.created_at, ds.updated_at,
               ue.nom as etudiant_nom, ue.prenom as etudiant_prenom, 
               ue.email as etudiant_email, ue.telephone as etudiant_telephone, ue.status as etudiant_status,
               ut.nom as tuteur_nom, ut.prenom as tuteur_prenom,
               ut.email as tuteur_email, ut.telephone as tuteur_telephone, ut.status as tuteur_status,
               t.description as tuteur_specialite, t.tarif_horaire as tuteur_tarif_horaire, 
               t.verifie as tuteur_verified,
               m.nom as matiere_nom, m.description as matiere_description,
               s.id as session_id, s.date_heure as session_date_heure, s.statut as session_statut
        FROM demande_sessions ds
        JOIN users ue ON ds.etudiant_id = ue.id
        JOIN users ut ON ds.tuteur_id = ut.id
        JOIN tutors t ON ds.tuteur_id = t.id
        JOIN matieres m ON ds.matiere_id = m.id
        LEFT JOIN sessions s ON ds.id = s.demande_session_id
        WHERE ds.urgence = :urgence
        AND (:statut IS NULL OR ds.statut = :statut)
        ORDER BY ds.date_creation ASC
        """)
    Flux<SessionRequestDetailsResult> findDetailedRequestsByUrgency(Urgence urgence, RequestStatus statut);

    @Query("""
        SELECT ds.id, ds.etudiant_id, ds.tuteur_id, ds.matiere_id, ds.date_creation, 
               ds.date_voulue, ds.message, ds.statut, ds.urgence, ds.duree_souhaitee,
               ds.budget_max, ds.reponse_tuteur, ds.date_reponse, ds.created_at, ds.updated_at,
               ue.nom as etudiant_nom, ue.prenom as etudiant_prenom, 
               ue.email as etudiant_email, ue.telephone as etudiant_telephone, ue.status as etudiant_status,
               ut.nom as tuteur_nom, ut.prenom as tuteur_prenom,
               ut.email as tuteur_email, ut.telephone as tuteur_telephone, ut.status as tuteur_status,
               t.description as tuteur_specialite, t.tarif_horaire as tuteur_tarif_horaire, 
               t.verifie as tuteur_verified,
               m.nom as matiere_nom, m.description as matiere_description,
               s.id as session_id, s.date_heure as session_date_heure, s.statut as session_statut
        FROM demande_sessions ds
        JOIN users ue ON ds.etudiant_id = ue.id
        JOIN users ut ON ds.tuteur_id = ut.id
        JOIN tutors t ON ds.tuteur_id = t.id
        JOIN matieres m ON ds.matiere_id = m.id
        LEFT JOIN sessions s ON ds.id = s.demande_session_id
        WHERE (LOWER(ds.message) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
               OR LOWER(m.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
               OR LOWER(ue.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
               OR LOWER(ue.prenom) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
               OR LOWER(ut.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
               OR LOWER(ut.prenom) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        AND (:statut IS NULL OR ds.statut = :statut)
        ORDER BY ds.date_creation DESC
        """)
    Flux<SessionRequestDetailsResult> searchDetailedRequests(String searchTerm, RequestStatus statut);

    // Advanced filtering query
    @Query("""
        SELECT ds.id, ds.etudiant_id, ds.tuteur_id, ds.matiere_id, ds.date_creation, 
               ds.date_voulue, ds.message, ds.statut, ds.urgence, ds.duree_souhaitee,
               ds.budget_max, ds.reponse_tuteur, ds.date_reponse, ds.created_at, ds.updated_at,
               ue.nom as etudiant_nom, ue.prenom as etudiant_prenom, 
               ue.email as etudiant_email, ue.telephone as etudiant_telephone, ue.status as etudiant_status,
               ut.nom as tuteur_nom, ut.prenom as tuteur_prenom,
               ut.email as tuteur_email, ut.telephone as tuteur_telephone, ut.status as tuteur_status,
               t.description as tuteur_specialite, t.tarif_horaire as tuteur_tarif_horaire, 
               t.verifie as tuteur_verified,
               m.nom as matiere_nom, m.description as matiere_description,
               s.id as session_id, s.date_heure as session_date_heure, s.statut as session_statut
        FROM demande_sessions ds
        JOIN users ue ON ds.etudiant_id = ue.id
        JOIN users ut ON ds.tuteur_id = ut.id
        JOIN tutors t ON ds.tuteur_id = t.id
        JOIN matieres m ON ds.matiere_id = m.id
        LEFT JOIN sessions s ON ds.id = s.demande_session_id
        WHERE (:tuteurId IS NULL OR ds.tuteur_id = :tuteurId)
        AND (:etudiantId IS NULL OR ds.etudiant_id = :etudiantId)
        AND (:matiereId IS NULL OR ds.matiere_id = :matiereId)
        AND (:statut IS NULL OR ds.statut = :statut)
        AND (:urgence IS NULL OR ds.urgence = :urgence)
        AND (:dateDebut IS NULL OR ds.date_voulue >= :dateDebut)
        AND (:dateFin IS NULL OR ds.date_voulue <= :dateFin)
        AND (:budgetMin IS NULL OR ds.budget_max >= :budgetMin)
        AND (:budgetMax IS NULL OR ds.budget_max <= :budgetMax)
        ORDER BY ds.date_creation DESC
        """)
    Flux<SessionRequestDetailsResult> findDetailedRequestsWithFilters(
        Long tuteurId, Long etudiantId, Long matiereId, RequestStatus statut, Urgence urgence,
        LocalDateTime dateDebut, LocalDateTime dateFin, Double budgetMin, Double budgetMax);
}
