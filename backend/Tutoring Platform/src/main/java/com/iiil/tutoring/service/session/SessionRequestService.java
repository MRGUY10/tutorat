package com.iiil.tutoring.service.session;

import com.iiil.tutoring.dto.session.*;
import com.iiil.tutoring.entity.DemandeSession;
import com.iiil.tutoring.enums.RequestStatus;
import com.iiil.tutoring.enums.SessionType;
import com.iiil.tutoring.enums.Urgence;
import com.iiil.tutoring.repository.DemandeSessionRepository;
import com.iiil.tutoring.repository.result.SessionRequestDetailsResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Service for managing session requests
 */
@Service
public class SessionRequestService {

    private final DemandeSessionRepository demandeSessionRepository;
    private final SessionService sessionService;

    @Autowired
    public SessionRequestService(DemandeSessionRepository demandeSessionRepository, 
                                 @Lazy SessionService sessionService) {
        this.demandeSessionRepository = demandeSessionRepository;
        this.sessionService = sessionService;
    }

    // ===============================================
    // CRUD OPERATIONS
    // ===============================================

    /**
     * Create a new session request
     */
    public Mono<SessionRequestResponseDTO> createSessionRequest(Long etudiantId, CreateSessionRequestDTO createDTO) {
        DemandeSession demandeSession = new DemandeSession();
        demandeSession.setEtudiantId(etudiantId);
        demandeSession.setTuteurId(createDTO.getTuteurId());
        demandeSession.setMatiereId(createDTO.getMatiereId());
        demandeSession.setDateVoulue(createDTO.getDateVoulue());
        demandeSession.setMessage(createDTO.getMessage());
        demandeSession.setUrgence(createDTO.getUrgence());
        demandeSession.setDureeSouhaitee(createDTO.getDureeSouhaitee());
        demandeSession.setBudgetMax(createDTO.getBudgetMax());
        demandeSession.setStatut(RequestStatus.EN_ATTENTE);
        demandeSession.setDateCreation(LocalDateTime.now());

        return demandeSessionRepository.save(demandeSession)
                .map(this::mapToResponseDTO);
    }

    /**
     * Update an existing session request
     */
    public Mono<SessionRequestResponseDTO> updateSessionRequest(Long requestId, UpdateSessionRequestDTO updateDTO) {
        return demandeSessionRepository.findById(requestId)
                .switchIfEmpty(Mono.error(new RuntimeException("Session request not found with id: " + requestId)))
                .flatMap(existingRequest -> {
                    // Only allow updates if request is still pending
                    if (existingRequest.getStatut() != RequestStatus.EN_ATTENTE) {
                        return Mono.error(new RuntimeException("Cannot update request that is no longer pending"));
                    }

                    // Update fields if provided
                    if (updateDTO.getDateVoulue() != null) {
                        existingRequest.setDateVoulue(updateDTO.getDateVoulue());
                    }
                    if (updateDTO.getMessage() != null) {
                        existingRequest.setMessage(updateDTO.getMessage());
                    }
                    if (updateDTO.getUrgence() != null) {
                        existingRequest.setUrgence(updateDTO.getUrgence());
                    }
                    if (updateDTO.getDureeSouhaitee() != null) {
                        existingRequest.setDureeSouhaitee(updateDTO.getDureeSouhaitee());
                    }
                    if (updateDTO.getBudgetMax() != null) {
                        existingRequest.setBudgetMax(updateDTO.getBudgetMax());
                    }

                    return demandeSessionRepository.save(existingRequest);
                })
                .map(this::mapToResponseDTO);
    }

    /**
     * Respond to a session request (tutor action)
     */
    public Mono<SessionRequestResponseDTO> respondToRequest(Long requestId, UpdateSessionRequestDTO responseDTO) {
        return demandeSessionRepository.findById(requestId)
                .switchIfEmpty(Mono.error(new RuntimeException("Session request not found with id: " + requestId)))
                .flatMap(request -> {
                    if (request.getStatut() != RequestStatus.EN_ATTENTE) {
                        return Mono.error(new RuntimeException("Request has already been responded to"));
                    }

                    // Update response fields
                    if (responseDTO.getReponseTuteur() != null) {
                        request.setReponseTuteur(responseDTO.getReponseTuteur());
                    }
                    if (responseDTO.getStatut() != null) {
                        request.setStatut(responseDTO.getStatut());
                        request.setDateReponse(LocalDateTime.now());
                    }

                    // Save the request
                    return demandeSessionRepository.save(request)
                            .flatMap(savedRequest -> {
                                // If request was accepted, create a session
                                if (savedRequest.getStatut() == RequestStatus.ACCEPTEE) {
                                    CreateSessionDTO createSessionDTO = new CreateSessionDTO();
                                    createSessionDTO.setTuteurId(savedRequest.getTuteurId());
                                    createSessionDTO.setEtudiantId(savedRequest.getEtudiantId());
                                    createSessionDTO.setMatiereId(savedRequest.getMatiereId());
                                    createSessionDTO.setDemandeSessionId(savedRequest.getId());
                                    createSessionDTO.setDateHeure(savedRequest.getDateVoulue());
                                    createSessionDTO.setDuree(savedRequest.getDureeSouhaitee() > 0 ? 
                                            savedRequest.getDureeSouhaitee() : 60);
                                    createSessionDTO.setTypeSession(SessionType.EN_LIGNE);
                                    createSessionDTO.setPrix(savedRequest.getBudgetMax() > 0 ? 
                                            savedRequest.getBudgetMax() : 0.0);
                                    createSessionDTO.setRequiresConfirmation(false);
                                    
                                    return sessionService.createSession(createSessionDTO)
                                            .thenReturn(savedRequest);
                                }
                                // If not accepted, just return the saved request
                                return Mono.just(savedRequest);
                            });
                })
                .map(this::mapToResponseDTO);
    }

    /**
     * Accept a session request and create corresponding session
     */
    public Mono<SessionRequestResponseDTO> acceptRequest(Long requestId, String tutorResponse) {
        return demandeSessionRepository.findById(requestId)
                .switchIfEmpty(Mono.error(new RuntimeException("Session request not found with id: " + requestId)))
                .flatMap(request -> {
                    // Mark the request as accepted
                    request.accepter(tutorResponse);
                    
                    // Save the updated request first
                    return demandeSessionRepository.save(request)
                            .flatMap(savedRequest -> {
                                // Create a session from the accepted request
                                CreateSessionDTO createSessionDTO = new CreateSessionDTO();
                                createSessionDTO.setTuteurId(savedRequest.getTuteurId());
                                createSessionDTO.setEtudiantId(savedRequest.getEtudiantId());
                                createSessionDTO.setMatiereId(savedRequest.getMatiereId());
                                createSessionDTO.setDemandeSessionId(savedRequest.getId());
                                createSessionDTO.setDateHeure(savedRequest.getDateVoulue());
                                createSessionDTO.setDuree(savedRequest.getDureeSouhaitee() > 0 ? 
                                        savedRequest.getDureeSouhaitee() : 60); // Default to 60 minutes if not specified
                                createSessionDTO.setTypeSession(SessionType.EN_LIGNE); // Default type, can be updated later
                                createSessionDTO.setPrix(savedRequest.getBudgetMax() > 0 ? 
                                        savedRequest.getBudgetMax() : 0.0);
                                createSessionDTO.setRequiresConfirmation(false); // Automatically confirmed since tutor accepted
                                
                                // Create the session and return the request DTO
                                return sessionService.createSession(createSessionDTO)
                                        .thenReturn(savedRequest);
                            });
                })
                .map(this::mapToResponseDTO);
    }

    /**
     * Reject a session request
     */
    public Mono<SessionRequestResponseDTO> rejectRequest(Long requestId, String tutorResponse) {
        return demandeSessionRepository.findById(requestId)
                .switchIfEmpty(Mono.error(new RuntimeException("Session request not found with id: " + requestId)))
                .flatMap(request -> {
                    request.refuser(tutorResponse);
                    return demandeSessionRepository.save(request);
                })
                .map(this::mapToResponseDTO);
    }

    /**
     * Delete a session request
     */
    public Mono<Void> deleteSessionRequest(Long requestId) {
        return demandeSessionRepository.findById(requestId)
                .switchIfEmpty(Mono.error(new RuntimeException("Session request not found with id: " + requestId)))
                .flatMap(request -> {
                    // Only allow deletion if request is pending
                    if (request.getStatut() != RequestStatus.EN_ATTENTE) {
                        return Mono.error(new RuntimeException("Cannot delete request that is no longer pending"));
                    }
                    return demandeSessionRepository.delete(request);
                });
    }

    // ===============================================
    // QUERY OPERATIONS
    // ===============================================

    /**
     * Get session request by ID
     */
    public Mono<SessionRequestResponseDTO> getSessionRequestById(Long requestId) {
        return demandeSessionRepository.findById(requestId)
                .switchIfEmpty(Mono.error(new RuntimeException("Session request not found with id: " + requestId)))
                .map(this::mapToResponseDTO);
    }

    /**
     * Get detailed session request by ID
     */
    public Mono<SessionRequestDetailsDTO> getDetailedSessionRequestById(Long requestId) {
        return demandeSessionRepository.findDetailedRequestById(requestId)
                .switchIfEmpty(Mono.error(new RuntimeException("Session request not found with id: " + requestId)))
                .map(this::mapToDetailsDTO);
    }

    /**
     * Get session requests for a student
     */
    public Flux<SessionRequestResponseDTO> getSessionRequestsByStudent(Long etudiantId) {
        return demandeSessionRepository.findByEtudiantId(etudiantId)
                .map(this::mapToResponseDTO);
    }

    /**
     * Get detailed session requests for a student
     */
    public Flux<SessionRequestDetailsDTO> getDetailedSessionRequestsByStudent(Long etudiantId) {
        return demandeSessionRepository.findDetailedRequestsByStudent(etudiantId)
                .map(this::mapToDetailsDTO);
    }

    /**
     * Get session requests for a tutor
     */
    public Flux<SessionRequestResponseDTO> getSessionRequestsByTutor(Long tuteurId) {
        return demandeSessionRepository.findByTuteurId(tuteurId)
                .map(this::mapToResponseDTO);
    }

    /**
     * Get detailed session requests for a tutor
     */
    public Flux<SessionRequestDetailsDTO> getDetailedSessionRequestsByTutor(Long tuteurId) {
        return demandeSessionRepository.findDetailedRequestsByTutor(tuteurId)
                .map(this::mapToDetailsDTO);
    }

    /**
     * Get session requests by status
     */
    public Flux<SessionRequestDetailsDTO> getSessionRequestsByStatus(RequestStatus statut) {
        return demandeSessionRepository.findDetailedRequestsByStatus(statut)
                .map(this::mapToDetailsDTO);
    }

    /**
     * Get pending session requests by urgency
     */
    public Flux<SessionRequestDetailsDTO> getPendingRequestsByUrgency(Urgence urgence) {
        return demandeSessionRepository.findDetailedRequestsByUrgency(urgence, RequestStatus.EN_ATTENTE)
                .map(this::mapToDetailsDTO);
    }

    /**
     * Get session requests by subject
     */
    public Flux<SessionRequestDetailsDTO> getSessionRequestsBySubject(Long matiereId, RequestStatus statut) {
        return demandeSessionRepository.findDetailedRequestsBySubject(matiereId, statut)
                .map(this::mapToDetailsDTO);
    }

    /**
     * Get session requests by date range
     */
    public Flux<SessionRequestDetailsDTO> getSessionRequestsByDateRange(LocalDateTime dateDebut, LocalDateTime dateFin, RequestStatus statut) {
        return demandeSessionRepository.findDetailedRequestsByDateRange(dateDebut, dateFin, statut)
                .map(this::mapToDetailsDTO);
    }

    /**
     * Search session requests
     */
    public Flux<SessionRequestDetailsDTO> searchSessionRequests(String searchTerm, RequestStatus statut) {
        return demandeSessionRepository.searchDetailedRequests(searchTerm, statut)
                .map(this::mapToDetailsDTO);
    }

    /**
     * Get session requests with advanced filters
     */
    public Flux<SessionRequestDetailsDTO> getSessionRequestsWithFilters(
            Long tuteurId, Long etudiantId, Long matiereId, RequestStatus statut, Urgence urgence,
            LocalDateTime dateDebut, LocalDateTime dateFin, Double budgetMin, Double budgetMax) {
        return demandeSessionRepository.findDetailedRequestsWithFilters(
                tuteurId, etudiantId, matiereId, statut, urgence, dateDebut, dateFin, budgetMin, budgetMax)
                .map(this::mapToDetailsDTO);
    }

    // ===============================================
    // STATISTICS AND ANALYTICS
    // ===============================================

    /**
     * Count requests by tutor and status
     */
    public Mono<Long> countRequestsByTutorAndStatus(Long tuteurId, RequestStatus statut) {
        return demandeSessionRepository.countByTuteurIdAndStatut(tuteurId, statut);
    }

    /**
     * Count requests by student and status
     */
    public Mono<Long> countRequestsByStudentAndStatus(Long etudiantId, RequestStatus statut) {
        return demandeSessionRepository.countByEtudiantIdAndStatut(etudiantId, statut);
    }

    /**
     * Get old pending requests (for cleanup)
     */
    public Flux<SessionRequestResponseDTO> getOldPendingRequests(int daysOld) {
        LocalDateTime dateLimit = LocalDateTime.now().minusDays(daysOld);
        return demandeSessionRepository.findOldPendingRequests(dateLimit)
                .map(this::mapToResponseDTO);
    }

    // ===============================================
    // MAPPING METHODS
    // ===============================================

    private SessionRequestResponseDTO mapToResponseDTO(DemandeSession demandeSession) {
        SessionRequestResponseDTO dto = new SessionRequestResponseDTO();
        dto.setId(demandeSession.getId());
        dto.setEtudiantId(demandeSession.getEtudiantId());
        dto.setTuteurId(demandeSession.getTuteurId());
        dto.setMatiereId(demandeSession.getMatiereId());
        dto.setDateCreation(demandeSession.getDateCreation());
        dto.setDateVoulue(demandeSession.getDateVoulue());
        dto.setMessage(demandeSession.getMessage());
        dto.setStatut(demandeSession.getStatut());
        dto.setUrgence(demandeSession.getUrgence());
        dto.setDureeSouhaitee(demandeSession.getDureeSouhaitee());
        dto.setBudgetMax(demandeSession.getBudgetMax());
        dto.setReponseTuteur(demandeSession.getReponseTuteur());
        dto.setDateReponse(demandeSession.getDateReponse());
        dto.setCreatedAt(demandeSession.getDateCreation());
        dto.setUpdatedAt(demandeSession.getDateCreation()); // Use dateCreation as fallback

        // Calculate additional fields
        dto.setCanBeModified(canBeModified(demandeSession));
        dto.setExpired(isExpired(demandeSession));
        dto.setHoursUntilDesiredDate(calculateHoursUntilDesiredDate(demandeSession));

        return dto;
    }

    private SessionRequestDetailsDTO mapToDetailsDTO(SessionRequestDetailsResult result) {
        SessionRequestDetailsDTO dto = new SessionRequestDetailsDTO();

        // Session request details
        dto.setId(result.getId());
        dto.setDateCreation(result.getDateCreation());
        dto.setDateVoulue(result.getDateVoulue());
        dto.setMessage(result.getMessage());
        dto.setStatut(result.getStatut());
        dto.setUrgence(result.getUrgence());
        dto.setDureeSouhaitee(result.getDureeSouhaitee());
        dto.setBudgetMax(result.getBudgetMax());
        dto.setReponseTuteur(result.getReponseTuteur());
        dto.setDateReponse(result.getDateReponse());
        dto.setCreatedAt(result.getCreatedAt());
        dto.setUpdatedAt(result.getUpdatedAt());

        // Student details
        dto.setEtudiantId(result.getEtudiantId());
        dto.setEtudiantNom(result.getEtudiantNom());
        dto.setEtudiantPrenom(result.getEtudiantPrenom());
        dto.setEtudiantEmail(result.getEtudiantEmail());
        dto.setEtudiantTelephone(result.getEtudiantTelephone());
        dto.setEtudiantStatus(result.getEtudiantStatus());

        // Tutor details (may be null if no tutor assigned yet)
        dto.setTuteurId(result.getTuteurId());
        dto.setTuteurNom(result.getTuteurNom());
        dto.setTuteurPrenom(result.getTuteurPrenom());
        dto.setTuteurEmail(result.getTuteurEmail());
        dto.setTuteurTelephone(result.getTuteurTelephone());
        dto.setTuteurStatus(result.getTuteurStatus());
        dto.setTuteurSpecialite(result.getTuteurSpecialite());
        dto.setTuteurTarifHoraire(result.getTuteurTarifHoraire() != null ? result.getTuteurTarifHoraire() : 0.0);
        dto.setTuteurVerified(result.getTuteurVerified() != null ? result.getTuteurVerified() : false);

        // Subject details
        dto.setMatiereId(result.getMatiereId());
        dto.setMatiereNom(result.getMatiereNom());
        dto.setMatiereDescription(result.getMatiereDescription());

        // Related session
        dto.setSessionId(result.getSessionId());
        dto.setSessionDateHeure(result.getSessionDateHeure());
        dto.setSessionStatut(result.getSessionStatut());

        // Calculate additional fields
        dto.setCanBeModified(result.getStatut() == RequestStatus.EN_ATTENTE);
        dto.setExpired(result.getDateVoulue() != null && result.getDateVoulue().isBefore(LocalDateTime.now()));
        dto.setUrgent(result.getUrgence() == Urgence.HAUTE);
        dto.setHoursUntilDesiredDate(result.getDateVoulue() != null ? Duration.between(LocalDateTime.now(), result.getDateVoulue()).toHours() : 0);
        dto.setEstimatedPrice(calculateEstimatedPrice(result.getDureeSouhaitee(), result.getTuteurTarifHoraire()));
        dto.setStatusDescription(getStatusDescription(result.getStatut()));

        return dto;
    }

    // ===============================================
    // UTILITY METHODS
    // ===============================================

    private boolean canBeModified(DemandeSession demandeSession) {
        return demandeSession.getStatut() == RequestStatus.EN_ATTENTE 
               && demandeSession.getDateVoulue() != null
               && demandeSession.getDateVoulue().isAfter(LocalDateTime.now().plusHours(2));
    }

    private boolean isExpired(DemandeSession demandeSession) {
        return demandeSession.getDateVoulue() != null 
               && demandeSession.getDateVoulue().isBefore(LocalDateTime.now());
    }

    private long calculateHoursUntilDesiredDate(DemandeSession demandeSession) {
        if (demandeSession.getDateVoulue() == null) {
            return 0;
        }
        return Duration.between(LocalDateTime.now(), demandeSession.getDateVoulue()).toHours();
    }

    private double calculateEstimatedPrice(int dureeSouhaitee, Double tarifHoraire) {
        if (tarifHoraire == null) return 0.0;
        return (dureeSouhaitee / 60.0) * tarifHoraire;
    }

    private String getStatusDescription(RequestStatus statut) {
        return switch (statut) {
            case EN_ATTENTE -> "En attente de réponse du tuteur";
            case ACCEPTEE -> "Demande acceptée par le tuteur";
            case REFUSEE -> "Demande refusée par le tuteur";
        };
    }
}