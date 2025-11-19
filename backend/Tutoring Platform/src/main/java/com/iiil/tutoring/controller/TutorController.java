package com.iiil.tutoring.controller;

import com.iiil.tutoring.dto.tutor.*;
import com.iiil.tutoring.service.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * REST Controller for Tutor Management
 * Provides comprehensive tutor CRUD operations, search, and administration
 */
@RestController
@RequestMapping("/api/tutors")
@Tag(name = "Tutor Management", description = "Comprehensive tutor management operations")
@Slf4j
public class TutorController {

    @Autowired
    private TutorService tutorService;

    // === PROFILE OPERATIONS ===

    /**
     * Get tutor profile by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get tutor profile", description = "Retrieve detailed tutor profile by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tutor found"),
            @ApiResponse(responseCode = "404", description = "Tutor not found")
    })
    public Mono<ResponseEntity<TutorProfileResponse>> getTutorProfile(
            @Parameter(description = "Tutor ID") @PathVariable Long id) {
        return tutorService.getTutorProfile(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .doOnNext(response -> log.debug("Retrieved tutor profile: {}", id));
    }

    /**
     * Update tutor profile
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update tutor profile", description = "Update tutor profile information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "404", description = "Tutor not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public Mono<ResponseEntity<TutorProfileResponse>> updateTutorProfile(
            @Parameter(description = "Tutor ID") @PathVariable Long id,
            @Valid @RequestBody TutorProfileUpdateRequest request) {
        return tutorService.updateTutorProfile(id, request)
                .map(ResponseEntity::ok)
                .onErrorReturn(IllegalArgumentException.class, ResponseEntity.notFound().build())
                .doOnNext(response -> log.info("Updated tutor profile: {}", id));
    }

    /**
     * Delete tutor (soft delete)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete tutor", description = "Soft delete tutor (set as inactive)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tutor deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Tutor not found")
    })
    public Mono<ResponseEntity<Void>> deleteTutor(
            @Parameter(description = "Tutor ID") @PathVariable Long id) {
        return tutorService.deleteTutor(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorReturn(IllegalArgumentException.class, ResponseEntity.notFound().build())
                .doOnNext(response -> log.info("Deleted tutor: {}", id));
    }

    // === LISTING AND SEARCH OPERATIONS ===

    /**
     * Get all active tutors
     */
    @GetMapping
    @Operation(summary = "Get all active tutors", description = "Retrieve all active tutors")
    public Flux<TutorSummaryResponse> getAllActiveTutors() {
        log.debug("Fetching all active tutors");
        return tutorService.getAllActiveTutors();
    }

    /**
     * Get available tutors for booking
     */
    @GetMapping("/available")
    @Operation(summary = "Get available tutors", description = "Get tutors available for booking")
    public Flux<TutorSummaryResponse> getAvailableTutors() {
        log.debug("Fetching available tutors");
        return tutorService.getAvailableTutors();
    }

    /**
     * Get verified tutors
     */
    @GetMapping("/verified")
    @Operation(summary = "Get verified tutors", description = "Get all verified tutors")
    public Flux<TutorSummaryResponse> getVerifiedTutors() {
        log.debug("Fetching verified tutors");
        return tutorService.getVerifiedTutors();
    }

    /**
     * Get top-rated tutors
     */
    @GetMapping("/top-rated")
    @Operation(summary = "Get top-rated tutors", description = "Get highest rated tutors")
    public Flux<TutorSummaryResponse> getTopRatedTutors(
            @Parameter(description = "Maximum number of tutors to return") 
            @RequestParam(defaultValue = "10") int limit) {
        log.debug("Fetching top {} rated tutors", limit);
        return tutorService.getTopRatedTutors(limit);
    }

    /**
     * Search tutors by keyword
     */
    @GetMapping("/search")
    @Operation(summary = "Search tutors", description = "Search tutors by keyword in name, specialty, or description")
    public Flux<TutorSummaryResponse> searchTutors(
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword) {
        log.debug("Searching tutors with keyword: {}", keyword);
        return tutorService.searchTutors(keyword);
    }

    /**
     * Advanced search with filters
     */
    @PostMapping("/search")
    @Operation(summary = "Advanced tutor search", description = "Search tutors with advanced filters")
    @ApiResponse(responseCode = "200", description = "Search results")
    public Flux<TutorSummaryResponse> searchTutorsWithFilters(
            @Valid @RequestBody TutorSearchRequest searchRequest) {
        log.debug("Advanced search with filters: {}", searchRequest);
        return tutorService.searchTutorsWithFilters(searchRequest);
    }

    /**
     * Get tutors by specialty (matiere)
     */
    @GetMapping("/specialty/{matiereId}")
    @Operation(summary = "Get tutors by specialty", description = "Get tutors filtered by matiere ID")
    public Flux<TutorSummaryResponse> getTutorsBySpecialty(
            @Parameter(description = "Matiere ID") @PathVariable Long matiereId) {
        log.debug("Fetching tutors by matiere ID: {}", matiereId);
        return tutorService.getTutorsBySpecialty(matiereId);
    }

    /**
     * Get tutors by location
     */
    @GetMapping("/location/{ville}")
    @Operation(summary = "Get tutors by location", description = "Get tutors in a specific city")
    public Flux<TutorSummaryResponse> getTutorsByLocation(
            @Parameter(description = "City name") @PathVariable String ville) {
        log.debug("Fetching tutors by city: {}", ville);
        return tutorService.getTutorsByLocation(ville);
    }

    /**
     * Get tutors by price range
     */
    @GetMapping("/price-range")
    @Operation(summary = "Get tutors by price range", description = "Get tutors within specified price range")
    public Flux<TutorSummaryResponse> getTutorsByPriceRange(
            @Parameter(description = "Minimum hourly rate") @RequestParam BigDecimal minPrice,
            @Parameter(description = "Maximum hourly rate") @RequestParam BigDecimal maxPrice) {
        log.debug("Fetching tutors by price range: {} - {}", minPrice, maxPrice);
        return tutorService.getTutorsByPriceRange(minPrice, maxPrice);
    }

    // === ADMINISTRATION OPERATIONS ===

    /**
     * Verify tutor profile (Admin only)
     */
    @PutMapping("/{id}/verify")
    @Operation(summary = "Verify tutor", description = "Verify tutor profile (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tutor verified successfully"),
            @ApiResponse(responseCode = "404", description = "Tutor not found"),
            @ApiResponse(responseCode = "403", description = "Access forbidden - Admin only")
    })
    public Mono<ResponseEntity<TutorProfileResponse>> verifyTutor(
            @Parameter(description = "Tutor ID") @PathVariable Long id) {
        return tutorService.verifyTutor(id)
                .map(ResponseEntity::ok)
                .onErrorReturn(IllegalArgumentException.class, ResponseEntity.notFound().build())
                .doOnNext(response -> log.info("Verified tutor: {}", id));
    }

    /**
     * Unverify tutor profile (Admin only)
     */
    @PutMapping("/{id}/unverify")
    @Operation(summary = "Unverify tutor", description = "Remove verification from tutor profile (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tutor unverified successfully"),
            @ApiResponse(responseCode = "404", description = "Tutor not found"),
            @ApiResponse(responseCode = "403", description = "Access forbidden - Admin only")
    })
    public Mono<ResponseEntity<TutorProfileResponse>> unverifyTutor(
            @Parameter(description = "Tutor ID") @PathVariable Long id) {
        return tutorService.unverifyTutor(id)
                .map(ResponseEntity::ok)
                .onErrorReturn(IllegalArgumentException.class, ResponseEntity.notFound().build())
                .doOnNext(response -> log.info("Unverified tutor: {}", id));
    }

    /**
     * Update tutor availability
     */
    @PutMapping("/{id}/availability")
    @Operation(summary = "Update availability", description = "Update tutor availability status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability updated successfully"),
            @ApiResponse(responseCode = "404", description = "Tutor not found")
    })
    public Mono<ResponseEntity<TutorProfileResponse>> updateTutorAvailability(
            @Parameter(description = "Tutor ID") @PathVariable Long id,
            @Parameter(description = "Availability status") @RequestParam Boolean available) {
        return tutorService.updateTutorAvailability(id, available)
                .map(ResponseEntity::ok)
                .onErrorReturn(IllegalArgumentException.class, ResponseEntity.notFound().build())
                .doOnNext(response -> log.info("Updated availability for tutor {}: {}", id, available));
    }

    /**
     * Update tutor rating (called after evaluation)
     */
    @PutMapping("/{id}/rating")
    @Operation(summary = "Update tutor rating", description = "Update tutor rating after new evaluation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rating updated successfully"),
            @ApiResponse(responseCode = "404", description = "Tutor not found"),
            @ApiResponse(responseCode = "400", description = "Invalid rating value")
    })
    public Mono<ResponseEntity<TutorProfileResponse>> updateTutorRating(
            @Parameter(description = "Tutor ID") @PathVariable Long id,
            @Parameter(description = "New rating (0-5)") @RequestParam BigDecimal rating) {
        return tutorService.updateTutorRating(id, rating)
                .map(ResponseEntity::ok)
                .onErrorReturn(IllegalArgumentException.class, ResponseEntity.badRequest().build())
                .doOnNext(response -> log.info("Updated rating for tutor {}: {}", id, rating));
    }

    // === STATISTICS AND ANALYTICS ===

    /**
     * Get tutor statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get tutor statistics", description = "Get comprehensive tutor platform statistics")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully", 
                 content = @Content(schema = @Schema(implementation = TutorStatisticsResponse.class)))
    public Mono<ResponseEntity<TutorStatisticsResponse>> getTutorStatistics() {
        log.debug("Generating tutor statistics");
        return tutorService.getTutorStatistics()
                .map(ResponseEntity::ok);
    }

    /**
     * Get recent tutors
     */
    @GetMapping("/recent")
    @Operation(summary = "Get recent tutors", description = "Get tutors registered in the last N days")
    public Flux<TutorSummaryResponse> getRecentTutors(
            @Parameter(description = "Number of days") @RequestParam(defaultValue = "30") int days) {
        log.debug("Fetching tutors registered in the last {} days", days);
        return tutorService.getRecentTutors(days);
    }

    // === ERROR HANDLING ===

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("INVALID_ARGUMENT", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unexpected error in tutor controller: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "Une erreur inattendue s'est produite"));
    }

    // === TUTOR SPECIALTY ENDPOINTS ===

    /**
     * Get all specialties for a tutor
     */
    @GetMapping("/{id}/specialties")
    @Operation(summary = "Get tutor specialties", description = "Get all specialties for a specific tutor")
    @ApiResponse(responseCode = "200", description = "List of tutor specialties")
    public Flux<TutorSpecialiteDTO> getTutorSpecialties(
            @Parameter(description = "Tutor ID") @PathVariable Long id) {
        
        log.info("Getting specialties for tutor ID: {}", id);
        return tutorService.getTutorSpecialities(id);
    }

    /**
     * Add specialty to tutor
     */
    @PostMapping("/{id}/specialties/{matiereId}")
    @Operation(summary = "Add specialty to tutor", description = "Add a new specialty (matière) to a tutor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Specialty added successfully"),
            @ApiResponse(responseCode = "400", description = "Specialty already exists or invalid data"),
            @ApiResponse(responseCode = "404", description = "Tutor or matière not found")
    })
    public Mono<ResponseEntity<TutorSpecialiteDTO>> addTutorSpecialty(
            @Parameter(description = "Tutor ID") @PathVariable Long id,
            @Parameter(description = "Matière ID") @PathVariable Long matiereId) {
        
        log.info("Adding specialty matière {} to tutor {}", matiereId, id);
        
        return tutorService.addTutorSpeciality(id, matiereId)
                .map(specialty -> ResponseEntity.status(HttpStatus.CREATED).body(specialty))
                .onErrorResume(IllegalArgumentException.class, error ->
                        Mono.just(ResponseEntity.badRequest().build()))
                .onErrorResume(error -> {
                    log.error("Error adding specialty to tutor: {}", error.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    /**
     * Remove specialty from tutor
     */
    @DeleteMapping("/{id}/specialties/{matiereId}")
    @Operation(summary = "Remove specialty from tutor", description = "Remove a specialty (matière) from a tutor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Specialty removed successfully"),
            @ApiResponse(responseCode = "404", description = "Tutor or specialty not found")
    })
    public Mono<ResponseEntity<Void>> removeTutorSpecialty(
            @Parameter(description = "Tutor ID") @PathVariable Long id,
            @Parameter(description = "Matière ID") @PathVariable Long matiereId) {
        
        log.info("Removing specialty matière {} from tutor {}", matiereId, id);
        
        return tutorService.removeTutorSpeciality(id, matiereId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(error -> {
                    log.error("Error removing specialty from tutor: {}", error.getMessage());
                    return Mono.just(ResponseEntity.notFound().build());
                });
    }

    // Error response DTO
    private static class ErrorResponse {
        private final String code;
        private final String message;

        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() { return code; }
        public String getMessage() { return message; }
    }
}