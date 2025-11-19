package com.iiil.tutoring.repository;

import com.iiil.tutoring.entity.Notification;
import com.iiil.tutoring.enums.NotificationType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Repository for Notification entity
 */
public interface NotificationRepository extends R2dbcRepository<Notification, Long> {

    Flux<Notification> findByUserId(Long userId);

    Flux<Notification> findByUserIdAndLue(Long userId, boolean lue);

    Flux<Notification> findByType(NotificationType type);

    @Query("SELECT * FROM notifications WHERE user_id = :userId ORDER BY date_creation DESC")
    Flux<Notification> findByUserIdOrderByDateCreationDesc(Long userId);

    @Query("SELECT * FROM notifications WHERE user_id = :userId AND lue = false ORDER BY date_creation DESC")
    Flux<Notification> findUnreadByUserId(Long userId);

    @Query("SELECT * FROM notifications WHERE user_id = :userId AND type = :type ORDER BY date_creation DESC")
    Flux<Notification> findByUserIdAndType(Long userId, NotificationType type);

    @Query("SELECT * FROM notifications WHERE user_id = :userId AND date_creation >= :dateDebut ORDER BY date_creation DESC")
    Flux<Notification> findRecentByUserId(Long userId, LocalDateTime dateDebut);

    @Query("SELECT COUNT(*) FROM notifications WHERE user_id = :userId AND lue = false")
    Mono<Long> countUnreadByUserId(Long userId);

    @Query("SELECT * FROM notifications WHERE date_creation < :dateLimit AND lue = true")
    Flux<Notification> findOldReadNotifications(LocalDateTime dateLimit);

    @Query("UPDATE notifications SET lue = true WHERE user_id = :userId AND lue = false")
    Mono<Void> markAllAsReadByUserId(Long userId);
}