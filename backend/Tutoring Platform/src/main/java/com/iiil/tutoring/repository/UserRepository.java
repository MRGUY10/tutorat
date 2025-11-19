package com.iiil.tutoring.repository;

import com.iiil.tutoring.entity.User;
import com.iiil.tutoring.enums.UserStatus;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository for User entity
 */
public interface UserRepository extends R2dbcRepository<User, Long> {

    Mono<User> findByEmail(String email);

    Flux<User> findByStatut(UserStatus statut);

    @Query("SELECT * FROM users WHERE nom ILIKE :pattern OR prenom ILIKE :pattern OR email ILIKE :pattern")
    Flux<User> findBySearchPattern(String pattern);

    @Query("SELECT * FROM users WHERE statut = :statut AND date_inscription >= :dateDebut")
    Flux<User> findByStatutAndDateInscriptionAfter(UserStatus statut, java.time.LocalDateTime dateDebut);

    Mono<Boolean> existsByEmail(String email);

    @Query("SELECT COUNT(*) FROM users WHERE statut = :statut")
    Mono<Long> countByStatut(UserStatus statut);
}