package com.iiil.tutoring.service.session;

import com.iiil.tutoring.entity.Notification;
import com.iiil.tutoring.entity.Session;
import com.iiil.tutoring.enums.NotificationType;
import com.iiil.tutoring.enums.SessionStatus;
import com.iiil.tutoring.repository.NotificationRepository;
import com.iiil.tutoring.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing session-related notifications including automated reminders,
 * confirmations, and status updates
 */
@Service
public class SessionNotificationService {

    private final NotificationRepository notificationRepository;
    private final SessionRepository sessionRepository;

    @Autowired
    public SessionNotificationService(NotificationRepository notificationRepository,
                                    SessionRepository sessionRepository) {
        this.notificationRepository = notificationRepository;
        this.sessionRepository = sessionRepository;
    }

    // ===============================================
    // SESSION NOTIFICATION CREATION
    // ===============================================

    /**
     * Send session confirmation notification
     */
    @Async
    public CompletableFuture<Void> sendSessionConfirmationNotification(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .flatMap(session -> {
                    // Notify student
                    Notification studentNotification = createSessionNotification(
                            session.getEtudiantId(),
                            "Session confirmée",
                            buildSessionConfirmationMessage(session, false),
                            NotificationType.SESSION,
                            sessionId
                    );

                    // Notify tutor
                    Notification tutorNotification = createSessionNotification(
                            session.getTuteurId(),
                            "Session confirmée",
                            buildSessionConfirmationMessage(session, true),
                            NotificationType.SESSION,
                            sessionId
                    );

                    return Mono.when(
                            notificationRepository.save(studentNotification),
                            notificationRepository.save(tutorNotification)
                    );
                })
                .toFuture()
                .thenRun(() -> {});
    }

    /**
     * Send session reminder notification
     */
    @Async
    public CompletableFuture<Void> sendSessionReminderNotification(Long sessionId, int minutesBeforeSession) {
        return sessionRepository.findById(sessionId)
                .flatMap(session -> {
                    String reminderMessage = buildSessionReminderMessage(session, minutesBeforeSession);
                    
                    // Notify student
                    Notification studentNotification = createSessionNotification(
                            session.getEtudiantId(),
                            "Rappel de session",
                            reminderMessage,
                            NotificationType.SESSION,
                            sessionId
                    );

                    // Notify tutor
                    Notification tutorNotification = createSessionNotification(
                            session.getTuteurId(),
                            "Rappel de session",
                            reminderMessage,
                            NotificationType.SESSION,
                            sessionId
                    );

                    return Mono.when(
                            notificationRepository.save(studentNotification),
                            notificationRepository.save(tutorNotification)
                    );
                })
                .toFuture()
                .thenRun(() -> {});
    }

    /**
     * Send session cancellation notification
     */
    @Async
    public CompletableFuture<Void> sendSessionCancellationNotification(Long sessionId, String reason, Long cancelledBy) {
        return sessionRepository.findById(sessionId)
                .flatMap(session -> {
                    String cancellationMessage = buildSessionCancellationMessage(session, reason, cancelledBy);
                    
                    // Notify the other participant (not the one who cancelled)
                    Long recipientId = cancelledBy.equals(session.getTuteurId()) 
                            ? session.getEtudiantId() 
                            : session.getTuteurId();

                    Notification notification = createSessionNotification(
                            recipientId,
                            "Session annulée",
                            cancellationMessage,
                            NotificationType.SESSION,
                            sessionId
                    );

                    return notificationRepository.save(notification);
                })
                .toFuture()
                .thenRun(() -> {});
    }

    /**
     * Send session rescheduling notification
     */
    @Async
    public CompletableFuture<Void> sendSessionRescheduleNotification(Long sessionId, LocalDateTime newDateTime, 
                                                                    String reason, Long rescheduledBy) {
        return sessionRepository.findById(sessionId)
                .flatMap(session -> {
                    String rescheduleMessage = buildSessionRescheduleMessage(session, newDateTime, reason, rescheduledBy);
                    
                    // Notify the other participant
                    Long recipientId = rescheduledBy.equals(session.getTuteurId()) 
                            ? session.getEtudiantId() 
                            : session.getTuteurId();

                    Notification notification = createSessionNotification(
                            recipientId,
                            "Session reprogrammée",
                            rescheduleMessage,
                            NotificationType.SESSION,
                            sessionId
                    );

                    return notificationRepository.save(notification);
                })
                .toFuture()
                .thenRun(() -> {});
    }

    /**
     * Send session completion notification
     */
    @Async
    public CompletableFuture<Void> sendSessionCompletionNotification(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .flatMap(session -> {
                    String completionMessage = buildSessionCompletionMessage(session);
                    
                    // Notify both participants
                    Notification studentNotification = createSessionNotification(
                            session.getEtudiantId(),
                            "Session terminée",
                            completionMessage + " N'oubliez pas d'évaluer votre tuteur !",
                            NotificationType.SESSION,
                            sessionId
                    );

                    Notification tutorNotification = createSessionNotification(
                            session.getTuteurId(),
                            "Session terminée",
                            completionMessage + " N'oubliez pas d'évaluer votre étudiant !",
                            NotificationType.SESSION,
                            sessionId
                    );

                    return Mono.when(
                            notificationRepository.save(studentNotification),
                            notificationRepository.save(tutorNotification)
                    );
                })
                .toFuture()
                .thenRun(() -> {});
    }

    /**
     * Send session start notification
     */
    @Async
    public CompletableFuture<Void> sendSessionStartNotification(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .flatMap(session -> {
                    String startMessage = buildSessionStartMessage(session);
                    
                    // Notify both participants
                    Notification studentNotification = createSessionNotification(
                            session.getEtudiantId(),
                            "Session commencée",
                            startMessage,
                            NotificationType.SESSION,
                            sessionId
                    );

                    Notification tutorNotification = createSessionNotification(
                            session.getTuteurId(),
                            "Session commencée",
                            startMessage,
                            NotificationType.SESSION,
                            sessionId
                    );

                    return Mono.when(
                            notificationRepository.save(studentNotification),
                            notificationRepository.save(tutorNotification)
                    );
                })
                .toFuture()
                .thenRun(() -> {});
    }

    // ===============================================
    // AUTOMATED SCHEDULED NOTIFICATIONS
    // ===============================================

    /**
     * Send 24-hour reminders for upcoming sessions
     */
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void sendDailyReminders() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        LocalDateTime dayAfter = tomorrow.plusHours(1);

        sessionRepository.findByDateHeureBetween(tomorrow, dayAfter)
                .filter(session -> session.getStatut() == SessionStatus.CONFIRMEE)
                .subscribe(session -> 
                    sendSessionReminderNotification(session.getId(), 24 * 60));
    }

    /**
     * Send 1-hour reminders for upcoming sessions
     */
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void sendHourlyReminders() {
        LocalDateTime oneHourLater = LocalDateTime.now().plusHours(1);
        LocalDateTime oneHourFiveMinutesLater = oneHourLater.plusMinutes(5);

        sessionRepository.findByDateHeureBetween(oneHourLater, oneHourFiveMinutesLater)
                .filter(session -> session.getStatut() == SessionStatus.CONFIRMEE)
                .subscribe(session -> 
                    sendSessionReminderNotification(session.getId(), 60));
    }

    /**
     * Send 15-minute reminders for upcoming sessions
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    public void sendQuarterHourReminders() {
        LocalDateTime fifteenMinutesLater = LocalDateTime.now().plusMinutes(15);
        LocalDateTime sixteenMinutesLater = fifteenMinutesLater.plusMinutes(1);

        sessionRepository.findByDateHeureBetween(fifteenMinutesLater, sixteenMinutesLater)
                .filter(session -> session.getStatut() == SessionStatus.CONFIRMEE)
                .subscribe(session -> 
                    sendSessionReminderNotification(session.getId(), 15));
    }

    /**
     * Auto-start sessions that should have begun
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    public void autoStartSessions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveMinutesAgo = now.minusMinutes(5);

        sessionRepository.findByDateHeureBetween(fiveMinutesAgo, now)
                .filter(session -> session.getStatut() == SessionStatus.CONFIRMEE)
                .flatMap(session -> {
                    session.setStatut(SessionStatus.EN_COURS);
                    return sessionRepository.save(session)
                            .doOnSuccess(savedSession -> 
                                sendSessionStartNotification(savedSession.getId()));
                })
                .subscribe();
    }

    /**
     * Send notifications for missed sessions
     */
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void notifyMissedSessions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyMinutesAgo = now.minusMinutes(30);

        sessionRepository.findByDateHeureBetween(thirtyMinutesAgo, now)
                .filter(session -> session.getStatut() == SessionStatus.CONFIRMEE)
                .flatMap(session -> {
                    // Mark as missed and send notifications
                    session.setStatut(SessionStatus.ANNULEE);
                    session.setNotes(session.getNotes() + "\n\nSession marked as missed - no participants showed up");
                    
                    return sessionRepository.save(session)
                            .doOnSuccess(savedSession -> 
                                sendMissedSessionNotification(savedSession.getId()));
                })
                .subscribe();
    }

    // ===============================================
    // BULK NOTIFICATION OPERATIONS
    // ===============================================

    /**
     * Send bulk notifications for session updates
     */
    public Flux<Notification> sendBulkSessionNotifications(NotificationBatch batch) {
        return Flux.fromIterable(batch.getRecipients())
                .flatMap(recipient -> {
                    Notification notification = createSessionNotification(
                            recipient.getUserId(),
                            batch.getTitle(),
                            personalizeMessage(batch.getMessage(), recipient),
                            NotificationType.SESSION,
                            recipient.getSessionId()
                    );
                    return notificationRepository.save(notification);
                });
    }

    /**
     * Send system maintenance notifications
     */
    public Flux<Notification> sendMaintenanceNotifications(String maintenanceMessage, 
                                                          LocalDateTime maintenanceDate) {
        // Find all users with upcoming sessions during maintenance
        return sessionRepository.findByDateHeureBetween(
                maintenanceDate.minusHours(2), 
                maintenanceDate.plusHours(2))
                .flatMap(session -> {
                    String personalizedMessage = maintenanceMessage + 
                            "\n\nVotre session prévue le " + 
                            session.getDateHeure().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) +
                            " pourrait être affectée. Nous vous contacterons pour reprogrammer si nécessaire.";

                    // Notify both participants
                    Notification studentNotification = createSystemNotification(
                            session.getEtudiantId(),
                            "Maintenance système",
                            personalizedMessage
                    );

                    Notification tutorNotification = createSystemNotification(
                            session.getTuteurId(),
                            "Maintenance système",
                            personalizedMessage
                    );

                    return Flux.just(studentNotification, tutorNotification);
                })
                .flatMap(notificationRepository::save);
    }

    // ===============================================
    // UTILITY METHODS
    // ===============================================

    private Notification createSessionNotification(Long userId, String title, String message, 
                                                 NotificationType type, Long sessionId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitre(title);
        notification.setContenu(message);
        notification.setType(type);
        notification.setEntityId(sessionId);
        notification.setDateCreation(LocalDateTime.now());
        notification.setLue(false);
        notification.setActionUrl("/sessions/" + sessionId);
        return notification;
    }

    private Notification createSystemNotification(Long userId, String title, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitre(title);
        notification.setContenu(message);
        notification.setType(NotificationType.SYSTEME);
        notification.setDateCreation(LocalDateTime.now());
        notification.setLue(false);
        return notification;
    }

    private String buildSessionConfirmationMessage(Session session, boolean isTutor) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
        String role = isTutor ? "tuteur" : "étudiant";
        return String.format(
                "Votre session en tant que %s a été confirmée pour le %s. " +
                "Durée: %d minutes. Prix: %.2f€. " +
                "%s",
                role,
                session.getDateHeure().format(formatter),
                session.getDuree(),
                session.getPrix(),
                session.getLienVisio() != null ? 
                    "Lien de visioconférence: " + session.getLienVisio() : 
                    "Lieu: " + session.getSalle()
        );
    }

    private String buildSessionReminderMessage(Session session, int minutesBeforeSession) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
        String timeRemaining = minutesBeforeSession >= 60 ? 
                (minutesBeforeSession / 60) + " heure(s)" : 
                minutesBeforeSession + " minute(s)";
        
        return String.format(
                "Rappel: Votre session commence dans %s (%s). " +
                "Durée: %d minutes. " +
                "%s",
                timeRemaining,
                session.getDateHeure().format(formatter),
                session.getDuree(),
                session.getLienVisio() != null ? 
                    "Lien de visioconférence: " + session.getLienVisio() : 
                    "Lieu: " + session.getSalle()
        );
    }

    private String buildSessionCancellationMessage(Session session, String reason, Long cancelledBy) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
        String cancellationReason = reason != null ? " Raison: " + reason : "";
        
        return String.format(
                "Votre session prévue le %s a été annulée.%s " +
                "Nous vous contacterons pour proposer de nouvelles dates si souhaité.",
                session.getDateHeure().format(formatter),
                cancellationReason
        );
    }

    private String buildSessionRescheduleMessage(Session session, LocalDateTime newDateTime, 
                                               String reason, Long rescheduledBy) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
        String rescheduleReason = reason != null ? " Raison: " + reason : "";
        
        return String.format(
                "Votre session a été reprogrammée du %s au %s.%s " +
                "Veuillez confirmer votre disponibilité.",
                session.getDateHeure().format(formatter),
                newDateTime.format(formatter),
                rescheduleReason
        );
    }

    private String buildSessionCompletionMessage(Session session) {
        return String.format(
                "Votre session de %d minutes s'est terminée avec succès. " +
                "Merci d'avoir utilisé notre plateforme !",
                session.getDuree()
        );
    }

    private String buildSessionStartMessage(Session session) {
        return String.format(
                "Votre session de %d minutes vient de commencer. " +
                "%s",
                session.getDuree(),
                session.getLienVisio() != null ? 
                    "Rejoignez la visioconférence: " + session.getLienVisio() : 
                    "Rendez-vous en " + session.getSalle()
        );
    }

    @Async
    private CompletableFuture<Void> sendMissedSessionNotification(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .flatMap(session -> {
                    String missedMessage = String.format(
                            "Votre session prévue le %s a été marquée comme manquée car aucun participant ne s'est présenté. " +
                            "Contactez le support si vous pensez qu'il s'agit d'une erreur.",
                            session.getDateHeure().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))
                    );
                    
                    // Notify both participants
                    Notification studentNotification = createSessionNotification(
                            session.getEtudiantId(),
                            "Session manquée",
                            missedMessage,
                            NotificationType.SESSION,
                            sessionId
                    );

                    Notification tutorNotification = createSessionNotification(
                            session.getTuteurId(),
                            "Session manquée",
                            missedMessage,
                            NotificationType.SESSION,
                            sessionId
                    );

                    return Mono.when(
                            notificationRepository.save(studentNotification),
                            notificationRepository.save(tutorNotification)
                    );
                })
                .toFuture()
                .thenRun(() -> {});
    }

    private String personalizeMessage(String template, NotificationRecipient recipient) {
        return template.replace("{userName}", recipient.getUserName())
                      .replace("{sessionDate}", recipient.getSessionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
    }

    // ===============================================
    // INNER CLASSES FOR DATA TRANSFER
    // ===============================================

    public static class NotificationBatch {
        private String title;
        private String message;
        private java.util.List<NotificationRecipient> recipients;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public java.util.List<NotificationRecipient> getRecipients() { return recipients; }
        public void setRecipients(java.util.List<NotificationRecipient> recipients) { this.recipients = recipients; }
    }

    public static class NotificationRecipient {
        private Long userId;
        private String userName;
        private Long sessionId;
        private LocalDateTime sessionDate;

        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
        public LocalDateTime getSessionDate() { return sessionDate; }
        public void setSessionDate(LocalDateTime sessionDate) { this.sessionDate = sessionDate; }
    }
}