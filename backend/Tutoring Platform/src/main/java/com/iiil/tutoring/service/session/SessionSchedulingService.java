package com.iiil.tutoring.service.session;

import com.iiil.tutoring.entity.Session;
import com.iiil.tutoring.enums.SessionStatus;
import com.iiil.tutoring.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Service for session scheduling operations including availability checks,
 * conflict detection, and automated slot management
 */
@Service
public class SessionSchedulingService {

    private final SessionRepository sessionRepository;

    @Autowired
    public SessionSchedulingService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    // ===============================================
    // AVAILABILITY CHECKING
    // ===============================================

    /**
     * Check if a tutor is available at a specific time
     */
    public Mono<Boolean> isTutorAvailable(Long tutorId, LocalDateTime startTime, int durationMinutes) {
        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);
        
        return sessionRepository.findByTuteurIdAndDateRange(tutorId, startTime, endTime)
                .filter(session -> isActiveSession(session))
                .hasElements()
                .map(hasConflicts -> !hasConflicts);
    }

    /**
     * Check if a student is available at a specific time
     */
    public Mono<Boolean> isStudentAvailable(Long studentId, LocalDateTime startTime, int durationMinutes) {
        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);
        
        return sessionRepository.findByEtudiantIdAndDateRange(studentId, startTime, endTime)
                .filter(session -> isActiveSession(session))
                .hasElements()
                .map(hasConflicts -> !hasConflicts);
    }

    /**
     * Check if both tutor and student are available
     */
    public Mono<Boolean> areBothParticipantsAvailable(Long tutorId, Long studentId, 
                                                     LocalDateTime startTime, int durationMinutes) {
        return Mono.zip(
                isTutorAvailable(tutorId, startTime, durationMinutes),
                isStudentAvailable(studentId, startTime, durationMinutes)
        ).map(tuple -> tuple.getT1() && tuple.getT2());
    }

    /**
     * Find scheduling conflicts for a session
     */
    public Mono<List<SchedulingConflict>> findSchedulingConflicts(Long tutorId, Long studentId, 
                                                                 LocalDateTime startTime, int durationMinutes) {
        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);
        
        Mono<List<SchedulingConflict>> tutorConflicts = sessionRepository
                .findByTuteurIdAndDateRange(tutorId, startTime, endTime)
                .filter(this::isActiveSession)
                .map(session -> new SchedulingConflict(
                        ConflictType.TUTOR_BUSY,
                        "Tutor has another session",
                        session.getDateHeure(),
                        session.getDateHeure().plusMinutes(session.getDuree()),
                        session.getId()
                ))
                .collectList();

        Mono<List<SchedulingConflict>> studentConflicts = sessionRepository
                .findByEtudiantIdAndDateRange(studentId, startTime, endTime)
                .filter(this::isActiveSession)
                .map(session -> new SchedulingConflict(
                        ConflictType.STUDENT_BUSY,
                        "Student has another session",
                        session.getDateHeure(),
                        session.getDateHeure().plusMinutes(session.getDuree()),
                        session.getId()
                ))
                .collectList();

        return Mono.zip(tutorConflicts, studentConflicts)
                .map(tuple -> {
                    List<SchedulingConflict> allConflicts = tuple.getT1();
                    allConflicts.addAll(tuple.getT2());
                    return allConflicts;
                });
    }

    // ===============================================
    // AVAILABLE SLOT FINDING
    // ===============================================

    /**
     * Find available time slots for a tutor within a date range
     */
    public Flux<AvailableSlot> findAvailableSlots(Long tutorId, LocalDateTime startDate, 
                                                 LocalDateTime endDate, int durationMinutes) {
        return findAvailableSlotsWithConstraints(tutorId, null, startDate, endDate, durationMinutes, null);
    }

    /**
     * Find available time slots for both tutor and student
     */
    public Flux<AvailableSlot> findMutuallyAvailableSlots(Long tutorId, Long studentId, 
                                                         LocalDateTime startDate, LocalDateTime endDate, 
                                                         int durationMinutes) {
        return findAvailableSlotsWithConstraints(tutorId, studentId, startDate, endDate, durationMinutes, null);
    }

    /**
     * Find available slots with specific time constraints
     */
    public Flux<AvailableSlot> findAvailableSlotsWithConstraints(Long tutorId, Long studentId,
                                                               LocalDateTime startDate, LocalDateTime endDate,
                                                               int durationMinutes, TimeConstraints constraints) {
        // Implementation would iterate through time periods and check availability
        return Flux.empty(); // Placeholder - would implement complex scheduling logic
    }

    /**
     * Get next available slot for a tutor
     */
    public Mono<AvailableSlot> getNextAvailableSlot(Long tutorId, int durationMinutes) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endSearch = now.plusDays(30); // Search 30 days ahead
        
        return findAvailableSlots(tutorId, now, endSearch, durationMinutes)
                .next(); // Get the first available slot
    }

    /**
     * Get suggested time slots based on preferences
     */
    public Flux<AvailableSlot> getSuggestedSlots(Long tutorId, Long studentId, 
                                               SessionPreferences preferences) {
        LocalDateTime searchStart = preferences.getPreferredStartDate() != null 
                ? preferences.getPreferredStartDate() 
                : LocalDateTime.now().plusHours(2);
        
        LocalDateTime searchEnd = searchStart.plusDays(preferences.getSearchDaysAhead());
        
        return findMutuallyAvailableSlots(tutorId, studentId, searchStart, searchEnd, 
                                        preferences.getDurationMinutes())
                .filter(slot -> matchesPreferences(slot, preferences))
                .take(preferences.getMaxSuggestions());
    }

    // ===============================================
    // SCHEDULING VALIDATION
    // ===============================================

    /**
     * Validate a session scheduling request
     */
    public Mono<SchedulingValidationResult> validateScheduling(Long tutorId, Long studentId,
                                                             LocalDateTime startTime, int durationMinutes) {
        return findSchedulingConflicts(tutorId, studentId, startTime, durationMinutes)
                .map(conflicts -> {
                    SchedulingValidationResult result = new SchedulingValidationResult();
                    result.setValid(conflicts.isEmpty());
                    result.setConflicts(conflicts);
                    
                    if (!conflicts.isEmpty()) {
                        result.addError("Scheduling conflicts found");
                    }
                    
                    // Add business rule validations
                    if (startTime.isBefore(LocalDateTime.now().plusHours(1))) {
                        result.addError("Sessions must be scheduled at least 1 hour in advance");
                        result.setValid(false);
                    }
                    
                    if (durationMinutes < 30) {
                        result.addError("Session duration must be at least 30 minutes");
                        result.setValid(false);
                    }
                    
                    if (durationMinutes > 240) {
                        result.addError("Session duration cannot exceed 4 hours");
                        result.setValid(false);
                    }
                    
                    return result;
                });
    }

    /**
     * Check if a session can be rescheduled
     */
    public Mono<Boolean> canReschedule(Long sessionId, LocalDateTime newStartTime) {
        return sessionRepository.findById(sessionId)
                .flatMap(session -> {
                    // Check if session is in a state that allows rescheduling
                    if (session.getStatut() == SessionStatus.TERMINEE || 
                        session.getStatut() == SessionStatus.ANNULEE) {
                        return Mono.just(false);
                    }
                    
                    // Check if new time is valid
                    if (newStartTime.isBefore(LocalDateTime.now().plusHours(1))) {
                        return Mono.just(false);
                    }
                    
                    // Check for conflicts at new time
                    return areBothParticipantsAvailable(session.getTuteurId(), session.getEtudiantId(),
                                                      newStartTime, session.getDuree());
                })
                .defaultIfEmpty(false);
    }

    // ===============================================
    // BULK OPERATIONS
    // ===============================================

    /**
     * Check availability for multiple sessions
     */
    public Flux<BulkAvailabilityResult> checkBulkAvailability(List<SchedulingRequest> requests) {
        return Flux.fromIterable(requests)
                .flatMap(request -> 
                    areBothParticipantsAvailable(request.getTutorId(), request.getStudentId(),
                                               request.getStartTime(), request.getDurationMinutes())
                    .map(available -> new BulkAvailabilityResult(request, available))
                );
    }

    /**
     * Find alternative slots for conflicted sessions
     */
    public Flux<AlternativeSlot> findAlternativeSlots(Long sessionId, int numberOfAlternatives) {
        return sessionRepository.findById(sessionId)
                .flatMapMany(session -> {
                    LocalDateTime originalTime = session.getDateHeure();
                    LocalDateTime searchStart = originalTime.minusDays(3);
                    LocalDateTime searchEnd = originalTime.plusDays(7);
                    
                    return findMutuallyAvailableSlots(session.getTuteurId(), session.getEtudiantId(),
                                                    searchStart, searchEnd, session.getDuree())
                            .filter(slot -> !slot.getStartTime().equals(originalTime))
                            .map(slot -> new AlternativeSlot(sessionId, slot))
                            .take(numberOfAlternatives);
                });
    }

    // ===============================================
    // UTILITY METHODS
    // ===============================================

    private boolean isActiveSession(Session session) {
        return session.getStatut() == SessionStatus.DEMANDEE ||
               session.getStatut() == SessionStatus.CONFIRMEE ||
               session.getStatut() == SessionStatus.EN_COURS;
    }

    private boolean matchesPreferences(AvailableSlot slot, SessionPreferences preferences) {
        // Check time of day preferences
        if (preferences.getPreferredTimeOfDay() != null) {
            LocalTime slotTime = slot.getStartTime().toLocalTime();
            switch (preferences.getPreferredTimeOfDay()) {
                case MORNING:
                    return slotTime.isBefore(LocalTime.of(12, 0));
                case AFTERNOON:
                    return slotTime.isAfter(LocalTime.of(12, 0)) && slotTime.isBefore(LocalTime.of(17, 0));
                case EVENING:
                    return slotTime.isAfter(LocalTime.of(17, 0));
            }
        }
        
        // Check day of week preferences
        if (preferences.getPreferredDaysOfWeek() != null && !preferences.getPreferredDaysOfWeek().isEmpty()) {
            DayOfWeek slotDay = slot.getStartTime().getDayOfWeek();
            return preferences.getPreferredDaysOfWeek().contains(slotDay);
        }
        
        return true;
    }

    // ===============================================
    // INNER CLASSES FOR DATA TRANSFER
    // ===============================================

    public static class SchedulingConflict {
        private ConflictType type;
        private String description;
        private LocalDateTime conflictStart;
        private LocalDateTime conflictEnd;
        private Long conflictingSessionId;

        public SchedulingConflict(ConflictType type, String description, 
                                LocalDateTime conflictStart, LocalDateTime conflictEnd, 
                                Long conflictingSessionId) {
            this.type = type;
            this.description = description;
            this.conflictStart = conflictStart;
            this.conflictEnd = conflictEnd;
            this.conflictingSessionId = conflictingSessionId;
        }

        // Getters and setters
        public ConflictType getType() { return type; }
        public void setType(ConflictType type) { this.type = type; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public LocalDateTime getConflictStart() { return conflictStart; }
        public void setConflictStart(LocalDateTime conflictStart) { this.conflictStart = conflictStart; }
        public LocalDateTime getConflictEnd() { return conflictEnd; }
        public void setConflictEnd(LocalDateTime conflictEnd) { this.conflictEnd = conflictEnd; }
        public Long getConflictingSessionId() { return conflictingSessionId; }
        public void setConflictingSessionId(Long conflictingSessionId) { this.conflictingSessionId = conflictingSessionId; }
    }

    public enum ConflictType {
        TUTOR_BUSY, STUDENT_BUSY, SYSTEM_MAINTENANCE, HOLIDAY
    }

    public static class AvailableSlot {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private int durationMinutes;
        private double confidenceScore;

        public AvailableSlot(LocalDateTime startTime, int durationMinutes) {
            this.startTime = startTime;
            this.endTime = startTime.plusMinutes(durationMinutes);
            this.durationMinutes = durationMinutes;
            this.confidenceScore = 1.0;
        }

        // Getters and setters
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public int getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
        public double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    }

    public static class SchedulingValidationResult {
        private boolean valid;
        private List<SchedulingConflict> conflicts;
        private List<String> errors;

        public SchedulingValidationResult() {
            this.conflicts = new java.util.ArrayList<>();
            this.errors = new java.util.ArrayList<>();
        }

        public void addError(String error) {
            this.errors.add(error);
        }

        // Getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public List<SchedulingConflict> getConflicts() { return conflicts; }
        public void setConflicts(List<SchedulingConflict> conflicts) { this.conflicts = conflicts; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
    }

    public static class TimeConstraints {
        private LocalTime earliestTime;
        private LocalTime latestTime;
        private List<DayOfWeek> allowedDays;
        private int minimumAdvanceHours;

        // Getters and setters
        public LocalTime getEarliestTime() { return earliestTime; }
        public void setEarliestTime(LocalTime earliestTime) { this.earliestTime = earliestTime; }
        public LocalTime getLatestTime() { return latestTime; }
        public void setLatestTime(LocalTime latestTime) { this.latestTime = latestTime; }
        public List<DayOfWeek> getAllowedDays() { return allowedDays; }
        public void setAllowedDays(List<DayOfWeek> allowedDays) { this.allowedDays = allowedDays; }
        public int getMinimumAdvanceHours() { return minimumAdvanceHours; }
        public void setMinimumAdvanceHours(int minimumAdvanceHours) { this.minimumAdvanceHours = minimumAdvanceHours; }
    }

    public static class SessionPreferences {
        private LocalDateTime preferredStartDate;
        private int durationMinutes;
        private TimeOfDay preferredTimeOfDay;
        private List<DayOfWeek> preferredDaysOfWeek;
        private int searchDaysAhead = 14;
        private int maxSuggestions = 5;

        // Getters and setters
        public LocalDateTime getPreferredStartDate() { return preferredStartDate; }
        public void setPreferredStartDate(LocalDateTime preferredStartDate) { this.preferredStartDate = preferredStartDate; }
        public int getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
        public TimeOfDay getPreferredTimeOfDay() { return preferredTimeOfDay; }
        public void setPreferredTimeOfDay(TimeOfDay preferredTimeOfDay) { this.preferredTimeOfDay = preferredTimeOfDay; }
        public List<DayOfWeek> getPreferredDaysOfWeek() { return preferredDaysOfWeek; }
        public void setPreferredDaysOfWeek(List<DayOfWeek> preferredDaysOfWeek) { this.preferredDaysOfWeek = preferredDaysOfWeek; }
        public int getSearchDaysAhead() { return searchDaysAhead; }
        public void setSearchDaysAhead(int searchDaysAhead) { this.searchDaysAhead = searchDaysAhead; }
        public int getMaxSuggestions() { return maxSuggestions; }
        public void setMaxSuggestions(int maxSuggestions) { this.maxSuggestions = maxSuggestions; }
    }

    public enum TimeOfDay {
        MORNING, AFTERNOON, EVENING
    }

    public static class SchedulingRequest {
        private Long tutorId;
        private Long studentId;
        private LocalDateTime startTime;
        private int durationMinutes;

        // Getters and setters
        public Long getTutorId() { return tutorId; }
        public void setTutorId(Long tutorId) { this.tutorId = tutorId; }
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public int getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    }

    public static class BulkAvailabilityResult {
        private SchedulingRequest request;
        private boolean available;
        private String reason;

        public BulkAvailabilityResult(SchedulingRequest request, boolean available) {
            this.request = request;
            this.available = available;
        }

        // Getters and setters
        public SchedulingRequest getRequest() { return request; }
        public void setRequest(SchedulingRequest request) { this.request = request; }
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class AlternativeSlot {
        private Long originalSessionId;
        private AvailableSlot alternativeSlot;
        private String reason;

        public AlternativeSlot(Long originalSessionId, AvailableSlot alternativeSlot) {
            this.originalSessionId = originalSessionId;
            this.alternativeSlot = alternativeSlot;
        }

        // Getters and setters
        public Long getOriginalSessionId() { return originalSessionId; }
        public void setOriginalSessionId(Long originalSessionId) { this.originalSessionId = originalSessionId; }
        public AvailableSlot getAlternativeSlot() { return alternativeSlot; }
        public void setAlternativeSlot(AvailableSlot alternativeSlot) { this.alternativeSlot = alternativeSlot; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}