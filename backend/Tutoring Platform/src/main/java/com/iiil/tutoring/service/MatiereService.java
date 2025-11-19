package com.iiil.tutoring.service;

import com.iiil.tutoring.entity.Matiere;
import com.iiil.tutoring.repository.MatiereRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service for managing subjects (matieres)
 */
@Service
@Transactional
public class MatiereService {

    @Autowired
    private MatiereRepository matiereRepository;

    /**
     * Create a new subject
     */
    public Mono<Matiere> createMatiere(Matiere matiere) {
        // Validate subject name uniqueness
        return matiereRepository.findByNom(matiere.getNom())
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("Une matière avec ce nom existe déjà"));
                    }
                    return matiereRepository.save(matiere);
                });
    }

    /**
     * Get subject by ID
     */
    public Mono<Matiere> getMatiereById(Long id) {
        return matiereRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Matière non trouvée avec l'ID: " + id)));
    }

    /**
     * Get all subjects
     */
    public Flux<Matiere> getAllMatieres() {
        return matiereRepository.findAll();
    }

    /**
     * Update a subject
     */
    public Mono<Matiere> updateMatiere(Long id, Matiere matiere) {
        return matiereRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Matière non trouvée avec l'ID: " + id)))
                .flatMap(existingMatiere -> {
                    // Check if name is being changed and if new name already exists
                    if (!existingMatiere.getNom().equals(matiere.getNom())) {
                        return matiereRepository.findByNom(matiere.getNom())
                                .hasElement()
                                .flatMap(nameExists -> {
                                    if (nameExists) {
                                        return Mono.error(new IllegalArgumentException("Une matière avec ce nom existe déjà"));
                                    }
                                    return updateMatiereFields(existingMatiere, matiere);
                                });
                    } else {
                        return updateMatiereFields(existingMatiere, matiere);
                    }
                });
    }

    /**
     * Delete a subject
     */
    public Mono<Void> deleteMatiere(Long id) {
        return matiereRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Matière non trouvée avec l'ID: " + id)))
                .flatMap(matiere -> matiereRepository.delete(matiere));
    }

    /**
     * Search subjects by pattern (name or description)
     */
    public Flux<Matiere> searchMatieres(String searchTerm) {
        String pattern = "%" + searchTerm.toLowerCase() + "%";
        return matiereRepository.findBySearchPattern(pattern);
    }

    /**
     * Get subjects by domain
     */
    public Flux<Matiere> getMatieresByDomaine(String domaine) {
        return matiereRepository.findByDomaine(domaine);
    }

    /**
     * Get subjects by academic level
     */
    public Flux<Matiere> getMatieresByNiveau(String niveau) {
        return matiereRepository.findByNiveau(niveau);
    }

    /**
     * Get subjects by domain and level
     */
    public Flux<Matiere> getMatieresByDomaineAndNiveau(String domaine, String niveau) {
        return matiereRepository.findByDomaineAndNiveau(domaine, niveau);
    }

    /**
     * Get all available domains
     */
    public Flux<String> getAllDomaines() {
        return matiereRepository.findAllDomaines();
    }

    /**
     * Count subjects by domain
     */
    public Mono<Long> countMatieresByDomaine(String domaine) {
        return matiereRepository.countByDomaine(domaine);
    }

    /**
     * Check if subject exists by name
     */
    public Mono<Boolean> existsByNom(String nom) {
        return matiereRepository.findByNom(nom).hasElement();
    }

    /**
     * Helper method to update matiere fields
     */
    private Mono<Matiere> updateMatiereFields(Matiere existing, Matiere updated) {
        existing.setNom(updated.getNom());
        existing.setDescription(updated.getDescription());
        existing.setNiveau(updated.getNiveau());
        existing.setDomaine(updated.getDomaine());
        
        return matiereRepository.save(existing);
    }

    /**
     * Bulk create subjects
     */
    public Flux<Matiere> createMatieres(Flux<Matiere> matieres) {
        return matieres
                .flatMap(matiere -> createMatiere(matiere)
                        .onErrorResume(error -> {
                            // Log error and continue with next matiere
                            System.err.println("Erreur lors de la création de la matière: " + matiere.getNom() + " - " + error.getMessage());
                            return Mono.empty();
                        }));
    }

    /**
     * Get subjects with pagination
     */
    public Flux<Matiere> getMatieresPaginated(int page, int size) {
        return matiereRepository.findAll()
                .skip((long) page * size)
                .take(size);
    }

    /**
     * Count total subjects
     */
    public Mono<Long> countAllMatieres() {
        return matiereRepository.count();
    }
}