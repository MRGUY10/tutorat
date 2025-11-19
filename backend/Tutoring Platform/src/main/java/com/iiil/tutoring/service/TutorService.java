package com.iiil.tutoring.service;

import com.iiil.tutoring.dto.tutor.*;
import com.iiil.tutoring.entity.Tutor;
import com.iiil.tutoring.entity.User;
import com.iiil.tutoring.entity.UserRole;
import com.iiil.tutoring.entity.TutorSpecialite;
import com.iiil.tutoring.enums.UserStatus;
import com.iiil.tutoring.repository.TutorRepository;
import com.iiil.tutoring.repository.UserRepository;
import com.iiil.tutoring.repository.UserRoleRepository;
import com.iiil.tutoring.repository.RoleRepository;
import com.iiil.tutoring.repository.TutorSpecialiteRepository;
import com.iiil.tutoring.repository.MatiereRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;


/**
 * Service class for tutor management operations
 * Handles business logic, validation, and data transformation
 */
@Service
@Transactional
@Slf4j
public class TutorService {

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TutorSpecialiteRepository tutorSpecialiteRepository;

    @Autowired
    private MatiereRepository matiereRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // === REGISTRATION AND PROFILE MANAGEMENT ===

    /**
     * Register a new tutor
     */
    public Mono<Tutor> registerTutor(TutorRegistrationRequest request) {
        log.info("Registering new tutor with email: {}", request.getEmail());
        
        return checkEmailAvailability(request.getEmail())
                .then(Mono.defer(() -> {
                    // Step 1: Create User first
                    User user = new User(
                        request.getNom(),
                        request.getPrenom(),
                        request.getEmail(),
                        passwordEncoder.encode(request.getMotDePasse())
                    );
                    user.setTelephone(request.getTelephone());
                    user.setStatut(UserStatus.ACTIVE);
                    
                    // Step 2: Save User and get the generated ID
                    return userRepository.save(user)
                            .flatMap(savedUser -> {
                                log.info("Created user with ID: {} for tutor registration", savedUser.getId());
                                
                                // Step 3: Assign TUTOR role to the user
                                return roleRepository.findByNom(com.iiil.tutoring.enums.UserRole.TUTOR)
                                        .flatMap(tutorRole -> {
                                            UserRole userRole = new UserRole(savedUser.getId(), tutorRole.getId());
                                            return userRoleRepository.save(userRole)
                                                    .then(Mono.just(savedUser));
                                        })
                                        .switchIfEmpty(Mono.error(new IllegalStateException("TUTOR role not found in database")))
                                        .flatMap(userWithRole -> {
                                            // Step 4: Create Tutor with the same ID as User
                                            Tutor tutor = new Tutor(request.getTarifHoraire());
                                            
                                            // Set the ID explicitly to match the user ID
                                            tutor.setId(savedUser.getId());
                                            
                                            // Set optional tutor-specific fields
                                            tutor.setExperience(request.getExperience());
                                            tutor.setDiplomes(request.getDiplomes());
                                            tutor.setDescription(request.getDescription());
                                            tutor.setVille(request.getVille());
                                            tutor.setPays(request.getPays());
                                            
                                            if (request.getCoursEnLigne() != null) {
                                                tutor.setCoursEnLigne(request.getCoursEnLigne());
                                            }
                                            if (request.getCoursPresentiel() != null) {
                                                tutor.setCoursPresentiel(request.getCoursPresentiel());
                                            }
                                            
                                            // Step 5: Save Tutor
                                            return tutorRepository.save(tutor)
                                                    .flatMap(savedTutor -> {
                                                        log.info("Created tutor with ID: {}", savedTutor.getId());
                                                        
                                                        // Step 6: Save tutor specialities
                                                        if (request.getSpecialiteIds() != null && !request.getSpecialiteIds().isEmpty()) {
                                                            return Flux.fromIterable(request.getSpecialiteIds())
                                                                    .flatMap(matiereId -> 
                                                                        // Validate that matiere exists
                                                                        matiereRepository.findById(matiereId)
                                                                            .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                                                                "Matière introuvable avec l'ID: " + matiereId)))
                                                                            .map(matiere -> new TutorSpecialite(savedTutor.getId(), matiereId))
                                                                    )
                                                                    .flatMap(tutorSpecialiteRepository::save)
                                                                    .then(Mono.just(savedTutor));
                                                        }
                                                        return Mono.just(savedTutor);
                                                    });
                                        });
                            });
                }))
                .doOnSuccess(tutor -> log.info("Successfully registered tutor with ID: {}", tutor.getId()))
                .doOnError(error -> log.error("Failed to register tutor: {}", error.getMessage(), error));
    }

    /**
     * Get tutor by ID
     */
    public Mono<Tutor> getTutorById(Long id) {
        log.debug("Fetching tutor with ID: {}", id);
        return tutorRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Tuteur non trouvé avec l'ID: " + id)));
    }

    /**
     * Get tutor profile response by ID
     */
    public Mono<TutorProfileResponse> getTutorProfile(Long id) {
        return getTutorById(id)
                .flatMap(tutor -> 
                    // Fetch the corresponding user
                    userRepository.findById(tutor.getId())
                            .switchIfEmpty(Mono.error(new IllegalStateException(
                                "User not found for tutor ID: " + id)))
                            .flatMap(user -> {
                                TutorProfileResponse response = new TutorProfileResponse(tutor, user);
                                return getTutorSpecialities(id)
                                        .collectList()
                                        .map(specialites -> {
                                            response.setSpecialites(specialites);
                                            return response;
                                        });
                            })
                );
    }

    /**
     * Update tutor profile
     */
    public Mono<TutorProfileResponse> updateTutorProfile(Long id, TutorProfileUpdateRequest request) {
        log.info("Updating tutor profile for ID: {}", id);
        
        return getTutorById(id)
                .flatMap(tutor -> 
                    updateTutorFromRequest(tutor, request)
                            .then(tutorRepository.save(tutor))
                            .flatMap(savedTutor -> {
                                // Update specialties if provided
                                if (request.getSpecialiteIds() != null) {
                                    return tutorSpecialiteRepository.deleteByTutorId(id)
                                            .then(Flux.fromIterable(request.getSpecialiteIds())
                                                    .map(matiereId -> new TutorSpecialite(id, matiereId))
                                                    .flatMap(tutorSpecialiteRepository::save)
                                                    .then(Mono.just(savedTutor)));
                                }
                                return Mono.just(savedTutor);
                            })
                )
                .flatMap(tutor -> userRepository.findById(tutor.getId())
                        .map(user -> new TutorProfileResponse(tutor, user)))
                .doOnSuccess(response -> log.info("Successfully updated tutor profile: {}", id))
                .doOnError(error -> log.error("Failed to update tutor profile {}: {}", id, error.getMessage()));
    }

    /**
     * Delete tutor (soft delete by setting status to INACTIVE in users table)
     */
    public Mono<Void> deleteTutor(Long id) {
        log.info("Soft deleting tutor with ID: {}", id);
        
        return getTutorById(id)
                .flatMap(tutor -> {
                    // Update user status in users table
                    return userRepository.findById(tutor.getId())
                            .flatMap(user -> {
                                user.setStatut(UserStatus.INACTIVE);
                                return userRepository.save(user);
                            })
                            .then(Mono.defer(() -> {
                                // Also set tutor as unavailable
                                tutor.setDisponible(false);
                                return tutorRepository.save(tutor);
                            }));
                })
                .then()
                .doOnSuccess(v -> log.info("Successfully deleted tutor: {}", id));
    }

    // === SEARCH AND FILTERING ===

    /**
     * Get all active tutors
     */
    public Flux<TutorSummaryResponse> getAllActiveTutors() {
        log.debug("Fetching all active tutors");
        return tutorRepository.findActiveTutors()
                .flatMap(this::createTutorSummaryResponse);
    }

    /**
     * Get available tutors for booking
     */
    public Flux<TutorSummaryResponse> getAvailableTutors() {
        log.debug("Fetching available tutors");
        return tutorRepository.findAvailableTutors()
                .flatMap(this::toTutorSummaryResponse);
    }

    /**
     * Get verified tutors
     */
    public Flux<TutorSummaryResponse> getVerifiedTutors() {
        log.debug("Fetching verified tutors");
        return tutorRepository.findVerifiedTutors()
                .flatMap(this::toTutorSummaryResponse);
    }

    /**
     * Search tutors by keyword
     */
    public Flux<TutorSummaryResponse> searchTutors(String keyword) {
        log.debug("Searching tutors with keyword: {}", keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActiveTutors();
        }
        return tutorRepository.searchByKeyword(keyword.trim())
                .flatMap(this::toTutorSummaryResponse);
    }

    /**
     * Advanced search with filters
     */
    public Flux<TutorSummaryResponse> searchTutorsWithFilters(TutorSearchRequest searchRequest) {
        log.debug("Advanced search with filters: {}", searchRequest);
        
        return tutorRepository.findWithFilters(
                searchRequest.getMatiereId(),
                searchRequest.getVille(),
                searchRequest.getMinTarif(),
                searchRequest.getMaxTarif(),
                searchRequest.getMinRating(),
                searchRequest.getVerifiedOnly(),
                searchRequest.getOnlineOnly(),
                searchRequest.getInPersonOnly()
        ).flatMap(this::toTutorSummaryResponse);
    }

    /**
     * Get tutors by specialty (matiere ID)
     */
    public Flux<TutorSummaryResponse> getTutorsBySpecialty(Long matiereId) {
        log.debug("Fetching tutors by matiere ID: {}", matiereId);
        return tutorRepository.findByMatiereId(matiereId)
                .flatMap(this::toTutorSummaryResponse);
    }

    /**
     * Get tutors by location
     */
    public Flux<TutorSummaryResponse> getTutorsByLocation(String ville) {
        log.debug("Fetching tutors by city: {}", ville);
        return tutorRepository.findByVille(ville)
                .flatMap(this::toTutorSummaryResponse);
    }

    /**
     * Get tutors by hourly rate range
     */
    public Flux<TutorSummaryResponse> getTutorsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Fetching tutors by price range: {} - {}", minPrice, maxPrice);
        return tutorRepository.findByTarifHoraireBetween(minPrice, maxPrice)
                .flatMap(this::toTutorSummaryResponse);
    }

    /**
     * Get top-rated tutors
     */
    public Flux<TutorSummaryResponse> getTopRatedTutors(int limit) {
        log.debug("Fetching top {} rated tutors", limit);
        return tutorRepository.getTopRatedTutors(limit)
                .flatMap(this::toTutorSummaryResponse);
    }

    // === ADMINISTRATION ===

    /**
     * Verify tutor profile (Admin only)
     */
    public Mono<TutorProfileResponse> verifyTutor(Long id) {
        log.info("Verifying tutor with ID: {}", id);
        
        return getTutorById(id)
                .flatMap(tutor -> {
                    tutor.verify();
                    return tutorRepository.save(tutor);
                })
                .flatMap(tutor -> userRepository.findById(tutor.getId())
                        .map(user -> new TutorProfileResponse(tutor, user)))
                .doOnSuccess(response -> log.info("Successfully verified tutor: {}", id));
    }

    /**
     * Unverify tutor profile (Admin only)
     */
    public Mono<TutorProfileResponse> unverifyTutor(Long id) {
        log.info("Unverifying tutor with ID: {}", id);
        
        return getTutorById(id)
                .flatMap(tutor -> {
                    tutor.unverify();
                    return tutorRepository.save(tutor);
                })
                .flatMap(tutor -> userRepository.findById(tutor.getId())
                        .map(user -> new TutorProfileResponse(tutor, user)))
                .doOnSuccess(response -> log.info("Successfully unverified tutor: {}", id));
    }

    /**
     * Update tutor availability
     */
    public Mono<TutorProfileResponse> updateTutorAvailability(Long id, boolean available) {
        log.info("Updating tutor availability for ID {}: {}", id, available);
        
        return getTutorById(id)
                .flatMap(tutor -> {
                    tutor.setAvailability(available);
                    return tutorRepository.save(tutor);
                })
                .flatMap(tutor -> userRepository.findById(tutor.getId())
                        .map(user -> new TutorProfileResponse(tutor, user)));
    }

    /**
     * Update tutor rating (called after new evaluation)
     */
    public Mono<TutorProfileResponse> updateTutorRating(Long id, BigDecimal newRating) {
        log.info("Updating tutor rating for ID {}: {}", id, newRating);
        
        return getTutorById(id)
                .flatMap(tutor -> {
                    tutor.updateRating(newRating);
                    return tutorRepository.save(tutor);
                })
                .flatMap(tutor -> userRepository.findById(tutor.getId())
                        .map(user -> new TutorProfileResponse(tutor, user)))
                .doOnSuccess(response -> log.info("Updated rating for tutor {}", id));
    }

    // === STATISTICS AND ANALYTICS ===

    /**
     * Get tutor statistics
     */
    public Mono<TutorStatisticsResponse> getTutorStatistics() {
        log.debug("Generating tutor statistics");
        
        return Mono.zip(
                tutorRepository.count(),
                tutorRepository.countActiveTutors(),
                tutorRepository.countAvailableTutors(),
                tutorRepository.countVerifiedTutors(),
                tutorRepository.getAverageRating().defaultIfEmpty(BigDecimal.ZERO),
                tutorRepository.getDistinctMatieres().collectList(),
                tutorRepository.getDistinctCities().collectList()
        ).map(tuple -> new TutorStatisticsResponse(
                tuple.getT1(), // total
                tuple.getT2(), // active
                tuple.getT3(), // available
                tuple.getT4(), // verified
                tuple.getT5(), // average rating
                tuple.getT6(), // specialties
                tuple.getT7(), // cities
                BigDecimal.ZERO, // average rate (to be calculated)
                0L // tutors with ratings (to be calculated)
        ));
    }

    /**
     * Get recent tutors (last N days)
     */
    public Flux<TutorSummaryResponse> getRecentTutors(int days) {
        log.debug("Fetching tutors registered in the last {} days", days);
        return tutorRepository.findRecentTutors(days)
                .flatMap(this::toTutorSummaryResponse);
    }

    // === UTILITY METHODS ===

    /**
     * Get tutor by email (searches in users table, then gets tutor)
     */
    public Mono<Tutor> getTutorByEmail(String email) {
        return userRepository.findByEmail(email)
                .flatMap(user -> tutorRepository.findById(user.getId()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Tuteur non trouvé avec l'email: " + email)));
    }

    // === TUTOR SPECIALTIES MANAGEMENT ===

    /**
     * Get all specialties for a tutor
     */
    public Flux<TutorSpecialiteDTO> getTutorSpecialities(Long tutorId) {
        log.debug("Fetching specialties for tutor ID: {}", tutorId);
        return tutorSpecialiteRepository.findByTutorId(tutorId)
                .flatMap(this::mapToSpecialiteDTO);
    }

    /**
     * Add specialty to tutor
     */
    public Mono<TutorSpecialiteDTO> addTutorSpeciality(Long tutorId, Long matiereId) {
        log.info("Adding specialty matière {} to tutor {}", matiereId, tutorId);
        
        return tutorSpecialiteRepository.existsByTutorIdAndMatiereId(tutorId, matiereId)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("Cette spécialité existe déjà pour ce tuteur"));
                    }
                    
                    TutorSpecialite specialite = new TutorSpecialite(tutorId, matiereId);
                    return tutorSpecialiteRepository.save(specialite)
                            .flatMap(this::mapToSpecialiteDTO);
                });
    }

    /**
     * Remove specialty from tutor
     */
    public Mono<Void> removeTutorSpeciality(Long tutorId, Long matiereId) {
        log.info("Removing specialty matière {} from tutor {}", matiereId, tutorId);
        
        return tutorSpecialiteRepository.findByTutorId(tutorId)
                .filter(specialite -> specialite.getMatiereId().equals(matiereId))
                .next()
                .flatMap(tutorSpecialiteRepository::delete)
                .then();
    }

    // === UTILITIES ===

    /**
     * Check if email is available for registration (checks users table)
     */
    public Mono<Void> checkEmailAvailability(String email) {
        return userRepository.existsByEmail(email)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("Un utilisateur existe déjà avec cet email: " + email));
                    }
                    return Mono.empty();
                });
    }

    /**
     * Update tutor entity from update request
     * Note: Basic user fields (nom, prenom, telephone) must be updated in users table
     */
    private Mono<Void> updateTutorFromRequest(Tutor tutor, TutorProfileUpdateRequest request) {
        // Update tutor-specific fields
        if (request.getExperience() != null) tutor.setExperience(request.getExperience());
        if (request.getTarifHoraire() != null) tutor.setTarifHoraire(request.getTarifHoraire());
        if (request.getDiplomes() != null) tutor.setDiplomes(request.getDiplomes());
        if (request.getDescription() != null) tutor.setDescription(request.getDescription());
        if (request.getVille() != null) tutor.setVille(request.getVille());
        if (request.getPays() != null) tutor.setPays(request.getPays());
        
        if (request.getCoursEnLigne() != null || request.getCoursPresentiel() != null) {
            boolean onlineValue = request.getCoursEnLigne() != null ? request.getCoursEnLigne() : tutor.getCoursEnLigne();
            boolean inPersonValue = request.getCoursPresentiel() != null ? request.getCoursPresentiel() : tutor.getCoursPresentiel();
            tutor.updateTeachingPreferences(onlineValue, inPersonValue);
        }
        
        // Update user fields in users table if provided
        if (request.getNom() != null || request.getPrenom() != null || request.getTelephone() != null) {
            return userRepository.findById(tutor.getId())
                    .flatMap(user -> {
                        if (request.getNom() != null) user.setNom(request.getNom());
                        if (request.getPrenom() != null) user.setPrenom(request.getPrenom());
                        if (request.getTelephone() != null) user.setTelephone(request.getTelephone());
                        return userRepository.save(user).then();
                    });
        }
        
        return Mono.empty();
    }

    /**
     * Map TutorSpecialite entity to DTO with matière information
     */
    private Mono<TutorSpecialiteDTO> mapToSpecialiteDTO(TutorSpecialite specialite) {
        return matiereRepository.findById(specialite.getMatiereId())
                .map(matiere -> new TutorSpecialiteDTO(
                        specialite.getId(),
                        specialite.getTutorId(),
                        specialite.getMatiereId(),
                        matiere.getNom(),
                        matiere.getDescription()
                ));
    }

    // === HELPER METHODS ===
    
    /**
     * Convert Tutor to TutorSummaryResponse (fetches user info)
     */
    private Mono<TutorSummaryResponse> toTutorSummaryResponse(com.iiil.tutoring.entity.Tutor tutor) {
        return userRepository.findById(tutor.getId())
                .map(user -> new TutorSummaryResponse(tutor, user));
    }

    /**
     * Create TutorSummaryResponse with specialties populated
     */
    private Mono<TutorSummaryResponse> createTutorSummaryResponse(com.iiil.tutoring.entity.Tutor tutor) {
        return userRepository.findById(tutor.getId())
                .flatMap(user -> {
                    TutorSummaryResponse response = new TutorSummaryResponse(tutor, user);
                    
                    return tutorSpecialiteRepository.findByTutorId(tutor.getId())
                            .flatMap(specialite -> matiereRepository.findById(specialite.getMatiereId())
                                    .map(matiere -> matiere.getNom()))
                            .collectList()
                            .map(specialiteNames -> {
                                response.setSpecialites(String.join(", ", specialiteNames));
                                return response;
                            });
                });
    }
}
