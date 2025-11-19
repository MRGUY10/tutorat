package com.iiil.tutoring.controller;

import com.iiil.tutoring.dto.user.PasswordUpdateRequest;
import com.iiil.tutoring.dto.user.PasswordUpdateResponse;
import com.iiil.tutoring.dto.user.UserWithRolesDTO;
import com.iiil.tutoring.dto.user.UserStatusChangeRequest;
import com.iiil.tutoring.entity.User;
import com.iiil.tutoring.entity.Etudiant;

import com.iiil.tutoring.entity.Admin;
import com.iiil.tutoring.dto.admin.CreateStudentRequest;

import com.iiil.tutoring.dto.admin.CreateAdminRequest;
import com.iiil.tutoring.service.UserService;
import com.iiil.tutoring.repository.UserRepository;
import com.iiil.tutoring.enums.UserStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import jakarta.validation.Valid;

/**
 * REST Controller for User operations
 */
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
@Tag(name = "User Management", description = "User profile and management operations")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all users with roles
     */
    @GetMapping
    @Operation(
            summary = "Get all users with roles",
            description = "Retrieve a list of all users in the system with their assigned roles. Requires admin privileges.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of users with roles retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserWithRolesDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access denied - Admin privileges required"
                    )
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<UserWithRolesDTO> getAllUsers() {
        return userService.getAllUsersWithRoles();
    }

    /**
     * Get user by ID with roles
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get user by ID with roles",
            description = "Retrieve a specific user by their unique identifier including role information",
            parameters = @Parameter(
                    name = "id",
                    description = "User ID",
                    required = true,
                    example = "123"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserWithRolesDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.user.id")
    public Mono<UserWithRolesDTO> getUserById(@PathVariable Long id) {
        return userService.getUserByIdWithRoles(id);
    }

    /**
     * Get user by email
     */
    @GetMapping("/email/{email}")
    @Operation(
            summary = "Get user by email",
            description = "Retrieve a user by their email address",
            parameters = @Parameter(
                    name = "email",
                    description = "User email address",
                    required = true,
                    example = "user@example.com"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    public Mono<User> getUserByEmail(@PathVariable String email) {
        return userService.findByEmail(email);
    }

    /**
     * Search users by pattern
     */
    @GetMapping("/search")
    public Flux<User> searchUsers(@RequestParam String pattern) {
        return userService.searchUsers(pattern);
    }

    /**
     * Create a new student (Admin only)
     * Note: Regular registration should use /api/auth/register/student
     */
    @PostMapping("/students")
    public Mono<Etudiant> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        User userData = new User();
        userData.setNom(request.getNom());
        userData.setPrenom(request.getPrenom());
        userData.setEmail(request.getEmail());
        userData.setMotDePasse(request.getMotDePasse()); // Should be hashed
        userData.setTelephone(request.getTelephone());
        userData.setStatut(UserStatus.ACTIVE);
        userData.setDateInscription(java.time.LocalDateTime.now());

        Etudiant etudiantData = new Etudiant();
        etudiantData.setFiliere(request.getFiliere());
        etudiantData.setAnnee(request.getAnnee());
        etudiantData.setNiveau(request.getNiveau());

        return userService.createEtudiant(userData, etudiantData);
    }



    /**
     * Block a user (Admin only)
     */
    @PostMapping("/{id}/block")
    @Operation(
            summary = "Block a user",
            description = "Block a user account (set status to SUSPENDED). Only admins can perform this action.",
            parameters = @Parameter(
                    name = "id",
                    description = "User ID to block",
                    required = true,
                    example = "123"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User blocked successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserWithRolesDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin privileges required"
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<UserWithRolesDTO>> blockUser(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusChangeRequest request) {
        String reason = request.getReason() != null ? request.getReason() : "No reason provided";
        return userService.blockUser(id, reason)
                .flatMap(user -> userService.convertToUserWithRolesDTO(user))
                .map(userDTO -> ResponseEntity.ok(userDTO))
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    /**
     * Unblock a user (Admin only)
     */
    @PostMapping("/{id}/unblock")
    @Operation(
            summary = "Unblock a user",
            description = "Unblock a user account (set status to ACTIVE). Only admins can perform this action.",
            parameters = @Parameter(
                    name = "id",
                    description = "User ID to unblock",
                    required = true,
                    example = "123"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User unblocked successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserWithRolesDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin privileges required"
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<UserWithRolesDTO>> unblockUser(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusChangeRequest request) {
        String reason = request.getReason() != null ? request.getReason() : "No reason provided";
        return userService.unblockUser(id, reason)
                .flatMap(user -> userService.convertToUserWithRolesDTO(user))
                .map(userDTO -> ResponseEntity.ok(userDTO))
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    /**
     * Get users by status
     */
    @GetMapping("/status/{status}")
    @Operation(
            summary = "Get users by status",
            description = "Retrieve users filtered by their status (ACTIVE, INACTIVE, SUSPENDED)",
            parameters = @Parameter(
                    name = "status",
                    description = "User status to filter by",
                    required = true,
                    example = "ACTIVE"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserWithRolesDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin privileges required"
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<UserWithRolesDTO> getUsersByStatus(@PathVariable String status) {
        try {
            UserStatus userStatus = UserStatus.valueOf(status.toUpperCase());
            return userService.getUsersByStatus(userStatus)
                    .flatMap(user -> userService.convertToUserWithRolesDTO(user));
        } catch (IllegalArgumentException e) {
            return Flux.empty();
        }
    }

    /**
     * Create a new admin (Super admin only)
     */
    @PostMapping("/admins")
    public Mono<Admin> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        User userData = new User();
        userData.setNom(request.getNom());
        userData.setPrenom(request.getPrenom());
        userData.setEmail(request.getEmail());
        userData.setMotDePasse(request.getMotDePasse()); // Should be hashed
        userData.setTelephone(request.getTelephone());
        userData.setStatut(UserStatus.ACTIVE);
        userData.setDateInscription(java.time.LocalDateTime.now());

        Admin adminData = new Admin();
        adminData.setPermissions(request.getPermissions());
        adminData.setDepartement(request.getDepartement());

        return userService.createAdmin(userData, adminData);
    }

    /**
     * Update user information
     */
    @Operation(summary = "Update user information", 
               description = "Update basic user information like name, email, phone, status, etc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserWithRolesDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<UserWithRolesDTO>> updateUser(
            @PathVariable Long id, 
            @Valid @RequestBody com.iiil.tutoring.dto.user.UpdateUserRequest updateRequest) {
        
        return userService.updateUser(id, updateRequest)
                .flatMap(user -> userService.convertToUserWithRolesDTO(user))
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalArgumentException.class, 
                    e -> Mono.just(ResponseEntity.badRequest().build()))
                .onErrorResume(Exception.class, 
                    e -> Mono.just(ResponseEntity.status(500).build()));
    }

    /**
     * Get users count
     */
    @GetMapping("/count")
    public Mono<Long> getUsersCount() {
        return userRepository.count();
    }

    /**
     * Get user statistics for dashboard
     */
    @GetMapping("/statistics")
    public Mono<com.iiil.tutoring.dto.user.UserStatisticsDTO> getUserStatistics() {
        return userService.getUserStatistics();
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public Mono<String> health() {
        return Mono.just("User service is running!");
    }

    /**
     * Update user password
     */
    @PutMapping("/{userId}/password")
    @Operation(
            summary = "Update user password",
            description = "Update the password for a specific user. Requires current password verification."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PasswordUpdateResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or password validation failed"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - can only update own password"
            )
    })
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public Mono<ResponseEntity<PasswordUpdateResponse>> updatePassword(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Valid @RequestBody PasswordUpdateRequest request) {
        
        return userService.updatePassword(userId, request)
                .map(response -> {
                    if (response.isSuccess()) {
                        return ResponseEntity.ok(response);
                    } else {
                        return ResponseEntity.badRequest().body(response);
                    }
                })
                .onErrorReturn(ResponseEntity.status(500).body(
                    PasswordUpdateResponse.failure("Erreur interne du serveur")
                ));
    }
}