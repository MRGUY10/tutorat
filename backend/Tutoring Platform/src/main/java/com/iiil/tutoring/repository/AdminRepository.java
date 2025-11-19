package com.iiil.tutoring.repository;

import com.iiil.tutoring.entity.Admin;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository for Admin entity with proper joins to users table
 */
public interface AdminRepository extends R2dbcRepository<Admin, Long> {

    @Modifying
    @Query("INSERT INTO admins (id, permissions, departement) VALUES (:id, :permissions, :departement)")
    Mono<Void> insertAdmin(Long id, String permissions, String departement);

    @Query("SELECT a.* FROM admins a INNER JOIN users u ON a.id = u.id " +
           "WHERE u.statut = 'active' ORDER BY u.nom, u.prenom")
    Flux<Admin> findAllActive();

    @Query("SELECT a.* FROM admins a INNER JOIN users u ON a.id = u.id " +
           "WHERE a.departement = :departement AND u.statut = 'active'")
    Flux<Admin> findByDepartement(String departement);

    @Query("SELECT a.* FROM admins a INNER JOIN users u ON a.id = u.id " +
           "WHERE a.permissions ILIKE :permissions AND u.statut = 'active'")
    Flux<Admin> findByPermissionsContaining(String permissions);

    @Query("SELECT COUNT(*) FROM admins a INNER JOIN users u ON a.id = u.id " +
           "WHERE u.statut = 'active'")
    Mono<Long> countActiveAdmins();
}