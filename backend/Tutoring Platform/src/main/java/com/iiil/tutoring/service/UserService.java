package com.iiil.tutoring.service;

import com.iiil.tutoring.dto.user.PasswordUpdateRequest;
import com.iiil.tutoring.dto.user.PasswordUpdateResponse;
import com.iiil.tutoring.dto.user.UserWithRolesDTO;
import com.iiil.tutoring.entity.User;
import com.iiil.tutoring.entity.Etudiant;

import com.iiil.tutoring.entity.Admin;
import com.iiil.tutoring.repository.UserRepository;
import com.iiil.tutoring.repository.EtudiantRepository;

import com.iiil.tutoring.repository.AdminRepository;
import com.iiil.tutoring.enums.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service layer for User management with inheritance handling
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EtudiantRepository etudiantRepository;



    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Create a new student user
     */
    public Mono<Etudiant> createEtudiant(User userData, Etudiant etudiantData) {
        return roleService.initializeDefaultRoles()
                .then(userRepository.save(userData))
                .flatMap(savedUser -> {
                    // Use custom insert to avoid R2DBC thinking it's an update
                    return etudiantRepository.insertEtudiant(
                            savedUser.getId(), 
                            etudiantData.getFiliere(), 
                            etudiantData.getAnnee(), 
                            etudiantData.getNiveau() // now String
                    ).thenReturn(savedUser.getId()); // Return the ID after successful insert
                })
                .flatMap(userId -> {
                    // Assign STUDENT role to the user
                    return roleService.assignRoleToUser(userId, com.iiil.tutoring.enums.UserRole.STUDENT)
                            .thenReturn(userId);
                })
                .map(userId -> {
                    // Create and return the complete Etudiant object
                    etudiantData.setId((Long) userId);
                    return etudiantData;
                });
    }


    /**
     * Create a new admin user
     */
    public Mono<Admin> createAdmin(User userData, Admin adminData) {
        return roleService.initializeDefaultRoles()
                .then(userRepository.save(userData))
                .flatMap(savedUser -> {
                    // Use custom insert to avoid R2DBC thinking it's an update
                    return adminRepository.insertAdmin(
                            savedUser.getId(),
                            adminData.getPermissions(),
                            adminData.getDepartement()
                    ).then(Mono.fromCallable(() -> {
                        adminData.setId(savedUser.getId());
                        return adminData;
                    }));
                })
                .flatMap(admin -> {
                    // Assign ADMIN role to the user
                    return roleService.assignRoleToUser(admin.getId(), com.iiil.tutoring.enums.UserRole.ADMIN)
                            .thenReturn(admin);
                });
    }

    /**
     * Get user by email regardless of type
     */
    public Mono<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Get all users with pagination
     */
    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get users by status
     */
    public Flux<User> getUsersByStatus(UserStatus status) {
        return userRepository.findByStatut(status);
    }

    /**
     * Search users by pattern
     */
    public Flux<User> searchUsers(String pattern) {
        return userRepository.findBySearchPattern("%" + pattern + "%");
    }

    /**
     * Convert User to UserWithRolesDTO including role information
     */
    public Mono<UserWithRolesDTO> convertToUserWithRolesDTO(User user) {
        return roleService.getUserRoles(user.getId())
                .map(role -> role.getNom().getValue())
                .collectList()
                .map(roles -> {
                    String primaryRole = roles.isEmpty() ? "USER" : roles.get(0);
                    String userType = determineUserType(user);
                    
                    return new UserWithRolesDTO(
                            user.getId(),
                            user.getNom(),
                            user.getPrenom(),
                            user.getEmail(),
                            user.getTelephone(),
                            user.getStatut(),
                            user.getPhoto(),
                            user.getDateInscription(),
                            roles,
                            primaryRole,
                            userType,
                            user.getCreatedAt(),
                            user.getUpdatedAt()
                    );
                });
    }

    /**
     * Get all users with role information
     */
    public Flux<UserWithRolesDTO> getAllUsersWithRoles() {
        return userRepository.findAll()
                .flatMap(this::convertToUserWithRolesDTO);
    }

    /**
     * Get user by ID with role information
     */
    public Mono<UserWithRolesDTO> getUserByIdWithRoles(Long userId) {
        return userRepository.findById(userId)
                .flatMap(this::convertToUserWithRolesDTO);
    }

    /**
     * Block a user (set status to SUSPENDED)
     */
    public Mono<User> blockUser(Long userId, String reason) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found with ID: " + userId)))
                .flatMap(user -> {
                    user.setStatut(UserStatus.SUSPENDED);
                    return userRepository.save(user);
                })
                .doOnSuccess(user -> System.out.println("✅ User " + userId + " has been blocked. Reason: " + reason))
                .doOnError(error -> System.err.println("❌ Failed to block user " + userId + ": " + error.getMessage()));
    }

    /**
     * Unblock a user (set status to ACTIVE)
     */
    public Mono<User> unblockUser(Long userId, String reason) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found with ID: " + userId)))
                .flatMap(user -> {
                    user.setStatut(UserStatus.ACTIVE);
                    return userRepository.save(user);
                })
                .doOnSuccess(user -> System.out.println("✅ User " + userId + " has been unblocked. Reason: " + reason))
                .doOnError(error -> System.err.println("❌ Failed to unblock user " + userId + ": " + error.getMessage()));
    }

    /**
     * Update user information
     */
    public Mono<User> updateUser(Long userId, com.iiil.tutoring.dto.user.UpdateUserRequest updateRequest) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId)))
                .flatMap(existingUser -> {
                    // Update only non-null fields
                    if (updateRequest.getPrenom() != null) {
                        existingUser.setPrenom(updateRequest.getPrenom());
                    }
                    if (updateRequest.getNom() != null) {
                        existingUser.setNom(updateRequest.getNom());
                    }
                    if (updateRequest.getEmail() != null) {
                        // Check if email is already taken by another user
                        return userRepository.findByEmail(updateRequest.getEmail())
                                .flatMap(userWithEmail -> {
                                    if (!userWithEmail.getId().equals(userId)) {
                                        return Mono.error(new IllegalArgumentException("Cet email est déjà utilisé par un autre utilisateur"));
                                    }
                                    existingUser.setEmail(updateRequest.getEmail());
                                    return updateUserFields(existingUser, updateRequest);
                                })
                                .switchIfEmpty(Mono.defer(() -> {
                                    existingUser.setEmail(updateRequest.getEmail());
                                    return updateUserFields(existingUser, updateRequest);
                                }));
                    } else {
                        return updateUserFields(existingUser, updateRequest);
                    }
                })
                .doOnSuccess(user -> System.out.println("✅ User " + userId + " has been updated successfully"))
                .doOnError(error -> System.err.println("❌ Error updating user " + userId + ": " + error.getMessage()));
    }

    private Mono<User> updateUserFields(User user, com.iiil.tutoring.dto.user.UpdateUserRequest updateRequest) {
        if (updateRequest.getTelephone() != null) {
            user.setTelephone(updateRequest.getTelephone());
        }
        if (updateRequest.getStatut() != null) {
            user.setStatut(updateRequest.getStatut());
        }
        if (updateRequest.getPhoto() != null) {
            user.setPhoto(updateRequest.getPhoto());
        }
        return userRepository.save(user);
    }

    /**
     * Get user statistics for dashboard
     */
    public Mono<com.iiil.tutoring.dto.user.UserStatisticsDTO> getUserStatistics() {
        return Mono.zip(
                userRepository.count(), // total users
                userRepository.countByStatut(UserStatus.ACTIVE), // active users
                userRepository.countByStatut(UserStatus.SUSPENDED), // blocked users
                etudiantRepository.count() // total students
        ).map(tuple -> new com.iiil.tutoring.dto.user.UserStatisticsDTO(
                tuple.getT1(), // totalUsers
                tuple.getT2(), // activeUsers
                tuple.getT3(), // blockedUsers
                0L, // totalTutors (removed)
                tuple.getT4()  // totalStudents
        ));
    }

    /**
     * Determine user type based on user class or database lookup
     */
    private String determineUserType(User user) {
        String className = user.getClass().getSimpleName();
        switch (className) {
            case "Etudiant":
                return "Etudiant";
            case "Tuteur":
                return "Tuteur";
            case "Admin":
                return "Admin";
            default:
                return "User";
        }
    }

    /**
     * Update user password with security validation
     */
    public Mono<PasswordUpdateResponse> updatePassword(Long userId, PasswordUpdateRequest request) {
        // Validate request
        if (!request.isPasswordConfirmed()) {
            return Mono.just(PasswordUpdateResponse.failure("Le nouveau mot de passe et sa confirmation ne correspondent pas"));
        }

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new RuntimeException("Utilisateur non trouvé")))
                .flatMap(user -> {
                    // Verify current password
                    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getMotDePasse())) {
                        return Mono.just(PasswordUpdateResponse.failure("Le mot de passe actuel est incorrect"));
                    }

                    // Check if new password is different from current password
                    if (passwordEncoder.matches(request.getNewPassword(), user.getMotDePasse())) {
                        return Mono.just(PasswordUpdateResponse.failure("Le nouveau mot de passe doit être différent de l'ancien"));
                    }

                    // Encode and update password
                    user.setMotDePasse(passwordEncoder.encode(request.getNewPassword()));
                    
                    return userRepository.save(user)
                            .map(savedUser -> PasswordUpdateResponse.success("Mot de passe mis à jour avec succès"))
                            .onErrorReturn(PasswordUpdateResponse.failure("Erreur lors de la mise à jour du mot de passe"));
                })
                .onErrorReturn(throwable -> {
                    if (throwable instanceof RuntimeException && throwable.getMessage().equals("Utilisateur non trouvé")) {
                        return true;
                    }
                    return false;
                }, PasswordUpdateResponse.failure("Utilisateur non trouvé"))
                .onErrorReturn(PasswordUpdateResponse.failure("Erreur interne du serveur"));
    }
}