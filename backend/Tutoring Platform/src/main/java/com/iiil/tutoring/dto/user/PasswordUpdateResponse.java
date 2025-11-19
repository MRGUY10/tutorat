package com.iiil.tutoring.dto.user;

import java.time.LocalDateTime;

/**
 * DTO for password update response
 */
public class PasswordUpdateResponse {

    private String message;
    private boolean success;
    private LocalDateTime updatedAt;

    // Constructors
    public PasswordUpdateResponse() {}

    public PasswordUpdateResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
        this.updatedAt = LocalDateTime.now();
    }

    public PasswordUpdateResponse(String message, boolean success, LocalDateTime updatedAt) {
        this.message = message;
        this.success = success;
        this.updatedAt = updatedAt;
    }

    // Static factory methods for common responses
    public static PasswordUpdateResponse success() {
        return new PasswordUpdateResponse("Mot de passe mis à jour avec succès", true);
    }

    public static PasswordUpdateResponse success(String message) {
        return new PasswordUpdateResponse(message, true);
    }

    public static PasswordUpdateResponse failure(String message) {
        return new PasswordUpdateResponse(message, false);
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "PasswordUpdateResponse{" +
                "message='" + message + '\'' +
                ", success=" + success +
                ", updatedAt=" + updatedAt +
                '}';
    }
}