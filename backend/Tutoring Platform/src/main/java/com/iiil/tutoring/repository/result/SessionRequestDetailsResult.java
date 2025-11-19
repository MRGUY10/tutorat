package com.iiil.tutoring.repository.result;

import com.iiil.tutoring.enums.RequestStatus;
import com.iiil.tutoring.enums.Urgence;
import com.iiil.tutoring.enums.UserStatus;

import java.time.LocalDateTime;

/**
 * Interface for session request query results with user details
 */
public interface SessionRequestDetailsResult {
    
    // Session request fields
    Long getId();
    Long getEtudiantId();
    Long getTuteurId();
    Long getMatiereId();
    LocalDateTime getDateCreation();
    LocalDateTime getDateVoulue();
    String getMessage();
    RequestStatus getStatut();
    Urgence getUrgence();
    Integer getDureeSouhaitee();
    Double getBudgetMax();
    String getReponseTuteur();
    LocalDateTime getDateReponse();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    
    // Student details
    String getEtudiantNom();
    String getEtudiantPrenom();
    String getEtudiantEmail();
    String getEtudiantTelephone();
    UserStatus getEtudiantStatus();
    
    // Tutor details
    String getTuteurNom();
    String getTuteurPrenom();
    String getTuteurEmail();
    String getTuteurTelephone();
    UserStatus getTuteurStatus();
    String getTuteurSpecialite();
    Double getTuteurTarifHoraire();
    Boolean getTuteurVerified();
    
    // Subject details
    String getMatiereNom();
    String getMatiereDescription();
    
    // Related session (if exists)
    Long getSessionId();
    LocalDateTime getSessionDateHeure();
    String getSessionStatut();
}