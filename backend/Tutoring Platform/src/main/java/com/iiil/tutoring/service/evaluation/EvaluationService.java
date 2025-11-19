package com.iiil.tutoring.service.evaluation;

import com.iiil.tutoring.dto.evaluation.CreateEvaluationDTO;
import com.iiil.tutoring.dto.evaluation.EvaluationResponseDTO;
import com.iiil.tutoring.dto.evaluation.EvaluationSummaryDTO;
import com.iiil.tutoring.entity.Evaluation;
import com.iiil.tutoring.entity.Session;
import com.iiil.tutoring.entity.User;
import com.iiil.tutoring.enums.EvaluationType;
import com.iiil.tutoring.enums.SessionStatus;
import com.iiil.tutoring.repository.EvaluationRepository;
import com.iiil.tutoring.repository.SessionRepository;
import com.iiil.tutoring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for comprehensive evaluation management
 */
@Service
public class EvaluationService {

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new evaluation
     */
    public Mono<EvaluationResponseDTO> createEvaluation(CreateEvaluationDTO createDto) {
        return validateEvaluationCreation(createDto)
                .flatMap(validation -> {
                    if (!validation) {
                        return Mono.error(new IllegalArgumentException("Validation échouée pour la création de l'évaluation"));
                    }
                    
                    Evaluation evaluation = new Evaluation();
                    evaluation.setSessionId(createDto.getSessionId());
                    evaluation.setEvaluateurId(createDto.getEvaluateurId());
                    evaluation.setEvalueId(createDto.getEvalueId());
                    evaluation.setNote(createDto.getNote());
                    evaluation.setCommentaire(createDto.getCommentaire());
                    evaluation.setTypeEvaluation(createDto.getTypeEvaluation());
                    evaluation.setDate(LocalDateTime.now());
                    
                    // Set detailed criteria if provided
                    evaluation.setQualiteEnseignement(createDto.getQualiteEnseignement());
                    evaluation.setCommunication(createDto.getCommunication());
                    evaluation.setPonctualite(createDto.getPonctualite());
                    evaluation.setPreparation(createDto.getPreparation());
                    evaluation.setPatience(createDto.getPatience());
                    evaluation.setRecommanderais(createDto.getRecommanderais());

                    return evaluationRepository.save(evaluation);
                })
                .flatMap(this::convertToResponseDTO);
    }

    /**
     * Update an existing evaluation
     */
    public Mono<EvaluationResponseDTO> updateEvaluation(Long evaluationId, CreateEvaluationDTO updateDto) {
        return evaluationRepository.findById(evaluationId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Évaluation non trouvée")))
                .flatMap(evaluation -> {
                    // Verify ownership
                    if (!evaluation.getEvaluateurId().equals(updateDto.getEvaluateurId())) {
                        return Mono.error(new IllegalArgumentException("Non autorisé à modifier cette évaluation"));
                    }

                    // Update fields
                    evaluation.setNote(updateDto.getNote());
                    evaluation.setCommentaire(updateDto.getCommentaire());
                    evaluation.setDate(LocalDateTime.now()); // Update timestamp
                    
                    // Update detailed criteria if provided
                    evaluation.setQualiteEnseignement(updateDto.getQualiteEnseignement());
                    evaluation.setCommunication(updateDto.getCommunication());
                    evaluation.setPonctualite(updateDto.getPonctualite());
                    evaluation.setPreparation(updateDto.getPreparation());
                    evaluation.setPatience(updateDto.getPatience());
                    evaluation.setRecommanderais(updateDto.getRecommanderais());

                    return evaluationRepository.save(evaluation);
                })
                .flatMap(this::convertToResponseDTO);
    }

    /**
     * Get evaluation by ID
     */
    public Mono<EvaluationResponseDTO> getEvaluationById(Long evaluationId) {
        return evaluationRepository.findById(evaluationId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Évaluation non trouvée")))
                .flatMap(this::convertToResponseDTO);
    }

    /**
     * Get evaluations for a session
     */
    public Flux<EvaluationResponseDTO> getEvaluationsBySession(Long sessionId) {
        return evaluationRepository.findBySessionId(sessionId)
                .flatMap(this::convertToResponseDTO);
    }

    /**
     * Get evaluations by user (as evaluator)
     */
    public Flux<EvaluationResponseDTO> getEvaluationsByEvaluator(Long evaluatorId) {
        return evaluationRepository.findByEvaluateurIdOrderByDateDesc(evaluatorId)
                .flatMap(this::convertToResponseDTO);
    }

    /**
     * Get evaluations for a user (as evaluated)
     */
    public Flux<EvaluationResponseDTO> getEvaluationsForUser(Long userId, EvaluationType type) {
        return evaluationRepository.findByEvalueIdAndTypeOrderByDateDesc(userId, type)
                .flatMap(this::convertToResponseDTO);
    }

    /**
     * Get recent evaluations for a user
     */
    public Flux<EvaluationResponseDTO> getRecentEvaluationsForUser(Long userId, EvaluationType type, int limit) {
        return evaluationRepository.findRecentEvaluationsByEvalueAndType(userId, type, limit)
                .flatMap(this::convertToResponseDTO);
    }

    /**
     * Check if evaluation exists for session and type
     */
    public Mono<Boolean> evaluationExists(Long sessionId, EvaluationType type) {
        return evaluationRepository.findBySessionIdAndType(sessionId, type)
                .hasElement();
    }

    /**
     * Get evaluation summary for a user
     */
    public Mono<EvaluationSummaryDTO> getEvaluationSummary(Long userId, EvaluationType type) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Utilisateur non trouvé")))
                .flatMap(user -> {
                    EvaluationSummaryDTO summary = new EvaluationSummaryDTO();
                    summary.setUserId(userId);
                    summary.setUserNom(user.getNom());
                    summary.setUserPrenom(user.getPrenom());
                    summary.setUserType(type == EvaluationType.ETUDIANT_VERS_TUTEUR ? "TUTEUR" : "ETUDIANT");

                    return Mono.zip(
                            evaluationRepository.getAverageRatingByEvalueAndType(userId, type).defaultIfEmpty(0.0),
                            evaluationRepository.countByEvalueIdAndType(userId, type).defaultIfEmpty(0L),
                            evaluationRepository.getRatingDistributionByEvalueAndType(userId, type).collectList(),
                            evaluationRepository.getEvaluationDateRangeByEvalueAndType(userId, type).defaultIfEmpty(new Object[]{null, null}),
                            getRecentEvaluationsForUser(userId, type, 5).collectList()
                    ).map(tuple -> {
                        summary.setNoteGlobale(tuple.getT1());
                        summary.setNombreEvaluations(tuple.getT2());
                        
                        // Process rating distribution
                        List<Object[]> distribution = tuple.getT3();
                        for (Object[] rating : distribution) {
                            Integer note = (Integer) rating[0];
                            Long count = (Long) rating[1];
                            switch (note) {
                                case 1 -> summary.setNote1Etoile(count.intValue());
                                case 2 -> summary.setNote2Etoiles(count.intValue());
                                case 3 -> summary.setNote3Etoiles(count.intValue());
                                case 4 -> summary.setNote4Etoiles(count.intValue());
                                case 5 -> summary.setNote5Etoiles(count.intValue());
                            }
                        }

                        // Set date range
                        Object[] dateRange = tuple.getT4();
                        if (dateRange[0] != null) {
                            summary.setPremiereEvaluation((LocalDateTime) dateRange[0]);
                            summary.setDerniereEvaluation((LocalDateTime) dateRange[1]);
                        }

                        // Set recent evaluations
                        summary.setEvaluationsRecentes(tuple.getT5());

                        return summary;
                    });
                });
    }

    /**
     * Delete evaluation (only by evaluator within time limit)
     */
    public Mono<Void> deleteEvaluation(Long evaluationId, Long evaluatorId) {
        return evaluationRepository.findById(evaluationId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Évaluation non trouvée")))
                .flatMap(evaluation -> {
                    // Verify ownership
                    if (!evaluation.getEvaluateurId().equals(evaluatorId)) {
                        return Mono.error(new IllegalArgumentException("Non autorisé à supprimer cette évaluation"));
                    }

                    // Check time limit (24 hours)
                    if (evaluation.getDate().isBefore(LocalDateTime.now().minusHours(24))) {
                        return Mono.error(new IllegalArgumentException("Délai de suppression dépassé"));
                    }

                    return evaluationRepository.delete(evaluation);
                });
    }

    /**
     * Get average rating for a user
     */
    public Mono<Double> getAverageRating(Long userId, EvaluationType type) {
        return evaluationRepository.getAverageRatingByEvalueAndType(userId, type)
                .defaultIfEmpty(0.0);
    }

    /**
     * Get evaluation count for a user
     */
    public Mono<Long> getEvaluationCount(Long userId, EvaluationType type) {
        return evaluationRepository.countByEvalueIdAndType(userId, type)
                .defaultIfEmpty(0L);
    }

    /**
     * Get evaluations with comments for a user
     */
    public Flux<EvaluationResponseDTO> getEvaluationsWithComments(Long userId, EvaluationType type) {
        return evaluationRepository.findEvaluationsWithCommentsByEvalueAndType(userId, type)
                .flatMap(this::convertToResponseDTO);
    }

    /**
     * Get positive evaluations count
     */
    public Mono<Long> getPositiveEvaluationsCount(Long userId, int minRating) {
        return evaluationRepository.countPositiveEvaluations(userId, minRating)
                .defaultIfEmpty(0L);
    }

    /**
     * Validate evaluation creation
     */
    private Mono<Boolean> validateEvaluationCreation(CreateEvaluationDTO createDto) {
        // Check if evaluation is valid
        if (!createDto.isValid()) {
            return Mono.just(false);
        }

        // Verify session exists and is completed
        return sessionRepository.findById(createDto.getSessionId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Session non trouvée")))
                .flatMap(session -> {
                    // Check session status
                    if (session.getStatut() != SessionStatus.TERMINEE) {
                        return Mono.error(new IllegalArgumentException("La session doit être terminée pour être évaluée"));
                    }

                    // Verify evaluator participated in session
                    boolean isParticipant = session.getTuteurId().equals(createDto.getEvaluateurId()) ||
                                           session.getEtudiantId().equals(createDto.getEvaluateurId());
                    if (!isParticipant) {
                        return Mono.error(new IllegalArgumentException("Seuls les participants peuvent évaluer la session"));
                    }

                    // Verify evaluation type matches evaluator role
                    boolean typeValid = (session.getTuteurId().equals(createDto.getEvaluateurId()) && 
                                        createDto.getTypeEvaluation() == EvaluationType.TUTEUR_VERS_ETUDIANT) ||
                                       (session.getEtudiantId().equals(createDto.getEvaluateurId()) && 
                                        createDto.getTypeEvaluation() == EvaluationType.ETUDIANT_VERS_TUTEUR);
                    if (!typeValid) {
                        return Mono.error(new IllegalArgumentException("Type d'évaluation incompatible avec le rôle"));
                    }

                    // Check if evaluation already exists
                    return evaluationRepository.findBySessionIdAndType(createDto.getSessionId(), createDto.getTypeEvaluation())
                            .hasElement()
                            .map(exists -> {
                                if (exists) {
                                    throw new IllegalArgumentException("Évaluation déjà existante pour cette session");
                                }
                                return true;
                            });
                });
    }

    /**
     * Convert Evaluation entity to ResponseDTO
     */
    private Mono<EvaluationResponseDTO> convertToResponseDTO(Evaluation evaluation) {
        return Mono.zip(
                userRepository.findById(evaluation.getEvaluateurId()),
                userRepository.findById(evaluation.getEvalueId()),
                sessionRepository.findById(evaluation.getSessionId())
        ).map(tuple -> {
            User evaluateur = tuple.getT1();
            User evalue = tuple.getT2();
            Session session = tuple.getT3();

            EvaluationResponseDTO dto = new EvaluationResponseDTO();
            dto.setId(evaluation.getId());
            dto.setSessionId(evaluation.getSessionId());
            dto.setEvaluateurId(evaluation.getEvaluateurId());
            dto.setEvalueId(evaluation.getEvalueId());
            dto.setNote(evaluation.getNote());
            dto.setCommentaire(evaluation.getCommentaire());
            dto.setDate(evaluation.getDate());
            dto.setTypeEvaluation(evaluation.getTypeEvaluation());

            // Set detailed criteria
            dto.setQualiteEnseignement(evaluation.getQualiteEnseignement());
            dto.setCommunication(evaluation.getCommunication());
            dto.setPonctualite(evaluation.getPonctualite());
            dto.setPreparation(evaluation.getPreparation());
            dto.setPatience(evaluation.getPatience());
            dto.setRecommanderais(evaluation.getRecommanderais());

            // Set evaluateur info
            dto.setEvaluateurNom(evaluateur.getNom());
            dto.setEvaluateurPrenom(evaluateur.getPrenom());
            dto.setEvaluateurEmail(evaluateur.getEmail());

            // Set evalue info
            dto.setEvalueNom(evalue.getNom());
            dto.setEvaluePrenom(evalue.getPrenom());
            dto.setEvalueEmail(evalue.getEmail());

            // Set session info
            dto.setSessionDate(session.getDateHeure());
            dto.setSessionDuree(session.getDuree());

            return dto;
        });
    }

    /**
     * Get session evaluation completion status
     */
    public Mono<Object[]> getSessionEvaluationStatus(Long sessionId) {
        return Mono.zip(
                evaluationRepository.findBySessionIdAndType(sessionId, EvaluationType.ETUDIANT_VERS_TUTEUR).hasElement(),
                evaluationRepository.findBySessionIdAndType(sessionId, EvaluationType.TUTEUR_VERS_ETUDIANT).hasElement()
        ).map(tuple -> new Object[]{tuple.getT1(), tuple.getT2()});
    }
}