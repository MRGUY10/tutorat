package com.iiil.tutoring.service;

import com.iiil.tutoring.dto.admin.CreateAdminRequest;
import com.iiil.tutoring.dto.auth.*;
import com.iiil.tutoring.dto.tutor.TutorRegistrationRequest;
import com.iiil.tutoring.entity.Admin;
import com.iiil.tutoring.entity.Etudiant;
import com.iiil.tutoring.entity.Role;
import com.iiil.tutoring.entity.User;
import com.iiil.tutoring.enums.UserStatus;
import com.iiil.tutoring.repository.UserRepository;
import com.iiil.tutoring.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Authentication service handling login, registration, and token management
 */
@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TutorService tutorService;

    @Autowired
    private RoleService roleService;

    /**
     * Authenticate user and return JWT tokens
     */
    public Mono<AuthResponse> login(LoginRequest loginRequest) {
        // Find user in users table (all users including tutors are in this table)
        return userRepository.findByEmail(loginRequest.getEmail())
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Utilisateur non trouvé")))
                .filter(user -> user.getStatut() == UserStatus.ACTIVE)
                .switchIfEmpty(Mono.error(new BadCredentialsException("Compte désactivé")))
                .filter(user -> passwordEncoder.matches(loginRequest.getMotDePasse(), user.getMotDePasse()))
                .switchIfEmpty(Mono.error(new BadCredentialsException("Mot de passe incorrect")))
                .flatMap(user -> 
                    determineUserRole(user).map(role -> new Object[] { user, role })
                )
                .map(data -> {
                    User user = (User) ((Object[]) data)[0];
                    String role = (String) ((Object[]) data)[1];
                    
                    // Prevent users with USER role from logging in
                    if ("USER".equals(role)) {
                        throw new BadCredentialsException("Accès refusé. Les utilisateurs avec le rôle USER ne peuvent pas se connecter.");
                    }
                    
                    String token = jwtUtil.generateToken(user.getEmail(), role, user.getId());
                    String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
                    
                    return new AuthResponse(
                            token,
                            refreshToken,
                            jwtUtil.getExpirationTime(),
                            user.getEmail(),
                            role,
                            user.getId()
                    );
                });
    }

    /**
     * Register a new student
     */
    public Mono<AuthResponse> registerStudent(StudentRegisterRequest request) {
        return checkEmailExists(request.getEmail())
                .then(Mono.fromCallable(() -> {
                    // Create user data
                    User userData = new User();
                    userData.setNom(request.getNom());
                    userData.setPrenom(request.getPrenom());
                    userData.setEmail(request.getEmail());
                    userData.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
                    userData.setTelephone(request.getTelephone());
                    userData.setStatut(UserStatus.ACTIVE);
                    userData.setDateInscription(LocalDateTime.now());

                    // Create student data
                    Etudiant etudiantData = new Etudiant();
                    etudiantData.setFiliere(request.getFiliere());
                    etudiantData.setAnnee(request.getAnnee());
                    etudiantData.setNiveau(request.getNiveau()); // This will use the enum setNiveau method

                    return new Object[] { userData, etudiantData };
                }))
                .flatMap(data -> {
                    User userData = (User) ((Object[]) data)[0];
                    Etudiant etudiantData = (Etudiant) ((Object[]) data)[1];
                    return userService.createEtudiant(userData, etudiantData)
                            .map(savedStudent -> {
                                String role = "STUDENT";
                                String token = jwtUtil.generateToken(userData.getEmail(), role, savedStudent.getId());
                                String refreshToken = jwtUtil.generateRefreshToken(userData.getEmail());
                                
                                return new AuthResponse(
                                        token,
                                        refreshToken,
                                        jwtUtil.getExpirationTime(),
                                        userData.getEmail(),
                                        role,
                                        savedStudent.getId()
                                );
                            });
                });
    }



    /**
     * Register a new admin user
     */
    public Mono<AuthResponse> registerAdmin(CreateAdminRequest request) {
        return checkEmailExists(request.getEmail())
                .then(Mono.defer(() -> {
                    // Create user data
                    User userData = new User();
                    userData.setNom(request.getNom());
                    userData.setPrenom(request.getPrenom());
                    userData.setEmail(request.getEmail());
                    userData.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
                    userData.setTelephone(request.getTelephone());
                    userData.setStatut(UserStatus.ACTIVE);
                    userData.setDateInscription(LocalDateTime.now());

                    // Create admin data
                    Admin adminData = new Admin();
                    adminData.setPermissions(request.getPermissions() != null ? request.getPermissions() : "READ_WRITE_ALL");
                    adminData.setDepartement(request.getDepartement() != null ? request.getDepartement() : "Administration");

                    return userService.createAdmin(userData, adminData);
                }))
                .map(savedAdmin -> {
                    String role = "ADMIN";
                    String token = jwtUtil.generateToken(request.getEmail(), role, savedAdmin.getId());
                    String refreshToken = jwtUtil.generateRefreshToken(request.getEmail());
                    
                    return new AuthResponse(
                            token,
                            refreshToken,
                            jwtUtil.getExpirationTime(),
                            request.getEmail(),
                            role,
                            savedAdmin.getId()
                    );
                })
                .doOnError(error -> {
                    System.err.println("Error in registerAdmin: " + error.getMessage());
                    error.printStackTrace();
                })
                .onErrorMap(throwable -> {
                    System.err.println("Mapping error in registerAdmin: " + throwable.getMessage());
                    return new RuntimeException("Admin registration failed: " + throwable.getMessage(), throwable);
                });
    }

    /**
     * Register a new tutor user
     */
    public Mono<AuthResponse> registerTutor(TutorRegistrationRequest request) {
        return checkEmailExists(request.getEmail())
                .then(Mono.defer(() -> tutorService.registerTutor(request)))
                .flatMap(savedTutor -> 
                    userRepository.findById(savedTutor.getId())
                        .map(user -> {
                            String role = "TUTOR";
                            String token = jwtUtil.generateToken(user.getEmail(), role, user.getId());
                            String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
                            
                            return new AuthResponse(
                                    token,
                                    refreshToken,
                                    jwtUtil.getExpirationTime(),
                                    user.getEmail(),
                                    role,
                                    user.getId()
                            );
                        })
                )
                .doOnError(error -> {
                    System.err.println("Error in registerTutor: " + error.getMessage());
                    error.printStackTrace();
                })
                .onErrorMap(throwable -> {
                    System.err.println("Mapping error in registerTutor: " + throwable.getMessage());
                    return new RuntimeException("Tutor registration failed: " + throwable.getMessage(), throwable);
                });
    }

    /**
     * Refresh access token using refresh token
     */
    public Mono<AuthResponse> refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        if (!jwtUtil.isValidToken(refreshToken)) {
            return Mono.error(new BadCredentialsException("Refresh token invalide"));
        }

        String email = jwtUtil.extractEmail(refreshToken);
        
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Utilisateur non trouvé")))
                .filter(user -> user.getStatut() == UserStatus.ACTIVE)
                .switchIfEmpty(Mono.error(new BadCredentialsException("Compte désactivé")))
                .flatMap(user -> 
                    determineUserRole(user).map(role -> new Object[] { user, role })
                )
                .map(data -> {
                    User user = (User) ((Object[]) data)[0];
                    String role = (String) ((Object[]) data)[1];
                    String newToken = jwtUtil.generateToken(user.getEmail(), role, user.getId());
                    String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail());
                    
                    return new AuthResponse(
                            newToken,
                            newRefreshToken,
                            jwtUtil.getExpirationTime(),
                            user.getEmail(),
                            role,
                            user.getId()
                    );
                });
    }

    /**
     * Check if email already exists in both users and tutors tables
     */
    private Mono<Void> checkEmailExists(String email) {
        return userRepository.existsByEmail(email)
                .flatMap(userExists -> {
                    if (userExists) {
                        return Mono.error(new IllegalArgumentException("Un compte avec cet email existe déjà"));
                    }
                    // Also check tutors table
                    return tutorService.getTutorByEmail(email)
                            .flatMap(tutor -> Mono.error(new IllegalArgumentException("Un tuteur avec cet email existe déjà")))
                            .onErrorResume(IllegalArgumentException.class, ex -> {
                                // If the error is "tuteur non trouvé", that's good - email is available
                                if (ex.getMessage().contains("Tuteur non trouvé")) {
                                    return Mono.empty();
                                }
                                // Re-throw other IllegalArgumentExceptions
                                return Mono.error(ex);
                            })
                            .then();
                });
    }

    /**
     * Public method to check if email exists (for API endpoint)
     */
    public Mono<Boolean> isEmailAvailable(String email) {
        return userRepository.existsByEmail(email)
                .flatMap(userExists -> {
                    if (userExists) {
                        return Mono.just(false); // Email not available
                    }
                    // Also check tutors table
                    return tutorService.getTutorByEmail(email)
                            .map(tutor -> false) // Email not available - tutor exists
                            .onErrorReturn(IllegalArgumentException.class, true); // Email available - no tutor found
                });
    }

    /**
     * Determine user role from user_roles table
     */
    private Mono<String> determineUserRole(User user) {
        return roleService.getUserRoles(user.getId())
                .map(role -> role.getNom().name())  // Convert UserRole enum to String
                .collectList()
                .map(roleNames -> {
                    if (roleNames.isEmpty()) {
                        return "USER";
                    }
                    
                    // Priority: ADMIN > TUTOR > STUDENT
                    if (roleNames.contains("ADMIN")) {
                        return "ADMIN";
                    } else if (roleNames.contains("TUTOR")) {
                        return "TUTOR";
                    } else if (roleNames.contains("STUDENT")) {
                        return "STUDENT";
                    } else {
                        return roleNames.get(0);
                    }
                });
    }
}