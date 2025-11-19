package com.iiil.tutoring.repository;

import com.iiil.tutoring.entity.Matiere;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository for Matiere entity
 */
public interface MatiereRepository extends R2dbcRepository<Matiere, Long> {

    Flux<Matiere> findByNiveau(String niveau);

    Flux<Matiere> findByDomaine(String domaine);

    @Query("SELECT * FROM matieres WHERE nom ILIKE :pattern OR description ILIKE :pattern")
    Flux<Matiere> findBySearchPattern(String pattern);

    @Query("SELECT * FROM matieres WHERE domaine = :domaine AND niveau = :niveau")
    Flux<Matiere> findByDomaineAndNiveau(String domaine, String niveau);

    @Query("SELECT DISTINCT domaine FROM matieres ORDER BY domaine")
    Flux<String> findAllDomaines();

    Mono<Matiere> findByNom(String nom);

    @Query("SELECT COUNT(*) FROM matieres WHERE domaine = :domaine")
    Mono<Long> countByDomaine(String domaine);
}