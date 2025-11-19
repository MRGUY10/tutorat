package com.iiil.tutoring.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user statistics dashboard
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsDTO {
    
    private Long totalUsers;
    private Long activeUsers;
    private Long blockedUsers;
    private Long totalTutors;
    private Long totalStudents;
    
    // Additional computed fields
    public Double getActiveUsersPercentage() {
        if (totalUsers == null || totalUsers == 0) {
            return 0.0;
        }
        return (activeUsers * 100.0) / totalUsers;
    }
    
    public Double getBlockedUsersPercentage() {
        if (totalUsers == null || totalUsers == 0) {
            return 0.0;
        }
        return (blockedUsers * 100.0) / totalUsers;
    }
}