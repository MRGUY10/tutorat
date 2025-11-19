package com.iiil.tutoring.controller;

import com.iiil.tutoring.dto.evaluation.*;
import com.iiil.tutoring.enums.EvaluationType;
import com.iiil.tutoring.service.evaluation.EvaluationService;
import com.iiil.tutoring.service.evaluation.FeedbackAnalyticsService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * REST Controller for comprehensive evaluation management and analytics
 */
@RestController
@RequestMapping("/api/evaluations")
@CrossOrigin(origins = "*")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;



    @Autowired
    private FeedbackAnalyticsService feedbackAnalyticsService;

    // ===== EVALUATION CRUD OPERATIONS =====

    /**
     * Create a new evaluation
     */
    @PostMapping
    public Mono<ResponseEntity<EvaluationResponseDTO>> createEvaluation(@Valid @RequestBody CreateEvaluationDTO createDto) {
        return evaluationService.createEvaluation(createDto)
                .map(evaluation -> ResponseEntity.status(HttpStatus.CREATED).body(evaluation))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Update an existing evaluation
     */
    @PutMapping("/{evaluationId}")
    public Mono<ResponseEntity<EvaluationResponseDTO>> updateEvaluation(
            @PathVariable Long evaluationId,
            @Valid @RequestBody CreateEvaluationDTO updateDto) {
        return evaluationService.updateEvaluation(evaluationId, updateDto)
                .map(evaluation -> ResponseEntity.ok(evaluation))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Get evaluation by ID
     */
    @GetMapping("/{evaluationId}")
    public Mono<ResponseEntity<EvaluationResponseDTO>> getEvaluation(@PathVariable Long evaluationId) {
        return evaluationService.getEvaluationById(evaluationId)
                .map(evaluation -> ResponseEntity.ok(evaluation))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Delete an evaluation
     */
    @DeleteMapping("/{evaluationId}")
    public Mono<ResponseEntity<Void>> deleteEvaluation(
            @PathVariable Long evaluationId,
            @RequestParam Long evaluatorId) {
        return evaluationService.deleteEvaluation(evaluationId, evaluatorId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    // ===== EVALUATION RETRIEVAL =====

    /**
     * Get evaluations for a session
     */
    @GetMapping("/session/{sessionId}")
    public Flux<EvaluationResponseDTO> getEvaluationsBySession(@PathVariable Long sessionId) {
        return evaluationService.getEvaluationsBySession(sessionId);
    }

    /**
     * Get evaluations by evaluator
     */
    @GetMapping("/evaluator/{evaluatorId}")
    public Flux<EvaluationResponseDTO> getEvaluationsByEvaluator(@PathVariable Long evaluatorId) {
        return evaluationService.getEvaluationsByEvaluator(evaluatorId);
    }

    /**
     * Get evaluations for a user (as evaluated)
     */
    @GetMapping("/user/{userId}")
    public Flux<EvaluationResponseDTO> getEvaluationsForUser(
            @PathVariable Long userId,
            @RequestParam EvaluationType type) {
        return evaluationService.getEvaluationsForUser(userId, type);
    }

    /**
     * Get recent evaluations for a user
     */
    @GetMapping("/user/{userId}/recent")
    public Flux<EvaluationResponseDTO> getRecentEvaluationsForUser(
            @PathVariable Long userId,
            @RequestParam EvaluationType type,
            @RequestParam(defaultValue = "5") int limit) {
        return evaluationService.getRecentEvaluationsForUser(userId, type, limit);
    }

    /**
     * Get evaluations with comments for a user
     */
    @GetMapping("/user/{userId}/comments")
    public Flux<EvaluationResponseDTO> getEvaluationsWithComments(
            @PathVariable Long userId,
            @RequestParam EvaluationType type) {
        return evaluationService.getEvaluationsWithComments(userId, type);
    }

    // ===== EVALUATION SUMMARIES AND STATISTICS =====

    /**
     * Get evaluation summary for a user
     */
    @GetMapping("/user/{userId}/summary")
    public Mono<ResponseEntity<EvaluationSummaryDTO>> getEvaluationSummary(
            @PathVariable Long userId,
            @RequestParam EvaluationType type) {
        return evaluationService.getEvaluationSummary(userId, type)
                .map(summary -> ResponseEntity.ok(summary))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Get average rating for a user
     */
    @GetMapping("/user/{userId}/average")
    public Mono<ResponseEntity<Map<String, Object>>> getAverageRating(
            @PathVariable Long userId,
            @RequestParam EvaluationType type) {
        return evaluationService.getAverageRating(userId, type)
                .zipWith(evaluationService.getEvaluationCount(userId, type))
                .map(tuple -> {
                    Map<String, Object> result = Map.of(
                        "averageRating", tuple.getT1(),
                        "evaluationCount", tuple.getT2()
                    );
                    return ResponseEntity.ok(result);
                });
    }

    /**
     * Check if evaluation exists for session
     */
    @GetMapping("/session/{sessionId}/status")
    public Mono<ResponseEntity<Map<String, Object>>> getSessionEvaluationStatus(@PathVariable Long sessionId) {
        return evaluationService.getSessionEvaluationStatus(sessionId)
                .map(status -> {
                    Map<String, Object> result = Map.of(
                        "studentEvaluationExists", status[0],
                        "tutorEvaluationExists", status[1],
                        "evaluationComplete", (Boolean) status[0] && (Boolean) status[1]
                    );
                    return ResponseEntity.ok(result);
                });
    }



    // ===== SESSION FEEDBACK ANALYTICS =====

    /**
     * Get comprehensive session feedback
     */
    @GetMapping("/session/{sessionId}/feedback")
    public Mono<ResponseEntity<SessionFeedbackDTO>> getSessionFeedback(@PathVariable Long sessionId) {
        return feedbackAnalyticsService.getSessionFeedback(sessionId)
                .map(feedback -> ResponseEntity.ok(feedback))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Get session feedback completion rate for user
     */
    @GetMapping("/user/{userId}/completion-rate")
    public Mono<ResponseEntity<Map<String, Object>>> getSessionFeedbackCompletionRate(@PathVariable Long userId) {
        return feedbackAnalyticsService.getSessionFeedbackCompletionRate(userId)
                .map(rate -> ResponseEntity.ok(rate));
    }

    /**
     * Get user satisfaction insights
     */
    @GetMapping("/user/{userId}/satisfaction")
    public Mono<ResponseEntity<Map<String, Object>>> getUserSatisfactionInsights(@PathVariable Long userId) {
        return feedbackAnalyticsService.getUserSatisfactionInsights(userId)
                .map(insights -> ResponseEntity.ok(insights));
    }

    // ===== PLATFORM-WIDE ANALYTICS =====

    /**
     * Get platform-wide evaluation statistics
     */
    @GetMapping("/platform/statistics")
    public Mono<ResponseEntity<Map<String, Object>>> getPlatformEvaluationStatistics() {
        return feedbackAnalyticsService.getPlatformEvaluationStatistics()
                .map(stats -> ResponseEntity.ok(stats));
    }

    /**
     * Get feedback trends over time
     */
    @GetMapping("/platform/trends")
    public Flux<Map<String, Object>> getFeedbackTrends(@RequestParam(defaultValue = "12") int months) {
        return feedbackAnalyticsService.getFeedbackTrends(months);
    }

    /**
     * Get feedback themes analysis
     */
    @GetMapping("/platform/themes")
    public Mono<ResponseEntity<Map<String, Object>>> getFeedbackThemesAnalysis() {
        return feedbackAnalyticsService.getFeedbackThemesAnalysis()
                .map(themes -> ResponseEntity.ok(themes));
    }

    /**
     * Get session success metrics
     */
    @GetMapping("/platform/success-metrics")
    public Mono<ResponseEntity<Map<String, Object>>> getSessionSuccessMetrics() {
        return feedbackAnalyticsService.getSessionSuccessMetrics()
                .map(metrics -> ResponseEntity.ok(metrics));
    }

    // ===== UTILITY ENDPOINTS =====

    /**
     * Check if user can evaluate a session
     */
    @GetMapping("/session/{sessionId}/can-evaluate")
    public Mono<ResponseEntity<Map<String, Object>>> canEvaluateSession(
            @PathVariable Long sessionId,
            @RequestParam Long userId,
            @RequestParam EvaluationType type) {
        return evaluationService.evaluationExists(sessionId, type)
                .map(exists -> {
                    Map<String, Object> result = Map.of(
                        "canEvaluate", !exists,
                        "evaluationExists", exists
                    );
                    return ResponseEntity.ok(result);
                });
    }

    /**
     * Get positive evaluations count for user
     */
    @GetMapping("/user/{userId}/positive-count")
    public Mono<ResponseEntity<Map<String, Object>>> getPositiveEvaluationsCount(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "4") int minRating) {
        return evaluationService.getPositiveEvaluationsCount(userId, minRating)
                .zipWith(evaluationService.getEvaluationCount(userId, EvaluationType.ETUDIANT_VERS_TUTEUR))
                .map(tuple -> {
                    Long positiveCount = tuple.getT1();
                    Long totalCount = tuple.getT2();
                    double percentage = totalCount > 0 ? (positiveCount * 100.0) / totalCount : 0.0;
                    
                    Map<String, Object> result = Map.of(
                        "positiveCount", positiveCount,
                        "totalCount", totalCount,
                        "positivePercentage", percentage
                    );
                    return ResponseEntity.ok(result);
                });
    }

    // ===== ERROR HANDLING =====

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> error = Map.of(
            "error", "Invalid request",
            "message", ex.getMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        Map<String, String> error = Map.of(
            "error", "Internal server error",
            "message", "Une erreur inattendue s'est produite"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}