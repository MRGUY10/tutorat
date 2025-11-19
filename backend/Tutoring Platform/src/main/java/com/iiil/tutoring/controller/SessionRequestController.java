package com.iiil.tutoring.controller;

import com.iiil.tutoring.dto.session.*;
import com.iiil.tutoring.enums.RequestStatus;
import com.iiil.tutoring.enums.Urgence;
import com.iiil.tutoring.service.session.SessionRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * REST Controller for session request operations
 */
@RestController
@RequestMapping("/api/session-requests")
@Tag(name = "Session Requests", description = "API for managing session requests")
public class SessionRequestController {

    private final SessionRequestService sessionRequestService;

    @Autowired
    public SessionRequestController(SessionRequestService sessionRequestService) {
        this.sessionRequestService = sessionRequestService;
    }

    // ===============================================
    // CRUD OPERATIONS
    // ===============================================

    @PostMapping
    @Operation(summary = "Create a new session request", description = "Creates a new session request with specified details")
    public Mono<ResponseEntity<SessionRequestResponseDTO>> createSessionRequest(
            @Parameter(description = "ID of the student making the request") @RequestParam Long etudiantId,
            @Valid @RequestBody CreateSessionRequestDTO createDTO) {
        return sessionRequestService.createSessionRequest(etudiantId, createDTO)
                .map(sessionRequest -> ResponseEntity.status(HttpStatus.CREATED).body(sessionRequest))
                .onErrorResume(error -> {
                    // Log the error for debugging
                    System.err.println("Error creating session request: " + error.getMessage());
                    error.printStackTrace();
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    @PutMapping("/{requestId}")
    @Operation(summary = "Update a session request", description = "Updates an existing session request details")
    public Mono<ResponseEntity<SessionRequestResponseDTO>> updateSessionRequest(
            @Parameter(description = "ID of the session request to update") @PathVariable Long requestId,
            @Valid @RequestBody UpdateSessionRequestDTO updateDTO) {
        return sessionRequestService.updateSessionRequest(requestId, updateDTO)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{requestId}/respond")
    @Operation(summary = "Respond to a session request", description = "Tutor responds to a session request with acceptance or rejection")
    public Mono<ResponseEntity<SessionRequestResponseDTO>> respondToRequest(
            @Parameter(description = "ID of the session request") @PathVariable Long requestId,
            @Parameter(description = "Response status (ACCEPTEE or REFUSEE)") @RequestParam RequestStatus statut,
            @Parameter(description = "Optional response message") @RequestParam(required = false) String message) {
        
        UpdateSessionRequestDTO responseDTO = new UpdateSessionRequestDTO();
        responseDTO.setStatut(statut);
        responseDTO.setReponseTuteur(message);
        
        return sessionRequestService.respondToRequest(requestId, responseDTO)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{requestId}/accept")
    @Operation(summary = "Accept a session request", description = "Tutor accepts a session request")
    public Mono<ResponseEntity<SessionRequestResponseDTO>> acceptRequest(
            @Parameter(description = "ID of the session request") @PathVariable Long requestId,
            @Parameter(description = "Optional acceptance message") @RequestParam(required = false) String message) {
        return sessionRequestService.acceptRequest(requestId, message)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    @PostMapping("/{requestId}/reject")
    @Operation(summary = "Reject a session request", description = "Tutor rejects a session request")
    public Mono<ResponseEntity<SessionRequestResponseDTO>> rejectRequest(
            @Parameter(description = "ID of the session request") @PathVariable Long requestId,
            @Parameter(description = "Optional rejection reason") @RequestParam(required = false) String reason) {
        return sessionRequestService.rejectRequest(requestId, reason)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{requestId}")
    @Operation(summary = "Delete a session request", description = "Deletes a session request (only allowed in certain states)")
    public Mono<ResponseEntity<Void>> deleteSessionRequest(
            @Parameter(description = "ID of the session request") @PathVariable Long requestId) {
        return sessionRequestService.deleteSessionRequest(requestId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    // ===============================================
    // QUERY OPERATIONS
    // ===============================================

    @GetMapping("/{requestId}")
    @Operation(summary = "Get session request by ID", description = "Retrieves a specific session request by its ID")
    public Mono<ResponseEntity<SessionRequestResponseDTO>> getSessionRequestById(
            @Parameter(description = "ID of the session request") @PathVariable Long requestId) {
        return sessionRequestService.getSessionRequestById(requestId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{requestId}/details")
    @Operation(summary = "Get detailed session request by ID", description = "Retrieves detailed information about a session request including user and subject details")
    public Mono<ResponseEntity<SessionRequestDetailsDTO>> getDetailedSessionRequestById(
            @Parameter(description = "ID of the session request") @PathVariable Long requestId) {
        return sessionRequestService.getDetailedSessionRequestById(requestId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get session requests by student", description = "Retrieves all session requests for a specific student")
    public Flux<SessionRequestResponseDTO> getSessionRequestsByStudent(
            @Parameter(description = "ID of the student") @PathVariable Long studentId) {
        return sessionRequestService.getSessionRequestsByStudent(studentId);
    }

    @GetMapping("/student/{studentId}/details")
    @Operation(summary = "Get detailed session requests by student", description = "Retrieves detailed session requests for a specific student")
    public Flux<SessionRequestDetailsDTO> getDetailedSessionRequestsByStudent(
            @Parameter(description = "ID of the student") @PathVariable Long studentId) {
        return sessionRequestService.getDetailedSessionRequestsByStudent(studentId);
    }

    @GetMapping("/tutor/{tutorId}")
    @Operation(summary = "Get session requests by tutor", description = "Retrieves all session requests for a specific tutor")
    public Flux<SessionRequestResponseDTO> getSessionRequestsByTutor(
            @Parameter(description = "ID of the tutor") @PathVariable Long tutorId) {
        return sessionRequestService.getSessionRequestsByTutor(tutorId);
    }

    @GetMapping("/tutor/{tutorId}/details")
    @Operation(summary = "Get detailed session requests by tutor", description = "Retrieves detailed session requests for a specific tutor")
    public Flux<SessionRequestDetailsDTO> getDetailedSessionRequestsByTutor(
            @Parameter(description = "ID of the tutor") @PathVariable Long tutorId) {
        return sessionRequestService.getDetailedSessionRequestsByTutor(tutorId);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get session requests by status", description = "Retrieves session requests filtered by status")
    public Flux<SessionRequestDetailsDTO> getSessionRequestsByStatus(
            @Parameter(description = "Request status to filter by") @PathVariable RequestStatus status) {
        return sessionRequestService.getSessionRequestsByStatus(status);
    }

    @GetMapping("/subject/{subjectId}")
    @Operation(summary = "Get session requests by subject", description = "Retrieves session requests for a specific subject")
    public Flux<SessionRequestDetailsDTO> getSessionRequestsBySubject(
            @Parameter(description = "ID of the subject") @PathVariable Long subjectId,
            @Parameter(description = "Optional status filter") @RequestParam(required = false) RequestStatus statut) {
        return sessionRequestService.getSessionRequestsBySubject(subjectId, statut);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending session requests", description = "Retrieves all session requests awaiting tutor response")
    public Flux<SessionRequestDetailsDTO> getPendingSessionRequests(
            @Parameter(description = "Optional tutor ID to filter by") @RequestParam(required = false) Long tuteurId,
            @Parameter(description = "Optional student ID to filter by") @RequestParam(required = false) Long etudiantId) {
        return sessionRequestService.getSessionRequestsWithFilters(
                tuteurId, etudiantId, null, RequestStatus.EN_ATTENTE, null, null, null, null, null);
    }

    @GetMapping("/accepted")
    @Operation(summary = "Get accepted session requests", description = "Retrieves accepted session requests ready for session creation")
    public Flux<SessionRequestDetailsDTO> getAcceptedSessionRequests(
            @Parameter(description = "Optional tutor ID to filter by") @RequestParam(required = false) Long tuteurId,
            @Parameter(description = "Optional student ID to filter by") @RequestParam(required = false) Long etudiantId) {
        return sessionRequestService.getSessionRequestsWithFilters(
                tuteurId, etudiantId, null, RequestStatus.ACCEPTEE, null, null, null, null, null);
    }

    @GetMapping("/urgent")
    @Operation(summary = "Get urgent session requests", description = "Retrieves session requests marked as urgent")
    public Flux<SessionRequestDetailsDTO> getUrgentSessionRequests() {
        return sessionRequestService.getSessionRequestsWithFilters(
                null, null, null, RequestStatus.EN_ATTENTE, Urgence.HAUTE, null, null, null, null);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get session requests by date range", description = "Retrieves session requests within a specific date range")
    public Flux<SessionRequestDetailsDTO> getSessionRequestsByDateRange(
            @Parameter(description = "Start date (ISO format)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @Parameter(description = "End date (ISO format)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin,
            @Parameter(description = "Optional status filter") @RequestParam(required = false) RequestStatus statut) {
        return sessionRequestService.getSessionRequestsByDateRange(dateDebut, dateFin, statut);
    }

    @GetMapping("/search")
    @Operation(summary = "Search session requests", description = "Searches session requests by keywords in message or notes")
    public Flux<SessionRequestDetailsDTO> searchSessionRequests(
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            @Parameter(description = "Optional status filter") @RequestParam(required = false) RequestStatus statut) {
        return sessionRequestService.searchSessionRequests(searchTerm, statut);
    }

    @GetMapping("/filter")
    @Operation(summary = "Get session requests with advanced filters", description = "Retrieves session requests with multiple filter criteria")
    public Flux<SessionRequestDetailsDTO> getSessionRequestsWithFilters(
            @Parameter(description = "Optional tutor ID") @RequestParam(required = false) Long tuteurId,
            @Parameter(description = "Optional student ID") @RequestParam(required = false) Long etudiantId,
            @Parameter(description = "Optional subject ID") @RequestParam(required = false) Long matiereId,
            @Parameter(description = "Optional status") @RequestParam(required = false) RequestStatus statut,
            @Parameter(description = "Optional urgency level") @RequestParam(required = false) Urgence urgence,
            @Parameter(description = "Optional start date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @Parameter(description = "Optional end date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin,
            @Parameter(description = "Optional minimum price") @RequestParam(required = false) Double prixMin,
            @Parameter(description = "Optional maximum price") @RequestParam(required = false) Double prixMax) {
        return sessionRequestService.getSessionRequestsWithFilters(
                tuteurId, etudiantId, matiereId, statut, urgence, dateDebut, dateFin, prixMin, prixMax);
    }

    // ===============================================
    // STATISTICS AND ANALYTICS
    // ===============================================

    @GetMapping("/stats/count-by-student/{studentId}")
    @Operation(summary = "Count session requests by student", description = "Gets the total number of session requests for a student")
    public Mono<ResponseEntity<Long>> countSessionRequestsByStudent(
            @Parameter(description = "ID of the student") @PathVariable Long studentId) {
        return sessionRequestService.countRequestsByStudentAndStatus(studentId, null)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/stats/count-by-tutor/{tutorId}")
    @Operation(summary = "Count session requests by tutor", description = "Gets the total number of session requests for a tutor")
    public Mono<ResponseEntity<Long>> countSessionRequestsByTutor(
            @Parameter(description = "ID of the tutor") @PathVariable Long tutorId) {
        return sessionRequestService.countRequestsByTutorAndStatus(tutorId, null)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/stats/count-pending/{tutorId}")
    @Operation(summary = "Count pending requests by tutor", description = "Gets the number of pending requests for a tutor")
    public Mono<ResponseEntity<Long>> countPendingRequestsByTutor(
            @Parameter(description = "ID of the tutor") @PathVariable Long tutorId) {
        return sessionRequestService.countRequestsByTutorAndStatus(tutorId, RequestStatus.EN_ATTENTE)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/stats/expired")
    @Operation(summary = "Get expired session requests", description = "Retrieves session requests that have expired without response")
    public Flux<SessionRequestResponseDTO> getExpiredSessionRequests() {
        return sessionRequestService.getOldPendingRequests(7); // 7 days old
    }
}