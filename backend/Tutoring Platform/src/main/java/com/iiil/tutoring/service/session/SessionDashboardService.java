package com.iiil.tutoring.service.session;

import com.iiil.tutoring.entity.Session;
import com.iiil.tutoring.enums.SessionStatus;
import com.iiil.tutoring.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Service for session dashboard analytics, statistics, and reporting capabilities
 */
@Service
public class SessionDashboardService {

    private final SessionRepository sessionRepository;

    @Autowired
    public SessionDashboardService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    // ===============================================
    // USER-SPECIFIC DASHBOARD DATA
    // ===============================================

    /**
     * Get comprehensive dashboard data for a tutor
     */
    public Mono<TutorDashboard> getTutorDashboard(Long tutorId) {
        return Mono.zip(
                getTutorStatistics(tutorId),
                getUpcomingSessions(tutorId, null, 10),
                getRecentSessions(tutorId, null, 5),
                getTutorPerformanceMetrics(tutorId),
                getTutorEarningsData(tutorId)
        ).map(tuple -> {
            TutorDashboard dashboard = new TutorDashboard();
            dashboard.setStatistics(tuple.getT1());
            dashboard.setUpcomingSessions(tuple.getT2());
            dashboard.setRecentSessions(tuple.getT3());
            dashboard.setPerformanceMetrics(tuple.getT4());
            dashboard.setEarningsData(tuple.getT5());
            return dashboard;
        });
    }

    /**
     * Get comprehensive dashboard data for a student
     */
    public Mono<StudentDashboard> getStudentDashboard(Long studentId) {
        return Mono.zip(
                getStudentStatistics(studentId),
                getUpcomingSessions(null, studentId, 10),
                getRecentSessions(null, studentId, 5),
                getStudentLearningMetrics(studentId),
                getStudentSpendingData(studentId)
        ).map(tuple -> {
            StudentDashboard dashboard = new StudentDashboard();
            dashboard.setStatistics(tuple.getT1());
            dashboard.setUpcomingSessions(tuple.getT2());
            dashboard.setRecentSessions(tuple.getT3());
            dashboard.setLearningMetrics(tuple.getT4());
            dashboard.setSpendingData(tuple.getT5());
            return dashboard;
        });
    }

    /**
     * Get admin dashboard with platform-wide statistics
     */
    public Mono<AdminDashboard> getAdminDashboard() {
        return Mono.zip(
                getPlatformStatistics(),
                getSessionVolumeData(),
                getRevenueData(),
                getUserActivityData(),
                getPopularSubjectsData()
        ).map(tuple -> {
            AdminDashboard dashboard = new AdminDashboard();
            dashboard.setPlatformStatistics(tuple.getT1());
            dashboard.setSessionVolumeData(tuple.getT2());
            dashboard.setRevenueData(tuple.getT3());
            dashboard.setUserActivityData(tuple.getT4());
            dashboard.setPopularSubjectsData(tuple.getT5());
            return dashboard;
        });
    }

    // ===============================================
    // STATISTICS COMPUTATION
    // ===============================================

    /**
     * Get tutor-specific statistics
     */
    public Mono<TutorStatistics> getTutorStatistics(Long tutorId) {
        return Mono.zip(
                sessionRepository.countCompletedSessionsByTuteur(tutorId),
                sessionRepository.getTotalEarningsByTuteur(tutorId),
                getAverageSessionRating(tutorId),
                getSessionCountByStatus(tutorId, null),
                getThisMonthSessionCount(tutorId, null),
                getActiveStudentCount(tutorId)
        ).map(tuple -> {
            TutorStatistics stats = new TutorStatistics();
            stats.setTotalCompletedSessions(tuple.getT1());
            stats.setTotalEarnings(tuple.getT2() != null ? tuple.getT2() : 0.0);
            stats.setAverageRating(tuple.getT3());
            stats.setSessionsByStatus(tuple.getT4());
            stats.setThisMonthSessions(tuple.getT5());
            stats.setActiveStudents(tuple.getT6());
            return stats;
        });
    }

    /**
     * Get student-specific statistics
     */
    public Mono<StudentStatistics> getStudentStatistics(Long studentId) {
        return Mono.zip(
                sessionRepository.countCompletedSessionsByEtudiant(studentId),
                getTotalSpent(studentId),
                getSessionCountByStatus(null, studentId),
                getThisMonthSessionCount(null, studentId),
                getActiveTutorCount(studentId),
                getFavoriteSubjects(studentId)
        ).map(tuple -> {
            StudentStatistics stats = new StudentStatistics();
            stats.setTotalCompletedSessions(tuple.getT1());
            stats.setTotalSpent(tuple.getT2());
            stats.setSessionsByStatus(tuple.getT3());
            stats.setThisMonthSessions(tuple.getT4());
            stats.setActiveTutors(tuple.getT5());
            stats.setFavoriteSubjects(tuple.getT6());
            return stats;
        });
    }

    /**
     * Get platform-wide statistics
     */
    public Mono<PlatformStatistics> getPlatformStatistics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime lastMonth = thisMonth.minusMonths(1);
        
        return Mono.zip(
                getTotalSessionsCount(),
                getActiveUsersCount(),
                getMonthlySessionsCount(thisMonth, now),
                getMonthlySessionsCount(lastMonth, thisMonth),
                getTotalRevenueThisMonth(),
                getAveragePlatformRating()
        ).map(tuple -> {
            PlatformStatistics stats = new PlatformStatistics();
            stats.setTotalSessions(tuple.getT1());
            stats.setActiveUsers(tuple.getT2());
            stats.setThisMonthSessions(tuple.getT3());
            stats.setLastMonthSessions(tuple.getT4());
            stats.setMonthlyRevenue(tuple.getT5());
            stats.setAveragePlatformRating(tuple.getT6());
            
            // Calculate growth rate
            if (tuple.getT4() > 0) {
                double growthRate = ((double) (tuple.getT3() - tuple.getT4()) / tuple.getT4()) * 100;
                stats.setGrowthRate(growthRate);
            }
            
            return stats;
        });
    }

    // ===============================================
    // PERFORMANCE METRICS
    // ===============================================

    /**
     * Get tutor performance metrics
     */
    public Mono<PerformanceMetrics> getTutorPerformanceMetrics(Long tutorId) {
        return Mono.zip(
                getSessionCompletionRate(tutorId, null),
                getOnTimeRate(tutorId, null),
                getSessionCancellationRate(tutorId, null),
                getResponseTime(tutorId),
                getStudentRetentionRate(tutorId)
        ).map(tuple -> {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setCompletionRate(tuple.getT1());
            metrics.setOnTimeRate(tuple.getT2());
            metrics.setCancellationRate(tuple.getT3());
            metrics.setAverageResponseTime(tuple.getT4());
            metrics.setRetentionRate(tuple.getT5());
            return metrics;
        });
    }

    /**
     * Get student learning metrics
     */
    public Mono<LearningMetrics> getStudentLearningMetrics(Long studentId) {
        return Mono.zip(
                getTotalLearningHours(studentId),
                getAverageSessionDuration(studentId),
                getSubjectDiversity(studentId),
                getConsistencyScore(studentId),
                getProgressScore(studentId)
        ).map(tuple -> {
            LearningMetrics metrics = new LearningMetrics();
            metrics.setTotalHours(tuple.getT1());
            metrics.setAverageSessionDuration(tuple.getT2());
            metrics.setSubjectDiversity(tuple.getT3());
            metrics.setConsistencyScore(tuple.getT4());
            metrics.setProgressScore(tuple.getT5());
            return metrics;
        });
    }

    // ===============================================
    // TIME-BASED ANALYTICS
    // ===============================================

    /**
     * Get session volume data over time
     */
    public Mono<SessionVolumeData> getSessionVolumeData() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sixMonthsAgo = now.minusMonths(6);
        
        return sessionRepository.findByDateHeureBetween(sixMonthsAgo, now)
                .collectList()
                .map(sessions -> {
                    SessionVolumeData volumeData = new SessionVolumeData();
                    Map<String, Integer> monthlyVolume = new HashMap<>();
                    
                    sessions.forEach(session -> {
                        String monthKey = session.getDateHeure().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                        monthlyVolume.merge(monthKey, 1, Integer::sum);
                    });
                    
                    volumeData.setMonthlyVolume(monthlyVolume);
                    volumeData.setTotalSessions(sessions.size());
                    volumeData.setAveragePerMonth(sessions.size() / 6.0);
                    
                    return volumeData;
                });
    }

    /**
     * Get earnings data for a tutor
     */
    public Mono<EarningsData> getTutorEarningsData(Long tutorId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yearStart = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
        
        return sessionRepository.findByTuteurIdAndStatutOrderByDateHeureDesc(tutorId, SessionStatus.TERMINEE)
                .filter(session -> session.getDateHeure().isAfter(yearStart))
                .collectList()
                .map(sessions -> {
                    EarningsData earningsData = new EarningsData();
                    Map<String, Double> monthlyEarnings = new HashMap<>();
                    
                    sessions.forEach(session -> {
                        String monthKey = session.getDateHeure().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                        monthlyEarnings.merge(monthKey, session.getPrix(), Double::sum);
                    });
                    
                    earningsData.setMonthlyEarnings(monthlyEarnings);
                    earningsData.setTotalEarnings(sessions.stream().mapToDouble(Session::getPrix).sum());
                    earningsData.setAveragePerSession(sessions.isEmpty() ? 0 : 
                            sessions.stream().mapToDouble(Session::getPrix).average().orElse(0));
                    
                    return earningsData;
                });
    }

    /**
     * Get spending data for a student
     */
    public Mono<SpendingData> getStudentSpendingData(Long studentId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yearStart = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
        
        return sessionRepository.findByEtudiantIdAndStatutOrderByDateHeureDesc(studentId, SessionStatus.TERMINEE)
                .filter(session -> session.getDateHeure().isAfter(yearStart))
                .collectList()
                .map(sessions -> {
                    SpendingData spendingData = new SpendingData();
                    Map<String, Double> monthlySpending = new HashMap<>();
                    
                    sessions.forEach(session -> {
                        String monthKey = session.getDateHeure().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                        monthlySpending.merge(monthKey, session.getPrix(), Double::sum);
                    });
                    
                    spendingData.setMonthlySpending(monthlySpending);
                    spendingData.setTotalSpent(sessions.stream().mapToDouble(Session::getPrix).sum());
                    spendingData.setAveragePerSession(sessions.isEmpty() ? 0 : 
                            sessions.stream().mapToDouble(Session::getPrix).average().orElse(0));
                    
                    return spendingData;
                });
    }

    // ===============================================
    // UTILITY METHODS
    // ===============================================

    private Mono<List<Session>> getUpcomingSessions(Long tutorId, Long studentId, int limit) {
        LocalDateTime now = LocalDateTime.now();
        
        if (tutorId != null) {
            return sessionRepository.findUpcomingSessionsByTuteur(tutorId, now)
                    .take(limit)
                    .collectList();
        } else if (studentId != null) {
            return sessionRepository.findUpcomingSessionsByEtudiant(studentId, now)
                    .take(limit)
                    .collectList();
        }
        
        return Mono.just(new ArrayList<>());
    }

    private Mono<List<Session>> getRecentSessions(Long tutorId, Long studentId, int limit) {
        if (tutorId != null) {
            return sessionRepository.findByTuteurIdAndStatutOrderByDateHeureDesc(tutorId, SessionStatus.TERMINEE)
                    .take(limit)
                    .collectList();
        } else if (studentId != null) {
            return sessionRepository.findByEtudiantIdAndStatutOrderByDateHeureDesc(studentId, SessionStatus.TERMINEE)
                    .take(limit)
                    .collectList();
        }
        
        return Mono.just(new ArrayList<>());
    }

    private Mono<Double> getAverageSessionRating(Long tutorId) {
        // Placeholder - would integrate with evaluation system
        return Mono.just(4.5); // Mock rating
    }

    private Mono<Map<SessionStatus, Long>> getSessionCountByStatus(Long tutorId, Long studentId) {
        Flux<Session> sessions;
        
        if (tutorId != null) {
            sessions = sessionRepository.findByTuteurId(tutorId);
        } else if (studentId != null) {
            sessions = sessionRepository.findByEtudiantId(studentId);
        } else {
            sessions = sessionRepository.findAll();
        }
        
        return sessions.collectList()
                .map(sessionList -> {
                    Map<SessionStatus, Long> counts = new HashMap<>();
                    sessionList.forEach(session -> 
                        counts.merge(session.getStatut(), 1L, Long::sum));
                    return counts;
                });
    }

    private Mono<Long> getThisMonthSessionCount(Long tutorId, Long studentId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        return sessionRepository.findByDateHeureBetween(monthStart, now)
                .filter(session -> {
                    if (tutorId != null) return session.getTuteurId().equals(tutorId);
                    if (studentId != null) return session.getEtudiantId().equals(studentId);
                    return true;
                })
                .count();
    }

    private Mono<Long> getActiveStudentCount(Long tutorId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        return sessionRepository.findByTuteurId(tutorId)
                .filter(session -> session.getDateHeure().isAfter(thirtyDaysAgo))
                .map(Session::getEtudiantId)
                .distinct()
                .count();
    }

    private Mono<Long> getActiveTutorCount(Long studentId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        return sessionRepository.findByEtudiantId(studentId)
                .filter(session -> session.getDateHeure().isAfter(thirtyDaysAgo))
                .map(Session::getTuteurId)
                .distinct()
                .count();
    }

    private Mono<Double> getTotalSpent(Long studentId) {
        return sessionRepository.findByEtudiantId(studentId)
                .filter(session -> session.getStatut() == SessionStatus.TERMINEE)
                .map(Session::getPrix)
                .reduce(0.0, Double::sum);
    }

    private Mono<List<String>> getFavoriteSubjects(Long studentId) {
        // Placeholder - would query subjects by frequency
        return Mono.just(List.of("Mathématiques", "Physique", "Chimie"));
    }

    private Mono<Double> getSessionCompletionRate(Long tutorId, Long studentId) {
        // Calculate percentage of sessions completed vs total scheduled
        return Mono.just(85.5); // Mock value
    }

    private Mono<Double> getOnTimeRate(Long tutorId, Long studentId) {
        // Calculate percentage of sessions started on time
        return Mono.just(92.3); // Mock value
    }

    private Mono<Double> getSessionCancellationRate(Long tutorId, Long studentId) {
        // Calculate percentage of sessions cancelled
        return Mono.just(7.2); // Mock value
    }

    private Mono<Double> getResponseTime(Long tutorId) {
        // Average response time to session requests in hours
        return Mono.just(2.5); // Mock value
    }

    private Mono<Double> getStudentRetentionRate(Long tutorId) {
        // Percentage of students who book multiple sessions
        return Mono.just(68.4); // Mock value
    }

    private Mono<Double> getTotalLearningHours(Long studentId) {
        return sessionRepository.findByEtudiantId(studentId)
                .filter(session -> session.getStatut() == SessionStatus.TERMINEE)
                .map(session -> session.getDuree() / 60.0)
                .reduce(0.0, Double::sum);
    }

    private Mono<Double> getAverageSessionDuration(Long studentId) {
        return sessionRepository.findByEtudiantId(studentId)
                .filter(session -> session.getStatut() == SessionStatus.TERMINEE)
                .map(Session::getDuree)
                .collectList()
                .map(durations -> durations.isEmpty() ? 0.0 : 
                        durations.stream().mapToInt(Integer::intValue).average().orElse(0.0));
    }

    private Mono<Integer> getSubjectDiversity(Long studentId) {
        return sessionRepository.findByEtudiantId(studentId)
                .map(Session::getMatiereId)
                .distinct()
                .count()
                .map(Long::intValue);
    }

    private Mono<Double> getConsistencyScore(Long studentId) {
        // Mock calculation of how consistently the student books sessions
        return Mono.just(78.5);
    }

    private Mono<Double> getProgressScore(Long studentId) {
        // Mock calculation of learning progress based on session frequency and ratings
        return Mono.just(82.3);
    }

    // Platform-wide utility methods
    private Mono<Long> getTotalSessionsCount() {
        return sessionRepository.count();
    }

    private Mono<Long> getActiveUsersCount() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return sessionRepository.findByDateHeureBetween(thirtyDaysAgo, LocalDateTime.now())
                .flatMap(session -> Flux.just(session.getTuteurId(), session.getEtudiantId()))
                .distinct()
                .count();
    }

    private Mono<Long> getMonthlySessionsCount(LocalDateTime start, LocalDateTime end) {
        return sessionRepository.findByDateHeureBetween(start, end).count();
    }

    private Mono<Double> getTotalRevenueThisMonth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        return sessionRepository.findByDateHeureBetween(monthStart, now)
                .filter(session -> session.getStatut() == SessionStatus.TERMINEE)
                .map(Session::getPrix)
                .reduce(0.0, Double::sum);
    }

    private Mono<Double> getAveragePlatformRating() {
        return Mono.just(4.3); // Mock platform-wide rating
    }

    private Mono<Map<String, Object>> getRevenueData() {
        return Mono.just(Map.of("totalRevenue", 15000.0, "growth", 12.5));
    }

    private Mono<Map<String, Object>> getUserActivityData() {
        return Mono.just(Map.of("dailyActiveUsers", 450, "weeklyActiveUsers", 1200));
    }

    private Mono<Map<String, Object>> getPopularSubjectsData() {
        return Mono.just(Map.of("topSubjects", List.of("Mathématiques", "Physique", "Chimie")));
    }

    // ===============================================
    // INNER CLASSES FOR DATA TRANSFER
    // ===============================================

    public static class TutorDashboard {
        private TutorStatistics statistics;
        private List<Session> upcomingSessions;
        private List<Session> recentSessions;
        private PerformanceMetrics performanceMetrics;
        private EarningsData earningsData;

        // Getters and setters
        public TutorStatistics getStatistics() { return statistics; }
        public void setStatistics(TutorStatistics statistics) { this.statistics = statistics; }
        public List<Session> getUpcomingSessions() { return upcomingSessions; }
        public void setUpcomingSessions(List<Session> upcomingSessions) { this.upcomingSessions = upcomingSessions; }
        public List<Session> getRecentSessions() { return recentSessions; }
        public void setRecentSessions(List<Session> recentSessions) { this.recentSessions = recentSessions; }
        public PerformanceMetrics getPerformanceMetrics() { return performanceMetrics; }
        public void setPerformanceMetrics(PerformanceMetrics performanceMetrics) { this.performanceMetrics = performanceMetrics; }
        public EarningsData getEarningsData() { return earningsData; }
        public void setEarningsData(EarningsData earningsData) { this.earningsData = earningsData; }
    }

    public static class StudentDashboard {
        private StudentStatistics statistics;
        private List<Session> upcomingSessions;
        private List<Session> recentSessions;
        private LearningMetrics learningMetrics;
        private SpendingData spendingData;

        // Getters and setters
        public StudentStatistics getStatistics() { return statistics; }
        public void setStatistics(StudentStatistics statistics) { this.statistics = statistics; }
        public List<Session> getUpcomingSessions() { return upcomingSessions; }
        public void setUpcomingSessions(List<Session> upcomingSessions) { this.upcomingSessions = upcomingSessions; }
        public List<Session> getRecentSessions() { return recentSessions; }
        public void setRecentSessions(List<Session> recentSessions) { this.recentSessions = recentSessions; }
        public LearningMetrics getLearningMetrics() { return learningMetrics; }
        public void setLearningMetrics(LearningMetrics learningMetrics) { this.learningMetrics = learningMetrics; }
        public SpendingData getSpendingData() { return spendingData; }
        public void setSpendingData(SpendingData spendingData) { this.spendingData = spendingData; }
    }

    public static class AdminDashboard {
        private PlatformStatistics platformStatistics;
        private SessionVolumeData sessionVolumeData;
        private Map<String, Object> revenueData;
        private Map<String, Object> userActivityData;
        private Map<String, Object> popularSubjectsData;

        // Getters and setters
        public PlatformStatistics getPlatformStatistics() { return platformStatistics; }
        public void setPlatformStatistics(PlatformStatistics platformStatistics) { this.platformStatistics = platformStatistics; }
        public SessionVolumeData getSessionVolumeData() { return sessionVolumeData; }
        public void setSessionVolumeData(SessionVolumeData sessionVolumeData) { this.sessionVolumeData = sessionVolumeData; }
        public Map<String, Object> getRevenueData() { return revenueData; }
        public void setRevenueData(Map<String, Object> revenueData) { this.revenueData = revenueData; }
        public Map<String, Object> getUserActivityData() { return userActivityData; }
        public void setUserActivityData(Map<String, Object> userActivityData) { this.userActivityData = userActivityData; }
        public Map<String, Object> getPopularSubjectsData() { return popularSubjectsData; }
        public void setPopularSubjectsData(Map<String, Object> popularSubjectsData) { this.popularSubjectsData = popularSubjectsData; }
    }

    public static class TutorStatistics {
        private Long totalCompletedSessions;
        private Double totalEarnings;
        private Double averageRating;
        private Map<SessionStatus, Long> sessionsByStatus;
        private Long thisMonthSessions;
        private Long activeStudents;

        // Getters and setters
        public Long getTotalCompletedSessions() { return totalCompletedSessions; }
        public void setTotalCompletedSessions(Long totalCompletedSessions) { this.totalCompletedSessions = totalCompletedSessions; }
        public Double getTotalEarnings() { return totalEarnings; }
        public void setTotalEarnings(Double totalEarnings) { this.totalEarnings = totalEarnings; }
        public Double getAverageRating() { return averageRating; }
        public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
        public Map<SessionStatus, Long> getSessionsByStatus() { return sessionsByStatus; }
        public void setSessionsByStatus(Map<SessionStatus, Long> sessionsByStatus) { this.sessionsByStatus = sessionsByStatus; }
        public Long getThisMonthSessions() { return thisMonthSessions; }
        public void setThisMonthSessions(Long thisMonthSessions) { this.thisMonthSessions = thisMonthSessions; }
        public Long getActiveStudents() { return activeStudents; }
        public void setActiveStudents(Long activeStudents) { this.activeStudents = activeStudents; }
    }

    public static class StudentStatistics {
        private Long totalCompletedSessions;
        private Double totalSpent;
        private Map<SessionStatus, Long> sessionsByStatus;
        private Long thisMonthSessions;
        private Long activeTutors;
        private List<String> favoriteSubjects;

        // Getters and setters
        public Long getTotalCompletedSessions() { return totalCompletedSessions; }
        public void setTotalCompletedSessions(Long totalCompletedSessions) { this.totalCompletedSessions = totalCompletedSessions; }
        public Double getTotalSpent() { return totalSpent; }
        public void setTotalSpent(Double totalSpent) { this.totalSpent = totalSpent; }
        public Map<SessionStatus, Long> getSessionsByStatus() { return sessionsByStatus; }
        public void setSessionsByStatus(Map<SessionStatus, Long> sessionsByStatus) { this.sessionsByStatus = sessionsByStatus; }
        public Long getThisMonthSessions() { return thisMonthSessions; }
        public void setThisMonthSessions(Long thisMonthSessions) { this.thisMonthSessions = thisMonthSessions; }
        public Long getActiveTutors() { return activeTutors; }
        public void setActiveTutors(Long activeTutors) { this.activeTutors = activeTutors; }
        public List<String> getFavoriteSubjects() { return favoriteSubjects; }
        public void setFavoriteSubjects(List<String> favoriteSubjects) { this.favoriteSubjects = favoriteSubjects; }
    }

    public static class PlatformStatistics {
        private Long totalSessions;
        private Long activeUsers;
        private Long thisMonthSessions;
        private Long lastMonthSessions;
        private Double monthlyRevenue;
        private Double averagePlatformRating;
        private Double growthRate;

        // Getters and setters
        public Long getTotalSessions() { return totalSessions; }
        public void setTotalSessions(Long totalSessions) { this.totalSessions = totalSessions; }
        public Long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(Long activeUsers) { this.activeUsers = activeUsers; }
        public Long getThisMonthSessions() { return thisMonthSessions; }
        public void setThisMonthSessions(Long thisMonthSessions) { this.thisMonthSessions = thisMonthSessions; }
        public Long getLastMonthSessions() { return lastMonthSessions; }
        public void setLastMonthSessions(Long lastMonthSessions) { this.lastMonthSessions = lastMonthSessions; }
        public Double getMonthlyRevenue() { return monthlyRevenue; }
        public void setMonthlyRevenue(Double monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }
        public Double getAveragePlatformRating() { return averagePlatformRating; }
        public void setAveragePlatformRating(Double averagePlatformRating) { this.averagePlatformRating = averagePlatformRating; }
        public Double getGrowthRate() { return growthRate; }
        public void setGrowthRate(Double growthRate) { this.growthRate = growthRate; }
    }

    public static class PerformanceMetrics {
        private Double completionRate;
        private Double onTimeRate;
        private Double cancellationRate;
        private Double averageResponseTime;
        private Double retentionRate;

        // Getters and setters
        public Double getCompletionRate() { return completionRate; }
        public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
        public Double getOnTimeRate() { return onTimeRate; }
        public void setOnTimeRate(Double onTimeRate) { this.onTimeRate = onTimeRate; }
        public Double getCancellationRate() { return cancellationRate; }
        public void setCancellationRate(Double cancellationRate) { this.cancellationRate = cancellationRate; }
        public Double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(Double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        public Double getRetentionRate() { return retentionRate; }
        public void setRetentionRate(Double retentionRate) { this.retentionRate = retentionRate; }
    }

    public static class LearningMetrics {
        private Double totalHours;
        private Double averageSessionDuration;
        private Integer subjectDiversity;
        private Double consistencyScore;
        private Double progressScore;

        // Getters and setters
        public Double getTotalHours() { return totalHours; }
        public void setTotalHours(Double totalHours) { this.totalHours = totalHours; }
        public Double getAverageSessionDuration() { return averageSessionDuration; }
        public void setAverageSessionDuration(Double averageSessionDuration) { this.averageSessionDuration = averageSessionDuration; }
        public Integer getSubjectDiversity() { return subjectDiversity; }
        public void setSubjectDiversity(Integer subjectDiversity) { this.subjectDiversity = subjectDiversity; }
        public Double getConsistencyScore() { return consistencyScore; }
        public void setConsistencyScore(Double consistencyScore) { this.consistencyScore = consistencyScore; }
        public Double getProgressScore() { return progressScore; }
        public void setProgressScore(Double progressScore) { this.progressScore = progressScore; }
    }

    public static class EarningsData {
        private Map<String, Double> monthlyEarnings;
        private Double totalEarnings;
        private Double averagePerSession;

        public EarningsData() {
            this.monthlyEarnings = new HashMap<>();
        }

        // Getters and setters
        public Map<String, Double> getMonthlyEarnings() { return monthlyEarnings; }
        public void setMonthlyEarnings(Map<String, Double> monthlyEarnings) { this.monthlyEarnings = monthlyEarnings; }
        public Double getTotalEarnings() { return totalEarnings; }
        public void setTotalEarnings(Double totalEarnings) { this.totalEarnings = totalEarnings; }
        public Double getAveragePerSession() { return averagePerSession; }
        public void setAveragePerSession(Double averagePerSession) { this.averagePerSession = averagePerSession; }
    }

    public static class SpendingData {
        private Map<String, Double> monthlySpending;
        private Double totalSpent;
        private Double averagePerSession;

        public SpendingData() {
            this.monthlySpending = new HashMap<>();
        }

        // Getters and setters
        public Map<String, Double> getMonthlySpending() { return monthlySpending; }
        public void setMonthlySpending(Map<String, Double> monthlySpending) { this.monthlySpending = monthlySpending; }
        public Double getTotalSpent() { return totalSpent; }
        public void setTotalSpent(Double totalSpent) { this.totalSpent = totalSpent; }
        public Double getAveragePerSession() { return averagePerSession; }
        public void setAveragePerSession(Double averagePerSession) { this.averagePerSession = averagePerSession; }
    }

    public static class SessionVolumeData {
        private Map<String, Integer> monthlyVolume;
        private Integer totalSessions;
        private Double averagePerMonth;

        public SessionVolumeData() {
            this.monthlyVolume = new HashMap<>();
        }

        // Getters and setters
        public Map<String, Integer> getMonthlyVolume() { return monthlyVolume; }
        public void setMonthlyVolume(Map<String, Integer> monthlyVolume) { this.monthlyVolume = monthlyVolume; }
        public Integer getTotalSessions() { return totalSessions; }
        public void setTotalSessions(Integer totalSessions) { this.totalSessions = totalSessions; }
        public Double getAveragePerMonth() { return averagePerMonth; }
        public void setAveragePerMonth(Double averagePerMonth) { this.averagePerMonth = averagePerMonth; }
    }
}