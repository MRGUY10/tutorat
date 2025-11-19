package com.iiil.tutoring.controller;

import com.iiil.tutoring.entity.Role;
import com.iiil.tutoring.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controller for role management operations
 */
@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Role Management", description = "Endpoints for managing user roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * Get all roles for a specific user
     */
    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get user roles",
            description = "Retrieve all roles assigned to a specific user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.user.id")
    public Flux<Role> getUserRoles(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {
        return roleService.getUserRoles(userId);
    }

    /**
     * Check if user has a specific role
     */
    @GetMapping("/user/{userId}/has-role/{roleName}")
    @Operation(
            summary = "Check if user has role",
            description = "Check if a user has a specific role assigned"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role check completed"),
            @ApiResponse(responseCode = "400", description = "Invalid role name"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.user.id")
    public Mono<ResponseEntity<Boolean>> userHasRole(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Role name (STUDENT, TUTOR, ADMIN)", required = true)
            @PathVariable String roleName) {
        try {
            com.iiil.tutoring.enums.UserRole roleEnum = com.iiil.tutoring.enums.UserRole.valueOf(roleName.toUpperCase());
            return roleService.userHasRole(userId, roleEnum)
                    .map(hasRole -> ResponseEntity.ok(hasRole))
                    .defaultIfEmpty(ResponseEntity.ok(false));
        } catch (IllegalArgumentException e) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
    }

    /**
     * Assign a role to a user (Admin only)
     */
    @PostMapping("/user/{userId}/assign/{roleName}")
    @Operation(
            summary = "Assign role to user",
            description = "Assign a specific role to a user (Admin only)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid role name"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin only"),
            @ApiResponse(responseCode = "409", description = "Role already assigned")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<String>> assignRoleToUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Role name (STUDENT, TUTOR, ADMIN)", required = true)
            @PathVariable String roleName) {
        try {
            com.iiil.tutoring.enums.UserRole roleEnum = com.iiil.tutoring.enums.UserRole.valueOf(roleName.toUpperCase());
            return roleService.assignRoleToUser(userId, roleEnum)
                    .map(userRole -> ResponseEntity.ok("Role " + roleName + " assigned to user " + userId))
                    .onErrorReturn(ResponseEntity.status(409).body("Role assignment failed"));
        } catch (IllegalArgumentException e) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid role name: " + roleName));
        }
    }

    /**
     * Remove a role from a user (Admin only)
     */
    @DeleteMapping("/user/{userId}/remove/{roleName}")
    @Operation(
            summary = "Remove role from user",
            description = "Remove a specific role from a user (Admin only)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role removed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid role name"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin only"),
            @ApiResponse(responseCode = "404", description = "Role not assigned to user")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<String>> removeRoleFromUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Role name (STUDENT, TUTOR, ADMIN)", required = true)
            @PathVariable String roleName) {
        try {
            com.iiil.tutoring.enums.UserRole roleEnum = com.iiil.tutoring.enums.UserRole.valueOf(roleName.toUpperCase());
            return roleService.removeRoleFromUser(userId, roleEnum)
                    .then(Mono.just(ResponseEntity.ok("Role " + roleName + " removed from user " + userId)))
                    .onErrorReturn(ResponseEntity.status(404).body("Role removal failed"));
        } catch (IllegalArgumentException e) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid role name: " + roleName));
        }
    }
}