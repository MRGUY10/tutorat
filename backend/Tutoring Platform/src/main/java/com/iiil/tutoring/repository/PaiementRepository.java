package com.iiil.tutoring.repository;

import com.iiil.tutoring.entity.Paiement;
import com.iiil.tutoring.enums.PaymentMethod;
import com.iiil.tutoring.enums.PaymentStatus;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Repository for Paiement entity
 */
public interface PaiementRepository extends R2dbcRepository<Paiement, Long> {

    Flux<Paiement> findByUserId(Long userId);

    Flux<Paiement> findBySessionId(Long sessionId);

    Flux<Paiement> findByStatut(PaymentStatus statut);

    Flux<Paiement> findByMethodePaiement(PaymentMethod methodePaiement);

    Mono<Paiement> findByReferenceTransaction(String referenceTransaction);

    @Query("SELECT * FROM paiements WHERE user_id = :userId AND statut = :statut ORDER BY date_paiement DESC")
    Flux<Paiement> findByUserIdAndStatutOrderByDatePaiementDesc(Long userId, PaymentStatus statut);

    @Query("SELECT * FROM paiements WHERE date_paiement BETWEEN :debut AND :fin ORDER BY date_paiement DESC")
    Flux<Paiement> findByDatePaiementBetween(LocalDateTime debut, LocalDateTime fin);

    @Query("SELECT SUM(montant) FROM paiements WHERE user_id = :userId AND statut = 'COMPLETE'")
    Mono<Double> getTotalAmountByUser(Long userId);

    @Query("SELECT SUM(montant_tuteur) FROM paiements p " +
           "INNER JOIN sessions s ON p.session_id = s.id " +
           "WHERE s.tuteur_id = :tuteurId AND p.statut = 'COMPLETE'")
    Mono<Double> getTotalEarningsByTuteur(Long tuteurId);

    @Query("SELECT SUM(commission) FROM paiements WHERE statut = 'COMPLETE' AND date_paiement BETWEEN :debut AND :fin")
    Mono<Double> getTotalCommissionBetweenDates(LocalDateTime debut, LocalDateTime fin);

    @Query("SELECT COUNT(*) FROM paiements WHERE user_id = :userId AND statut = :statut")
    Mono<Long> countByUserIdAndStatut(Long userId, PaymentStatus statut);

    @Query("SELECT * FROM paiements WHERE statut = 'EN_ATTENTE' AND created_at < :dateLimit")
    Flux<Paiement> findExpiredPendingPayments(LocalDateTime dateLimit);

    @Query("SELECT AVG(montant) FROM paiements WHERE statut = 'COMPLETE'")
    Mono<Double> getAveragePaymentAmount();
}