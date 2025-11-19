package com.iiil.tutoring.dto.auth;

import com.iiil.tutoring.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private UserStatus statut;
    private String photo;
    private LocalDateTime dateInscription;
    private List<String> roles;
    private String primaryRole;
    private String userType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Tutor-specific fields
    private String specialite;
    private Double tarifHoraire;
    private Double noteMoyenne;
    private Integer nombreEvaluations;
    private Boolean verifie;
    private Boolean disponible;
    private String ville;
}
