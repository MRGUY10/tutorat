package com.iiil.tutoring.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

/**
 * Utility class for authentication operations
 */
public class AuthUtil {

    /**
     * Get the current authenticated user's email
     */
    public static Mono<String> getCurrentUserEmail() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName);
    }

    /**
     * Get the current authenticated user's ID
     */
    public static Mono<Long> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getDetails)
                .cast(Long.class);
    }

    /**
     * Check if current user has a specific role
     */
    public static Mono<Boolean> hasRole(String role) {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(auth -> auth.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role)));
    }

    /**
     * Check if current user is student
     */
    public static Mono<Boolean> isStudent() {
        return hasRole("STUDENT");
    }

    /**
     * Check if current user is tutor
     */
    public static Mono<Boolean> isTutor() {
        return hasRole("TUTOR");
    }

    /**
     * Check if current user is admin
     */
    public static Mono<Boolean> isAdmin() {
        return hasRole("ADMIN");
    }
}