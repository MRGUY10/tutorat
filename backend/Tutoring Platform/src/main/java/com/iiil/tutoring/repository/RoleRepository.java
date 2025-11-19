package com.iiil.tutoring.repository;

import com.iiil.tutoring.entity.Role;
import com.iiil.tutoring.enums.UserRole;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Repository for Role entity
 */
@Repository
public interface RoleRepository extends ReactiveCrudRepository<Role, Long> {

    /**
     * Find role by name
     */
    Mono<Role> findByNom(UserRole nom);

    /**
     * Check if role exists by name
     */
    @Query("SELECT EXISTS(SELECT 1 FROM roles WHERE nom = :nom)")
    Mono<Boolean> existsByNom(UserRole nom);
}