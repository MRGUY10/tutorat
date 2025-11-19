package com.iiil.tutoring.repository;

import com.iiil.tutoring.entity.UserRole;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository for UserRole junction table entity
 */
@Repository
public interface UserRoleRepository extends ReactiveCrudRepository<UserRole, Long> {

    /**
     * Find all roles for a user
     */
    @Query("SELECT ur.* FROM user_roles ur WHERE ur.user_id = :userId")
    Flux<UserRole> findByUserId(Long userId);

    /**
     * Find all users with a specific role
     */
    @Query("SELECT ur.* FROM user_roles ur WHERE ur.role_id = :roleId")
    Flux<UserRole> findByRoleId(Long roleId);

    /**
     * Check if user has a specific role
     */
    @Query("SELECT EXISTS(SELECT 1 FROM user_roles WHERE user_id = :userId AND role_id = :roleId)")
    Mono<Boolean> existsByUserIdAndRoleId(Long userId, Long roleId);

    /**
     * Delete user role assignment
     */
    @Query("DELETE FROM user_roles WHERE user_id = :userId AND role_id = :roleId")
    Mono<Void> deleteByUserIdAndRoleId(Long userId, Long roleId);

    /**
     * Delete all roles for a user
     */
    @Query("DELETE FROM user_roles WHERE user_id = :userId")
    Mono<Void> deleteByUserId(Long userId);
}