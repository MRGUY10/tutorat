package com.iiil.tutoring.repository;

import com.iiil.tutoring.entity.Etudiant;
import com.iiil.tutoring.enums.NiveauAcademique;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository for Etudiant entity
 */
public interface EtudiantRepository extends R2dbcRepository<Etudiant, Long> {

       @Modifying
       @Query("INSERT INTO etudiants (id, filiere, annee, niveau) VALUES (:id, :filiere, :annee, :niveau)")
       Mono<Void> insertEtudiant(Long id, String filiere, int annee, String niveau);

    @Query("SELECT e.* FROM etudiants e INNER JOIN users u ON e.id = u.id WHERE e.filiere = :filiere AND u.statut = 'active'")
    Flux<Etudiant> findByFiliere(String filiere);

    @Query("SELECT e.* FROM etudiants e INNER JOIN users u ON e.id = u.id WHERE e.annee = :annee AND u.statut = 'active'")
    Flux<Etudiant> findByAnnee(int annee);

    @Query("SELECT e.* FROM etudiants e INNER JOIN users u ON e.id = u.id WHERE e.niveau = :niveau AND u.statut = 'active'")
    Flux<Etudiant> findByNiveau(String niveau);

    @Query("SELECT e.* FROM etudiants e INNER JOIN users u ON e.id = u.id WHERE e.filiere = :filiere AND e.annee = :annee AND u.statut = 'active'")
    Flux<Etudiant> findByFiliereAndAnnee(String filiere, int annee);

    @Query("SELECT e.* FROM etudiants e INNER JOIN users u ON e.id = u.id WHERE e.niveau = :niveau AND e.filiere ILIKE :filiere AND u.statut = 'active'")
    Flux<Etudiant> findByNiveauAndFiliereContaining(String niveau, String filiere);

    @Query("SELECT COUNT(*) FROM etudiants e INNER JOIN users u ON e.id = u.id WHERE e.filiere = :filiere AND u.statut = 'active'")
    Mono<Long> countByFiliere(String filiere);

    @Query("""
           SELECT e.id, e.filiere, e.annee, e.niveau,
                  u.nom, u.prenom, u.email, u.telephone, u.date_inscription, 
                  u.statut, u.photo, u.created_at, u.updated_at, u.version
           FROM etudiants e 
           INNER JOIN users u ON e.id = u.id 
           WHERE u.statut = 'active' 
           ORDER BY u.nom, u.prenom
           """)
    Flux<Etudiant> findAllActiveWithUserInfo();
}