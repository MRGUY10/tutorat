package com.iiil.tutoring.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Authentication response DTO
 */
@Data
@NoArgsConstructor
@Schema(description = "Authentication response containing JWT tokens and user information")
public class AuthResponse {

    @Schema(
            description = "JWT access token for API authentication",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            required = true
    )
    private String token;

    @Schema(
            description = "JWT refresh token for obtaining new access tokens",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            required = true
    )
    private String refreshToken;

    @Schema(
            description = "Token type (always Bearer)",
            example = "Bearer",
            defaultValue = "Bearer"
    )
    private String tokenType = "Bearer";

    @Schema(
            description = "Token expiration time in seconds",
            example = "3600",
            required = true
    )
    private Long expiresIn;

    @Schema(
            description = "User email address",
            example = "user@example.com",
            required = true
    )
    private String email;

    @Schema(
            description = "User role in the system",
            example = "STUDENT",
            allowableValues = {"STUDENT", "TUTOR", "ADMIN"},
            required = true
    )
    private String role;
    private Long userId;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;

    public AuthResponse(String token, String refreshToken, Long expiresIn, 
                       String email, String role, Long userId) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.email = email;
        this.role = role;
        this.userId = userId;
        this.issuedAt = LocalDateTime.now();
        this.expiresAt = this.issuedAt.plusSeconds(expiresIn / 1000);
    }
}