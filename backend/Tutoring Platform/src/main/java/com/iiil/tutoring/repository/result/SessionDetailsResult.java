package com.iiil.tutoring.repository.result;

import com.iiil.tutoring.enums.SessionStatus;
import com.iiil.tutoring.enums.SessionType;
import com.iiil.tutoring.enums.UserStatus;
import com.iiil.tutoring.enums.RequestStatus;

import java.time.LocalDateTime;

/**
 * Interface for session query results with complete details
 */
public interface SessionDetailsResult {
    
    // Session fields
    Long getId();
    Long getTuteurId();
    Long getEtudiantId();
    Long getMatiereId();
    Long getDemandeSessionId();
    LocalDateTime getDateHeure();
    Integer getDuree();
    SessionStatus getStatut();
    Double getPrix();
    SessionType getTypeSession();
    String getLienVisio();
    String getNotes();
    String getSalle();
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
    
    // Related request details (if applicable)
    RequestStatus getDemandeStatut();
    LocalDateTime getDemandeCreatedAt();
    String getDemandeMessage();
}