package com.iiil.tutoring.service.evaluation;

import com.iiil.tutoring.dto.evaluation.EvaluationResponseDTO;
import com.iiil.tutoring.dto.evaluation.SessionFeedbackDTO;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for comprehensive session feedback analysis and platform-wide evaluation metrics
 */
@Service
public class FeedbackAnalyticsService {

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EvaluationService evaluationService;

    /**
     * Get comprehensive session feedback with evaluations
     */
    public Mono<SessionFeedbackDTO> getSessionFeedback(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Session non trouvée")))
                .flatMap(session -> {
                    SessionFeedbackDTO dto = new SessionFeedbackDTO();
                    dto.setSessionId(sessionId);
                    dto.setSessionDate(session.getDateHeure());
                    dto.setSessionDuree(session.getDuree());
                    dto.setSessionDescription(session.getNotes());

                    return Mono.zip(
                            userRepository.findById(session.getTuteurId()),
                            userRepository.findById(session.getEtudiantId()),
                            getSessionEvaluations(sessionId)
                    ).map(tuple -> {
                        var tuteur = tuple.getT1();
                        var etudiant = tuple.getT2();
                        var evaluations = tuple.getT3();

                        // Set participant information
                        dto.setTuteurId(session.getTuteurId());
                        dto.setTuteurNom(tuteur.getNom());
                        dto.setTuteurPrenom(tuteur.getPrenom());
                        dto.setEtudiantId(session.getEtudiantId());
                        dto.setEtudiantNom(etudiant.getNom());
                        dto.setEtudiantPrenom(etudiant.getPrenom());

                        // Process evaluations
                        enrichSessionFeedbackWithEvaluations(dto, evaluations);

                        return dto;
                    });
                });
    }

    /**
     * Get session feedback completion rate for a user
     */
    public Mono<Map<String, Object>> getSessionFeedbackCompletionRate(Long userId) {
        return evaluationRepository.getSessionEvaluationCompletionRate(userId, EvaluationType.ETUDIANT_VERS_TUTEUR)
                .zipWith(evaluationRepository.getSessionEvaluationCompletionRate(userId, EvaluationType.TUTEUR_VERS_ETUDIANT))
                .map(tuple -> {
                    Object[] asTutorData = tuple.getT1();
                    Object[] asStudentData = tuple.getT2();

                    Map<String, Object> completionStats = new HashMap<>();
                    
                    // As tutor (being evaluated by students)
                    Long totalSessionsAsTutor = (Long) asTutorData[0];
                    Long evaluatedSessionsAsTutor = (Long) asTutorData[1];
                    double tutorCompletionRate = totalSessionsAsTutor > 0 ? 
                        (double) evaluatedSessionsAsTutor / totalSessionsAsTutor * 100 : 0.0;

                    // As student (being evaluated by tutors)
                    Long totalSessionsAsStudent = (Long) asStudentData[0];
                    Long evaluatedSessionsAsStudent = (Long) asStudentData[1];
                    double studentCompletionRate = totalSessionsAsStudent > 0 ? 
                        (double) evaluatedSessionsAsStudent / totalSessionsAsStudent * 100 : 0.0;

                    completionStats.put("asTutor", Map.of(
                        "totalSessions", totalSessionsAsTutor,
                        "evaluatedSessions", evaluatedSessionsAsTutor,
                        "completionRate", tutorCompletionRate
                    ));

                    completionStats.put("asStudent", Map.of(
                        "totalSessions", totalSessionsAsStudent,
                        "evaluatedSessions", evaluatedSessionsAsStudent,
                        "completionRate", studentCompletionRate
                    ));

                    completionStats.put("overallCompletionRate", 
                        (tutorCompletionRate + studentCompletionRate) / 2);

                    return completionStats;
                });
    }

    /**
     * Get platform-wide evaluation statistics
     */
    public Mono<Map<String, Object>> getPlatformEvaluationStatistics() {
        return Mono.zip(
                evaluationRepository.getGlobalAverageRatingByType(EvaluationType.ETUDIANT_VERS_TUTEUR),
                evaluationRepository.getGlobalAverageRatingByType(EvaluationType.TUTEUR_VERS_ETUDIANT),
                evaluationRepository.getGlobalRatingDistributionByType(EvaluationType.ETUDIANT_VERS_TUTEUR).collectList(),
                evaluationRepository.getGlobalRatingDistributionByType(EvaluationType.TUTEUR_VERS_ETUDIANT).collectList(),
                evaluationRepository.findRecentPositiveEvaluations(4, 50).collectList()
        ).map(tuple -> {
            Double tutorAverage = tuple.getT1();
            Double studentAverage = tuple.getT2();
            List<Object[]> tutorDistribution = tuple.getT3();
            List<Object[]> studentDistribution = tuple.getT4();
                List<Object> recentPositive = tuple.getT5().stream()
                        .map(evaluation -> (Object) evaluation)
                        .collect(Collectors.toList());

            Map<String, Object> stats = new HashMap<>();
            stats.put("tutorAverageRating", tutorAverage != null ? tutorAverage : 0.0);
            stats.put("studentAverageRating", studentAverage != null ? studentAverage : 0.0);
            stats.put("overallAverageRating", 
                (tutorAverage != null && studentAverage != null) ? 
                (tutorAverage + studentAverage) / 2 : 0.0);

            // Process rating distributions
            stats.put("tutorRatingDistribution", processRatingDistribution(tutorDistribution));
            stats.put("studentRatingDistribution", processRatingDistribution(studentDistribution));
            stats.put("recentPositiveEvaluationsCount", recentPositive.size());

            return stats;
        });
    }

    /**
     * Get feedback trends over time
     */
    public Flux<Map<String, Object>> getFeedbackTrends(int months) {
        return evaluationRepository.getMonthlyAverageRatingByEvalueAndType(null, EvaluationType.ETUDIANT_VERS_TUTEUR)
                .zipWith(evaluationRepository.getMonthlyAverageRatingByEvalueAndType(null, EvaluationType.TUTEUR_VERS_ETUDIANT))
                .map(tuple -> {
                    Object[] tutorData = tuple.getT1();
                    Object[] studentData = tuple.getT2();

                    Map<String, Object> monthlyTrend = new HashMap<>();
                    monthlyTrend.put("month", tutorData[0]);
                    monthlyTrend.put("tutorAverage", tutorData[1]);
                    monthlyTrend.put("studentAverage", studentData[1]);
                    monthlyTrend.put("overallAverage", 
                        ((Double) tutorData[1] + (Double) studentData[1]) / 2);

                    return monthlyTrend;
                });
    }

    /**
     * Identify common feedback themes and improvement areas
     */
    public Mono<Map<String, Object>> getFeedbackThemesAnalysis() {
        return evaluationRepository.findEvaluationsWithCommentsByEvalueAndType(null, EvaluationType.ETUDIANT_VERS_TUTEUR)
                .collectList()
                .map(evaluations -> {
                    Map<String, Object> analysis = new HashMap<>();
                    List<String> positiveThemes = new ArrayList<>();
                    List<String> improvementAreas = new ArrayList<>();
                    Map<String, Integer> keywordFrequency = new HashMap<>();

                    // Analyze comments for themes (simplified keyword analysis)
                    // This would be enhanced with NLP in a real implementation
                    // For now, we'll use the evaluation count as a metric
                    analysis.put("totalCommentsAnalyzed", evaluations.size());

                    analysis.put("positiveThemes", positiveThemes.stream().distinct().limit(10).collect(Collectors.toList()));
                    analysis.put("improvementAreas", improvementAreas.stream().distinct().limit(10).collect(Collectors.toList()));
                    analysis.put("keywordFrequency", keywordFrequency);

                    return analysis;
                });
    }

    /**
     * Get session success metrics
     */
    public Mono<Map<String, Object>> getSessionSuccessMetrics() {
        return sessionRepository.findByStatut(SessionStatus.TERMINEE)
                .collectList()
                .flatMap(completedSessions -> {
                    List<Mono<Map<String, Object>>> sessionAnalytics = completedSessions.stream()
                            .map(session -> getSessionSuccessIndicators(session.getId()))
                            .collect(Collectors.toList());

                    return Flux.fromIterable(sessionAnalytics)
                            .flatMap(mono -> mono)
                            .collectList()
                            .map(sessionData -> {
                                Map<String, Object> metrics = new HashMap<>();
                                
                                long totalSessions = sessionData.size();
                                long successfulSessions = sessionData.stream()
                                        .mapToLong(data -> (Boolean) data.get("successful") ? 1 : 0)
                                        .sum();

                                double averageRating = sessionData.stream()
                                        .mapToDouble(data -> (Double) data.getOrDefault("averageRating", 0.0))
                                        .average()
                                        .orElse(0.0);

                                metrics.put("totalCompletedSessions", totalSessions);
                                metrics.put("successfulSessions", successfulSessions);
                                metrics.put("successRate", totalSessions > 0 ? (double) successfulSessions / totalSessions * 100 : 0.0);
                                metrics.put("averageSessionRating", averageRating);

                                return metrics;
                            });
                });
    }

    /**
     * Get user satisfaction insights
     */
    public Mono<Map<String, Object>> getUserSatisfactionInsights(Long userId) {
        return Mono.zip(
                evaluationService.getEvaluationsByEvaluator(userId).collectList(),
                evaluationService.getEvaluationsForUser(userId, EvaluationType.ETUDIANT_VERS_TUTEUR).collectList(),
                evaluationService.getEvaluationsForUser(userId, EvaluationType.TUTEUR_VERS_ETUDIANT).collectList()
        ).map(tuple -> {
            List<EvaluationResponseDTO> givenEvaluations = tuple.getT1();
            List<EvaluationResponseDTO> receivedAsTutor = tuple.getT2();
            List<EvaluationResponseDTO> receivedAsStudent = tuple.getT3();

            Map<String, Object> insights = new HashMap<>();
            
            // Analyze evaluation patterns
            double avgGivenRating = givenEvaluations.stream()
                    .mapToInt(EvaluationResponseDTO::getNote)
                    .average()
                    .orElse(0.0);

            double avgReceivedAsTutor = receivedAsTutor.stream()
                    .mapToInt(EvaluationResponseDTO::getNote)
                    .average()
                    .orElse(0.0);

            double avgReceivedAsStudent = receivedAsStudent.stream()
                    .mapToInt(EvaluationResponseDTO::getNote)
                    .average()
                    .orElse(0.0);

            insights.put("evaluationStyle", analyzeEvaluationStyle(avgGivenRating));
            insights.put("performanceAsTutor", analyzePerformance(avgReceivedAsTutor));
            insights.put("performanceAsStudent", analyzePerformance(avgReceivedAsStudent));
            insights.put("satisfactionLevel", calculateOverallSatisfaction(avgReceivedAsTutor, avgReceivedAsStudent));

            return insights;
        });
    }

    // Helper methods

    private Mono<List<EvaluationResponseDTO>> getSessionEvaluations(Long sessionId) {
        return evaluationService.getEvaluationsBySession(sessionId).collectList();
    }

    private void enrichSessionFeedbackWithEvaluations(SessionFeedbackDTO dto, List<EvaluationResponseDTO> evaluations) {
        for (EvaluationResponseDTO eval : evaluations) {
            if (eval.getTypeEvaluation() == EvaluationType.ETUDIANT_VERS_TUTEUR) {
                dto.setEvaluationTuteurComplete(true);
                dto.setDateEvaluationTuteur(eval.getDate());
                dto.setNoteTuteur(eval.getNote());
                dto.setCommentaireSurTuteur(eval.getCommentaire());
            } else if (eval.getTypeEvaluation() == EvaluationType.TUTEUR_VERS_ETUDIANT) {
                dto.setEvaluationEtudiantComplete(true);
                dto.setDateEvaluationEtudiant(eval.getDate());
                dto.setNoteEtudiant(eval.getNote());
                dto.setCommentaireSurEtudiant(eval.getCommentaire());
            }
        }

        // Calculate overall session rating
        if (dto.getNoteTuteur() != null && dto.getNoteEtudiant() != null) {
            dto.setNoteGlobaleSession((dto.getNoteTuteur() + dto.getNoteEtudiant()) / 2.0);
        } else if (dto.getNoteTuteur() != null) {
            dto.setNoteGlobaleSession(dto.getNoteTuteur().doubleValue());
        } else if (dto.getNoteEtudiant() != null) {
            dto.setNoteGlobaleSession(dto.getNoteEtudiant().doubleValue());
        }
    }

    private Map<String, Long> processRatingDistribution(List<Object[]> distribution) {
        Map<String, Long> processed = new HashMap<>();
        for (Object[] rating : distribution) {
            Integer note = (Integer) rating[0];
            Long count = (Long) rating[1];
            processed.put(note + "_stars", count);
        }
        return processed;
    }

    private Mono<Map<String, Object>> getSessionSuccessIndicators(Long sessionId) {
        return getSessionFeedback(sessionId)
                .map(feedback -> {
                    Map<String, Object> indicators = new HashMap<>();
                    
                    boolean hasEvaluations = feedback.isEvaluationComplete();
                    Double avgRating = feedback.getNoteMoyenneSession();
                    boolean isSuccessful = avgRating != null && avgRating >= 3.5;

                    indicators.put("hasEvaluations", hasEvaluations);
                    indicators.put("averageRating", avgRating != null ? avgRating : 0.0);
                    indicators.put("successful", isSuccessful);

                    return indicators;
                });
    }

    private String analyzeEvaluationStyle(double avgGivenRating) {
        if (avgGivenRating >= 4.5) {
            return "Très généreux";
        } else if (avgGivenRating >= 4.0) {
            return "Bienveillant";
        } else if (avgGivenRating >= 3.5) {
            return "Équilibré";
        } else if (avgGivenRating >= 3.0) {
            return "Critique constructif";
        } else {
            return "Très critique";
        }
    }

    private String analyzePerformance(double avgRating) {
        if (avgRating >= 4.5) {
            return "Excellent";
        } else if (avgRating >= 4.0) {
            return "Très bien";
        } else if (avgRating >= 3.5) {
            return "Bien";
        } else if (avgRating >= 3.0) {
            return "Satisfaisant";
        } else {
            return "À améliorer";
        }
    }

    private String calculateOverallSatisfaction(double tutorRating, double studentRating) {
        double overall = (tutorRating + studentRating) / 2;
        if (overall >= 4.5) {
            return "Très satisfait";
        } else if (overall >= 4.0) {
            return "Satisfait";
        } else if (overall >= 3.5) {
            return "Moyennement satisfait";
        } else {
            return "Peu satisfait";
        }
    }
}