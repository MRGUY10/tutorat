package com.iiil.tutoring.dto.tutor;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for tutor statistics and analytics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutorStatisticsResponse {

    private Long totalTutors;
    private Long activeTutors;
    private Long availableTutors;
    private Long verifiedTutors;
    private BigDecimal averageRating;
    private List<String> popularSpecialties;
    private List<String> popularCities;
    private BigDecimal averageHourlyRate;
    private Long tutorsWithRatings;
}