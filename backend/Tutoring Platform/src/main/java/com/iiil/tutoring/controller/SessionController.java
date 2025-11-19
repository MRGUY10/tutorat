package com.iiil.tutoring.controller;

import com.iiil.tutoring.dto.session.*;
import com.iiil.tutoring.service.session.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/sessions")
@Tag(name = "Sessions", description = "API for managing tutoring sessions")
public class SessionController {

    private static final Logger log = LoggerFactory.getLogger(SessionController.class);

    private final SessionService sessionService;

    @Autowired
    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    @Operation(summary = "Create a new session")
    public Mono<ResponseEntity<SessionResponseDTO>> createSession(
            @Valid @RequestBody CreateSessionDTO createDTO) {
        return sessionService.createSession(createDTO)
                .map(session -> ResponseEntity.status(HttpStatus.CREATED).body(session))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get session by ID")
    public Mono<ResponseEntity<SessionResponseDTO>> getSessionById(
            @PathVariable Long sessionId) {
        return sessionService.getSessionById(sessionId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{sessionId}")
    @Operation(summary = "Update a session")
    public Mono<ResponseEntity<SessionResponseDTO>> updateSession(
            @PathVariable Long sessionId,
            @Valid @RequestBody UpdateSessionDTO updateDTO) {
        return sessionService.updateSession(sessionId, updateDTO)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Error updating session {}: {}", sessionId, e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    @PostMapping("/{sessionId}/start")
    @Operation(summary = "Start a session")
    public Mono<ResponseEntity<SessionResponseDTO>> startSession(
            @PathVariable Long sessionId) {
        return sessionService.startSession(sessionId)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{sessionId}/complete")
    @Operation(summary = "Complete a session")
    public Mono<ResponseEntity<SessionResponseDTO>> completeSession(
            @PathVariable Long sessionId) {
        return sessionService.completeSession(sessionId, null)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{sessionId}/cancel")
    @Operation(summary = "Cancel a session")
    public Mono<ResponseEntity<SessionResponseDTO>> cancelSession(
            @PathVariable Long sessionId,
            @RequestParam(required = false) String reason) {
        return sessionService.cancelSession(sessionId, reason)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{sessionId}/confirm")
    @Operation(summary = "Confirm a session")
    public Mono<ResponseEntity<SessionResponseDTO>> confirmSession(
            @PathVariable Long sessionId) {
        return sessionService.confirmSession(sessionId)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @GetMapping("/tutor/{tutorId}")
    @Operation(summary = "Get sessions by tutor")
    public Flux<SessionResponseDTO> getSessionsByTutor(
            @PathVariable Long tutorId) {
        log.info("GET /api/sessions/tutor/{} - Getting sessions for tutor (Note: tutorId should be from users table, not tutors table)", tutorId);
        return sessionService.getSessionsByTutor(tutorId)
                .doOnComplete(() -> log.info("Completed fetching sessions for tutor: {}", tutorId))
                .doOnError(error -> log.error("Error in controller for tutor {}: {}", tutorId, error.getMessage(), error));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get sessions by student")
    public Flux<SessionResponseDTO> getSessionsByStudent(
            @PathVariable Long studentId) {
        log.info("GET /api/sessions/student/{} - Getting sessions for student (Note: studentId should be from users table)", studentId);
        return sessionService.getSessionsByStudent(studentId)
                .doOnComplete(() -> log.info("Completed fetching sessions for student: {}", studentId))
                .doOnError(error -> log.error("Error in controller for student {}: {}", studentId, error.getMessage(), error));
    }
}