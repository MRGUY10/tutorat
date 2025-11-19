package com.iiil.tutoring.service;

import com.iiil.tutoring.entity.Role;
import com.iiil.tutoring.entity.UserRole;
import com.iiil.tutoring.repository.RoleRepository;
import com.iiil.tutoring.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service for managing user roles and role assignments
 */
@Service
@Transactional
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    /**
     * Initialize default roles if they don't exist
     */
    public Mono<Void> initializeDefaultRoles() {
        return Mono.when(
                ensureRoleExists(com.iiil.tutoring.enums.UserRole.STUDENT, "Student role for learners"),
                ensureRoleExists(com.iiil.tutoring.enums.UserRole.TUTOR, "Tutor role for teachers"),
                ensureRoleExists(com.iiil.tutoring.enums.UserRole.ADMIN, "Admin role for system administrators")
        );
    }

    /**
     * Ensure a role exists, create if it doesn't
     */
    private Mono<Role> ensureRoleExists(com.iiil.tutoring.enums.UserRole roleEnum, String description) {
        return roleRepository.findByNom(roleEnum)
                .switchIfEmpty(
                        roleRepository.save(new Role(roleEnum, description))
                );
    }

    /**
     * Get role by name
     */
    public Mono<Role> getRoleByName(com.iiil.tutoring.enums.UserRole roleEnum) {
        return roleRepository.findByNom(roleEnum);
    }

    /**
     * Assign a role to a user
     */
    public Mono<UserRole> assignRoleToUser(Long userId, com.iiil.tutoring.enums.UserRole roleEnum) {
        return getRoleByName(roleEnum)
                .flatMap(role -> 
                    userRoleRepository.existsByUserIdAndRoleId(userId, role.getId())
                            .flatMap(exists -> {
                                if (exists) {
                                    // Role already assigned, return existing
                                    return userRoleRepository.findByUserId(userId)
                                            .filter(ur -> ur.getRoleId().equals(role.getId()))
                                            .next();
                                } else {
                                    // Create new role assignment
                                    UserRole userRole = new UserRole(userId, role.getId());
                                    return userRoleRepository.save(userRole);
                                }
                            })
                );
    }

    /**
     * Get all roles for a user
     */
    public Flux<Role> getUserRoles(Long userId) {
        return userRoleRepository.findByUserId(userId)
                .flatMap(userRole -> roleRepository.findById(userRole.getRoleId()));
    }

    /**
     * Remove a role from a user
     */
    public Mono<Void> removeRoleFromUser(Long userId, com.iiil.tutoring.enums.UserRole roleEnum) {
        return getRoleByName(roleEnum)
                .flatMap(role -> userRoleRepository.deleteByUserIdAndRoleId(userId, role.getId()));
    }

    /**
     * Remove all roles from a user
     */
    public Mono<Void> removeAllUserRoles(Long userId) {
        return userRoleRepository.deleteByUserId(userId);
    }

    /**
     * Check if user has a specific role
     */
    public Mono<Boolean> userHasRole(Long userId, com.iiil.tutoring.enums.UserRole roleEnum) {
        return getRoleByName(roleEnum)
                .flatMap(role -> userRoleRepository.existsByUserIdAndRoleId(userId, role.getId()))
                .defaultIfEmpty(false);
    }
}