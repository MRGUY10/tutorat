package com.iiil.tutoring.service.session;

import com.iiil.tutoring.dto.session.*;
import com.iiil.tutoring.entity.Session;
import com.iiil.tutoring.enums.SessionStatus;
import com.iiil.tutoring.enums.SessionType;
import com.iiil.tutoring.repository.SessionRepository;
import com.iiil.tutoring.repository.result.SessionDetailsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Service for managing sessions
 */
@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final SessionRepository sessionRepository;

    @Autowired
    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    // ===============================================
    // CRUD OPERATIONS
    // ===============================================

    /**
     * Create a new session
     */
    public Mono<SessionResponseDTO> createSession(CreateSessionDTO createDTO) {
        Session session = new Session();
        session.setTuteurId(createDTO.getTuteurId());
        session.setEtudiantId(createDTO.getEtudiantId());
        session.setMatiereId(createDTO.getMatiereId());
        session.setDemandeSessionId(createDTO.getDemandeSessionId());
        session.setDateHeure(createDTO.getDateHeure());
        session.setDuree(createDTO.getDuree());
        session.setTypeSession(createDTO.getTypeSession());
        session.setPrix(createDTO.getPrix());
        session.setLienVisio(createDTO.getLienVisio());
        session.setNotes(createDTO.getNotes());
        session.setSalle(createDTO.getSalle());
        
        // Set initial status based on confirmation requirement
        if (createDTO.isRequiresConfirmation()) {
            session.setStatut(SessionStatus.DEMANDEE);
        } else {
            session.setStatut(SessionStatus.CONFIRMEE);
        }

        return sessionRepository.save(session)
                .map(this::mapToResponseDTO);
    }

    /**
     * Create session from accepted request
     */
    public Mono<SessionResponseDTO> createSessionFromRequest(Long demandeSessionId, CreateSessionDTO createDTO) {
        createDTO.setDemandeSessionId(demandeSessionId);
        return createSession(createDTO);
    }

    /**
     * Update an existing session
     */
    public Mono<SessionResponseDTO> updateSession(Long sessionId, UpdateSessionDTO updateDTO) {
        return sessionRepository.findById(sessionId)
                .switchIfEmpty(Mono.error(new RuntimeException("Session not found with id: " + sessionId)))
                .flatMap(existingSession -> {
                    // Check if session can be modified
                    if (!canBeModified(existingSession)) {
                        return Mono.error(new RuntimeException("Session cannot be modified in its current state"));
                    }

                    // Update fields if provided
                    if (updateDTO.getDateHeure() != null) {
                        existingSession.setDateHeure(updateDTO.getDateHeure());
                    }
                    if (updateDTO.getDuree() != null) {
                        existingSession.setDuree(updateDTO.getDuree());
                    }
                    if (updateDTO.getStatut() != null) {
                        existingSession.setStatut(updateDTO.getStatut());
                    }
                    if (updateDTO.getPrix() != null) {
                        existingSession.setPrix(updateDTO.getPrix());
                    }
                    if (updateDTO.getTypeSession() != null) {
                        existingSession.setTypeSession(updateDTO.getTypeSession());
                    }
                    if (updateDTO.getLienVisio() != null) {
                        existingSession.setLienVisio(updateDTO.getLienVisio());
                    }
                    if (updateDTO.getNotes() != null) {
                        existingSession.setNotes(updateDTO.getNotes());
                    }
                    if (updateDTO.getSalle() != null) {
                        existingSession.setSalle(updateDTO.getSalle());
                    }

                    return sessionRepository.save(existingSession);
                })
                .map(this::mapToResponseDTO);
    }

    /**
     * Confirm a session
     */
    public Mono<SessionResponseDTO> confirmSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .switchIfEmpty(Mono.error(new RuntimeException("Session not found with id: " + sessionId)))
                .flatMap(session -> {
                    if (session.getStatut() != SessionStatus.DEMANDEE) {
                        return Mono.error(new RuntimeException("Session cannot be confirmed in its current state"));
                    }
                    session.setStatut(SessionStatus.CONFIRMEE);
                    return sessionRepository.save(session);
                })
                .map(this::mapToResponseDTO);
    }

    /**
     * Start a session
     */
    public Mono<SessionResponseDTO> startSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .switchIfEmpty(Mono.error(new RuntimeException("Session not found with id: " + sessionId)))
                .flatMap(session -> {
                    if (session.getStatut() != SessionStatus.CONFIRMEE) {
                        return Mono.error(new RuntimeException("Session must be confirmed before starting"));
                    }
                    session.setStatut(SessionStatus.EN_COURS);
                    return sessionRepository.save(session);
                })
                .map(this::mapToResponseDTO);
    }

    /**
     * Complete a session
     */
    public Mono<SessionResponseDTO> completeSession(Long sessionId, UpdateSessionDTO completionDTO) {
        return sessionRepository.findById(sessionId)
                .switchIfEmpty(Mono.error(new RuntimeException("Session not found with id: " + sessionId)))
                .flatMap(session -> {
                    if (session.getStatut() != SessionStatus.EN_COURS) {
                        return Mono.error(new RuntimeException("Only active sessions can be completed"));
                    }
                    
                    session.setStatut(SessionStatus.TERMINEE);
                    
                    // Add completion notes if provided
                    if (completionDTO != null && completionDTO.getNotes() != null) {
                        session.setNotes(session.getNotes() + "\n\nCompletion Notes: " + completionDTO.getNotes());
                    }
                    
                    return sessionRepository.save(session);
                })
                .map(this::mapToResponseDTO);
    }

    /**
     * Cancel a session
     */
    public Mono<SessionResponseDTO> cancelSession(Long sessionId, String reason) {
        return sessionRepository.findById(sessionId)
                .switchIfEmpty(Mono.error(new RuntimeException("Session not found with id: " + sessionId)))
                .flatMap(session -> {
                    if (session.getStatut() == SessionStatus.TERMINEE) {
                        return Mono.error(new RuntimeException("Cannot cancel a completed session"));
                    }
                    
                    session.setStatut(SessionStatus.ANNULEE);
                    if (reason != null) {
                        session.setNotes(session.getNotes() + "\n\nCancellation Reason: " + reason);
                    }
                    
                    return sessionRepository.save(session);
                })
                .map(this::mapToResponseDTO);
    }

    /**
     * Reschedule a session
     */
    public Mono<SessionResponseDTO> rescheduleSession(Long sessionId, LocalDateTime newDateTime, String reason) {
        return sessionRepository.findById(sessionId)
                .switchIfEmpty(Mono.error(new RuntimeException("Session not found with id: " + sessionId)))
                .flatMap(session -> {
                    if (!canBeRescheduled(session)) {
                        return Mono.error(new RuntimeException("Session cannot be rescheduled in its current state"));
                    }
                    
                    // Store old date for notes
                    LocalDateTime oldDate = session.getDateHeure();
                    session.setDateHeure(newDateTime);
                    
                    // Add reschedule note
                    String rescheduleNote = "\n\nRescheduled from " + oldDate + " to " + newDateTime;
                    if (reason != null) {
                        rescheduleNote += ". Reason: " + reason;
                    }
                    session.setNotes(session.getNotes() + rescheduleNote);
                    
                    return sessionRepository.save(session);
                })
                .map(this::mapToResponseDTO);
    }

    /**
     * Delete a session
     */
    public Mono<Void> deleteSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .switchIfEmpty(Mono.error(new RuntimeException("Session not found with id: " + sessionId)))
                .flatMap(session -> {
                    if (session.getStatut() == SessionStatus.EN_COURS || session.getStatut() == SessionStatus.TERMINEE) {
                        return Mono.error(new RuntimeException("Cannot delete a session that is in progress or completed"));
                    }
                    return sessionRepository.delete(session);
                });
    }

    // ===============================================
    // QUERY OPERATIONS
    // ===============================================

    /**
     * Get session by ID
     */
    public Mono<SessionResponseDTO> getSessionById(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .switchIfEmpty(Mono.error(new RuntimeException("Session not found with id: " + sessionId)))
                .map(this::mapToResponseDTO);
    }

    /**
     * Get detailed session by ID
     */
    public Mono<SessionDetailsDTO> getDetailedSessionById(Long sessionId) {
        return sessionRepository.findDetailedSessionById(sessionId)
                .switchIfEmpty(Mono.error(new RuntimeException("Session not found with id: " + sessionId)))
                .map(this::mapToDetailsDTO);
    }

    /**
     * Get sessions for a tutor
     */
    public Flux<SessionResponseDTO> getSessionsByTutor(Long tuteurId) {
        log.info("Getting sessions for tutor: {}", tuteurId);
        return sessionRepository.findByTuteurId(tuteurId)
                .doOnNext(session -> log.debug("Found session: {} for tutor {}", session.getId(), tuteurId))
                .collectList()
                .doOnNext(sessions -> log.info("Found {} sessions for tutor {}", sessions.size(), tuteurId))
                .flatMapMany(sessions -> Flux.fromIterable(sessions))
                .map(session -> {
                    try {
                        log.debug("Mapping session {} to DTO", session.getId());
                        return mapToResponseDTO(session);
                    } catch (Exception e) {
                        log.error("Error mapping session {} to DTO: {}", session.getId(), e.getMessage(), e);
                        throw e;
                    }
                })
                .doOnError(error -> log.error("Error in getSessionsByTutor for tutor {}: {}", tuteurId, error.getMessage(), error));
    }

    /**
     * Get detailed sessions for a tutor
     */
    public Flux<SessionDetailsDTO> getDetailedSessionsByTutor(Long tuteurId) {
        log.info("Getting detailed sessions for tutor: {}", tuteurId);
        return sessionRepository.findDetailedSessionsByTutor(tuteurId)
                .doOnNext(session -> log.debug("Found detailed session for tutor {}", tuteurId))
                .doOnComplete(() -> log.info("Completed fetching detailed sessions for tutor: {}", tuteurId))
                .doOnError(error -> log.error("Error fetching detailed sessions for tutor {}: {}", tuteurId, error.getMessage(), error))
                .map(this::mapToDetailsDTO);
    }

    /**
     * Get sessions for a student
     */
    public Flux<SessionResponseDTO> getSessionsByStudent(Long etudiantId) {
        log.info("Getting sessions for student: {}", etudiantId);
        return sessionRepository.findByEtudiantId(etudiantId)
                .doOnNext(session -> log.debug("Found session: {} for student {}", session.getId(), etudiantId))
                .collectList()
                .doOnNext(sessions -> log.info("Found {} sessions for student {}", sessions.size(), etudiantId))
                .flatMapMany(sessions -> Flux.fromIterable(sessions))
                .map(session -> {
                    try {
                        log.debug("Mapping session {} to DTO", session.getId());
                        return mapToResponseDTO(session);
                    } catch (Exception e) {
                        log.error("Error mapping session {} to DTO: {}", session.getId(), e.getMessage(), e);
                        throw e;
                    }
                })
                .doOnError(error -> log.error("Error in getSessionsByStudent for student {}: {}", etudiantId, error.getMessage(), error));
    }

    /**
     * Get detailed sessions for a student
     */
    public Flux<SessionDetailsDTO> getDetailedSessionsByStudent(Long etudiantId) {
        log.info("Getting detailed sessions for student: {}", etudiantId);
        return sessionRepository.findDetailedSessionsByStudent(etudiantId)
                .map(this::mapToDetailsDTO);
    }

    /**
     * Get sessions by status
     */
    public Flux<SessionDetailsDTO> getSessionsByStatus(SessionStatus statut) {
        return sessionRepository.findDetailedSessionsByStatus(statut)
                .map(this::mapToDetailsDTO);
    }

    /**
     * Get sessions by date range
     */
    public Flux<SessionDetailsDTO> getSessionsByDateRange(LocalDateTime dateDebut, LocalDateTime dateFin, SessionStatus statut) {
        return sessionRepository.findDetailedSessionsByDateRange(dateDebut, dateFin, statut)
                .map(this::mapToDetailsDTO);
    }

    /**
     * Get sessions by subject
     */
    public Flux<SessionDetailsDTO> getSessionsBySubject(Long matiereId, SessionStatus statut) {
        return sessionRepository.findDetailedSessionsBySubject(matiereId, statut)
                .map(this::mapToDetailsDTO);
    }

    /**
     * Get upcoming sessions
     */
    public Flux<SessionDetailsDTO> getUpcomingSessions(Long tuteurId, Long etudiantId) {
        return sessionRepository.findUpcomingDetailedSessions(LocalDateTime.now(), tuteurId, etudiantId)
                .map(this::mapToDetailsDTO);
    }

    /**
     * Get today's sessions
     */
    public Flux<SessionDetailsDTO> getTodaysSessions(Long tuteurId, Long etudiantId) {
        return sessionRepository.findTodaysDetailedSessions(tuteurId, etudiantId)
                .map(this::mapToDetailsDTO);
    }

    /**
     * Search sessions
     */
    public Flux<SessionDetailsDTO> searchSessions(String searchTerm, SessionStatus statut) {
        return sessionRepository.searchDetailedSessions(searchTerm, statut)
                .map(this::mapToDetailsDTO);
    }

    /**
     * Get sessions with advanced filters
     */
    public Flux<SessionDetailsDTO> getSessionsWithFilters(
            Long tuteurId, Long etudiantId, Long matiereId, SessionStatus statut, SessionType typeSession,
            LocalDateTime dateDebut, LocalDateTime dateFin, Double prixMin, Double prixMax) {
        return sessionRepository.findDetailedSessionsWithFilters(
                tuteurId, etudiantId, matiereId, statut, typeSession, dateDebut, dateFin, prixMin, prixMax)
                .map(this::mapToDetailsDTO);
    }

    // ===============================================
    // STATISTICS AND ANALYTICS
    // ===============================================

    /**
     * Count completed sessions by tutor
     */
    public Mono<Long> countCompletedSessionsByTutor(Long tuteurId) {
        return sessionRepository.countCompletedSessionsByTuteur(tuteurId);
    }

    /**
     * Count completed sessions by student
     */
    public Mono<Long> countCompletedSessionsByStudent(Long etudiantId) {
        return sessionRepository.countCompletedSessionsByEtudiant(etudiantId);
    }

    /**
     * Get total earnings by tutor
     */
    public Mono<Double> getTotalEarningsByTutor(Long tuteurId) {
        return sessionRepository.getTotalEarningsByTuteur(tuteurId);
    }

    /**
     * Get expired confirmed sessions (for cleanup)
     */
    public Flux<SessionResponseDTO> getExpiredConfirmedSessions() {
        return sessionRepository.findExpiredConfirmedSessions(LocalDateTime.now())
                .map(this::mapToResponseDTO);
    }

    // ===============================================
    // MAPPING METHODS
    // ===============================================

    private SessionResponseDTO mapToResponseDTO(Session session) {
        SessionResponseDTO dto = new SessionResponseDTO();
        dto.setId(session.getId());
        dto.setTuteurId(session.getTuteurId());
        dto.setEtudiantId(session.getEtudiantId());
        dto.setMatiereId(session.getMatiereId());
        dto.setDemandeSessionId(session.getDemandeSessionId());
        dto.setDateHeure(session.getDateHeure());
        dto.setDuree(session.getDuree());
        dto.setStatut(session.getStatut());
        dto.setPrix(session.getPrix());
        dto.setTypeSession(session.getTypeSession());
        dto.setLienVisio(session.getLienVisio());
        dto.setNotes(session.getNotes());
        dto.setSalle(session.getSalle());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setUpdatedAt(session.getUpdatedAt());

        // Calculate additional fields
        dto.setDateFin(session.getDateFin());
        dto.setCanBeModified(canBeModified(session));
        dto.setCanBeCancelled(canBeCancelled(session));
        dto.setCanBeCompleted(canBeCompleted(session));
        dto.setUpcoming(isUpcoming(session));
        dto.setToday(isToday(session));
        dto.setMinutesUntilStart(calculateMinutesUntilStart(session));
        dto.setStatusDescription(getStatusDescription(session.getStatut()));

        return dto;
    }

    private SessionDetailsDTO mapToDetailsDTO(SessionDetailsResult result) {
        SessionDetailsDTO dto = new SessionDetailsDTO();
        
        // Session details
        dto.setId(result.getId());
        dto.setDateHeure(result.getDateHeure());
        dto.setDuree(result.getDuree());
        dto.setStatut(result.getStatut());
        dto.setPrix(result.getPrix());
        dto.setTypeSession(result.getTypeSession());
        dto.setLienVisio(result.getLienVisio());
        dto.setNotes(result.getNotes());
        dto.setSalle(result.getSalle());
        dto.setCreatedAt(result.getCreatedAt());
        dto.setUpdatedAt(result.getUpdatedAt());

        // Student details
        dto.setEtudiantId(result.getEtudiantId());
        dto.setEtudiantNom(result.getEtudiantNom());
        dto.setEtudiantPrenom(result.getEtudiantPrenom());
        dto.setEtudiantEmail(result.getEtudiantEmail());
        dto.setEtudiantTelephone(result.getEtudiantTelephone());
        dto.setEtudiantStatus(result.getEtudiantStatus());

        // Tutor details
        dto.setTuteurId(result.getTuteurId());
        dto.setTuteurNom(result.getTuteurNom());
        dto.setTuteurPrenom(result.getTuteurPrenom());
        dto.setTuteurEmail(result.getTuteurEmail());
        dto.setTuteurTelephone(result.getTuteurTelephone());
        dto.setTuteurStatus(result.getTuteurStatus());
        dto.setTuteurSpecialite(result.getTuteurSpecialite());
        dto.setTuteurTarifHoraire(result.getTuteurTarifHoraire());
        dto.setTuteurVerified(result.getTuteurVerified());

        // Subject details
        dto.setMatiereId(result.getMatiereId());
        dto.setMatiereNom(result.getMatiereNom());
        dto.setMatiereDescription(result.getMatiereDescription());

        // Related request details
        dto.setDemandeSessionId(result.getDemandeSessionId());
        dto.setDemandeStatut(result.getDemandeStatut());
        dto.setDemandeCreatedAt(result.getDemandeCreatedAt());
        dto.setDemandeMessage(result.getDemandeMessage());

        // Calculate additional fields
        LocalDateTime now = LocalDateTime.now();
        dto.setDateFin(result.getDateHeure().plusMinutes(result.getDuree()));
        dto.setCanBeModified(canBeModifiedByStatus(result.getStatut(), result.getDateHeure()));
        dto.setCanBeCancelled(canBeCancelledByStatus(result.getStatut()));
        dto.setCanBeCompleted(result.getStatut() == SessionStatus.EN_COURS);
        dto.setUpcoming(result.getDateHeure().isAfter(now));
        dto.setToday(result.getDateHeure().toLocalDate().equals(now.toLocalDate()));
        dto.setInProgress(result.getStatut() == SessionStatus.EN_COURS);
        dto.setMinutesUntilStart(Duration.between(now, result.getDateHeure()).toMinutes());
        dto.setMinutesRemaining(result.getStatut() == SessionStatus.EN_COURS ? 
            Duration.between(now, result.getDateHeure().plusMinutes(result.getDuree())).toMinutes() : 0);
        dto.setStatusDescription(getStatusDescription(result.getStatut()));

        return dto;
    }

    // ===============================================
    // UTILITY METHODS
    // ===============================================

    private boolean canBeModified(Session session) {
        return canBeModifiedByStatus(session.getStatut(), session.getDateHeure());
    }

    private boolean canBeModifiedByStatus(SessionStatus statut, LocalDateTime dateHeure) {
        // Cannot modify completed or cancelled sessions
        if (statut == SessionStatus.TERMINEE || statut == SessionStatus.ANNULEE) {
            return false;
        }
        // Cannot modify sessions that are currently in progress
        if (statut == SessionStatus.EN_COURS) {
            return false;
        }
        // Can modify future sessions (no time restriction)
        // This allows tutors/students to update sessions even close to start time
        return dateHeure.isAfter(LocalDateTime.now());
    }

    private boolean canBeCancelled(Session session) {
        return canBeCancelledByStatus(session.getStatut());
    }

    private boolean canBeCancelledByStatus(SessionStatus statut) {
        return statut != SessionStatus.TERMINEE && statut != SessionStatus.ANNULEE;
    }

    private boolean canBeCompleted(Session session) {
        return session.getStatut() == SessionStatus.EN_COURS;
    }

    private boolean canBeRescheduled(Session session) {
        return session.getStatut() == SessionStatus.DEMANDEE || 
               (session.getStatut() == SessionStatus.CONFIRMEE && 
                session.getDateHeure().isAfter(LocalDateTime.now().plusHours(2)));
    }

    private boolean isUpcoming(Session session) {
        return session.getDateHeure().isAfter(LocalDateTime.now());
    }

    private boolean isToday(Session session) {
        return session.getDateHeure().toLocalDate().equals(LocalDateTime.now().toLocalDate());
    }

    private long calculateMinutesUntilStart(Session session) {
        LocalDateTime now = LocalDateTime.now();
        return session.getDateHeure().isAfter(now) ? 
            Duration.between(now, session.getDateHeure()).toMinutes() : 0;
    }

    private String getStatusDescription(SessionStatus statut) {
        return switch (statut) {
            case DEMANDEE -> "Session demandée, en attente de confirmation";
            case CONFIRMEE -> "Session confirmée";
            case EN_COURS -> "Session en cours";
            case TERMINEE -> "Session terminée";
            case ANNULEE -> "Session annulée";
        };
    }
}