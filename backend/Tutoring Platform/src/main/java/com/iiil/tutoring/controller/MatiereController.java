package com.iiil.tutoring.controller;

import com.iiil.tutoring.dto.matiere.*;
import com.iiil.tutoring.entity.Matiere;
import com.iiil.tutoring.enums.NiveauAcademique;
import com.iiil.tutoring.service.MatiereService;
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

/**
 * Controller for managing subjects (matieres)
 */
@RestController
@RequestMapping("/api/matieres")
@Tag(name = "Subject Management", description = "APIs for managing academic subjects")
@Slf4j
public class MatiereController {

    @Autowired
    private MatiereService matiereService;

    /**
     * Create a new subject
     */
    @PostMapping
    @Operation(summary = "Create a new subject", description = "Create a new academic subject")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Subject created successfully",
                    content = @Content(schema = @Schema(implementation = MatiereResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Subject name already exists")
    })
    public Mono<ResponseEntity<MatiereResponse>> createMatiere(
            @Valid @RequestBody CreateMatiereRequest request) {
        
        log.info("Creating new subject: {}", request.getNom());
        
        Matiere matiere = new Matiere();
        matiere.setNom(request.getNom());
        matiere.setDescription(request.getDescription());
        matiere.setNiveauEnum(request.getNiveau()); // Use enum setter
        matiere.setDomaine(request.getDomaine());

        return matiereService.createMatiere(matiere)
                .map(savedMatiere -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(mapToResponse(savedMatiere)))
                .onErrorReturn(IllegalArgumentException.class, 
                        ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    /**
     * Get subject by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get subject by ID", description = "Retrieve a subject by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subject found",
                    content = @Content(schema = @Schema(implementation = MatiereResponse.class))),
            @ApiResponse(responseCode = "404", description = "Subject not found")
    })
    public Mono<ResponseEntity<MatiereResponse>> getMatiereById(
            @Parameter(description = "Subject ID") @PathVariable Long id) {
        
        log.info("Getting subject by ID: {}", id);
        
        return matiereService.getMatiereById(id)
                .map(matiere -> ResponseEntity.ok(mapToResponse(matiere)))
                .onErrorReturn(IllegalArgumentException.class, 
                        ResponseEntity.notFound().build());
    }

    /**
     * Get all subjects
     */
    @GetMapping
    @Operation(summary = "Get all subjects", description = "Retrieve all subjects with optional pagination")
    @ApiResponse(responseCode = "200", description = "List of subjects")
    public Flux<MatiereResponse> getAllMatieres(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting all subjects - page: {}, size: {}", page, size);
        
        if (page >= 0 && size > 0) {
            return matiereService.getMatieresPaginated(page, size)
                    .map(this::mapToResponse);
        } else {
            return matiereService.getAllMatieres()
                    .map(this::mapToResponse);
        }
    }

    /**
     * Update subject
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update subject", description = "Update an existing subject")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subject updated successfully",
                    content = @Content(schema = @Schema(implementation = MatiereResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Subject not found"),
            @ApiResponse(responseCode = "409", description = "Subject name already exists")
    })
    public Mono<ResponseEntity<MatiereResponse>> updateMatiere(
            @Parameter(description = "Subject ID") @PathVariable Long id,
            @Valid @RequestBody UpdateMatiereRequest request) {
        
        log.info("Updating subject ID: {} with data: {}", id, request.getNom());
        
        Matiere matiere = new Matiere();
        matiere.setNom(request.getNom());
        matiere.setDescription(request.getDescription());
        if (request.getNiveau() != null) {
            matiere.setNiveauEnum(request.getNiveau());
        }
        matiere.setDomaine(request.getDomaine());

        return matiereService.updateMatiere(id, matiere)
                .map(updatedMatiere -> ResponseEntity.ok(mapToResponse(updatedMatiere)))
                .onErrorReturn(IllegalArgumentException.class, 
                        ResponseEntity.notFound().build());
    }

    /**
     * Delete subject
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete subject", description = "Delete a subject by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Subject deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Subject not found")
    })
    public Mono<ResponseEntity<Void>> deleteMatiere(
            @Parameter(description = "Subject ID") @PathVariable Long id) {
        
        log.info("Deleting subject ID: {}", id);
        
        return matiereService.deleteMatiere(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorReturn(IllegalArgumentException.class, 
                        ResponseEntity.notFound().build());
    }

    /**
     * Search subjects
     */
    @GetMapping("/search")
    @Operation(summary = "Search subjects", description = "Search subjects by name or description")
    @ApiResponse(responseCode = "200", description = "Search results")
    public Flux<MatiereResponse> searchMatieres(
            @Parameter(description = "Search term") @RequestParam String searchTerm) {
        
        log.info("Searching subjects with term: {}", searchTerm);
        
        return matiereService.searchMatieres(searchTerm)
                .map(this::mapToResponse);
    }

    /**
     * Get subjects by domain
     */
    @GetMapping("/domain/{domaine}")
    @Operation(summary = "Get subjects by domain", description = "Retrieve subjects by academic domain")
    @ApiResponse(responseCode = "200", description = "Subjects in the domain")
    public Flux<MatiereResponse> getMatieresByDomaine(
            @Parameter(description = "Domain name") @PathVariable String domaine) {
        
        log.info("Getting subjects by domain: {}", domaine);
        
        return matiereService.getMatieresByDomaine(domaine)
                .map(this::mapToResponse);
    }

    /**
     * Get subjects by academic level
     */
    @GetMapping("/level/{niveau}")
    @Operation(summary = "Get subjects by level", description = "Retrieve subjects by academic level")
    @ApiResponse(responseCode = "200", description = "Subjects at the level")
    public Flux<MatiereResponse> getMatieresByNiveau(
            @Parameter(description = "Academic level") @PathVariable NiveauAcademique niveau) {
        
        log.info("Getting subjects by level: {}", niveau);
        
        return matiereService.getMatieresByNiveau(niveau.getValue())
                .map(this::mapToResponse);
    }

    /**
     * Get all domains
     */
    @GetMapping("/domains")
    @Operation(summary = "Get all domains", description = "Retrieve all available academic domains")
    @ApiResponse(responseCode = "200", description = "List of domains")
    public Flux<String> getAllDomaines() {
        log.info("Getting all domains");
        return matiereService.getAllDomaines();
    }

    /**
     * Count subjects by domain
     */
    @GetMapping("/domain/{domaine}/count")
    @Operation(summary = "Count subjects by domain", description = "Count the number of subjects in a domain")
    @ApiResponse(responseCode = "200", description = "Subject count")
    public Mono<Long> countMatieresByDomaine(
            @Parameter(description = "Domain name") @PathVariable String domaine) {
        
        log.info("Counting subjects in domain: {}", domaine);
        return matiereService.countMatieresByDomaine(domaine);
    }

    /**
     * Check if subject exists by name
     */
    @GetMapping("/exists")
    @Operation(summary = "Check subject existence", description = "Check if a subject exists by name")
    @ApiResponse(responseCode = "200", description = "Existence status")
    public Mono<Boolean> existsByNom(
            @Parameter(description = "Subject name") @RequestParam String nom) {
        
        log.info("Checking existence of subject: {}", nom);
        return matiereService.existsByNom(nom);
    }

    /**
     * Map Matiere entity to response DTO
     */
    private MatiereResponse mapToResponse(Matiere matiere) {
        return new MatiereResponse(
                matiere.getId(),
                matiere.getNom(),
                matiere.getDescription(),
                matiere.getNiveauEnum(), // Use enum getter
                matiere.getDomaine(),
                matiere.getCreatedAt(),
                matiere.getUpdatedAt(),
                matiere.getVersion()
        );
    }
}