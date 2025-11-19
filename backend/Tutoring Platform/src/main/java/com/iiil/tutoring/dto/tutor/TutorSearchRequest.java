package com.iiil.tutoring.dto.tutor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for tutor search requests with filters
 */
@Data
@NoArgsConstructor
public class TutorSearchRequest {

    private String keyword;
    private Long matiereId;  // Changed from specialite (String) to matiereId (Long)
    private String ville;
    private String pays;
    
    private BigDecimal minTarif;
    private BigDecimal maxTarif;
    private BigDecimal minRating;
    private Integer minEvaluations;
    
    private Boolean verifiedOnly;
    private Boolean availableOnly;
    private Boolean onlineOnly;
    private Boolean inPersonOnly;
    
    // Sorting options
    private String sortBy = "rating"; // rating, price, name, date
    private String sortDirection = "desc"; // asc, desc
    
    // Pagination
    private Integer page = 0;
    private Integer size = 20;
}