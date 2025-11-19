package com.iiil.tutoring.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for blocking/unblocking user requests
 */
@Schema(description = "Request to block or unblock a user")
public class UserStatusChangeRequest {

    @Schema(description = "Reason for status change", example = "Violation of terms of service")
    private String reason;

    @Schema(description = "Additional notes", example = "User reported for inappropriate behavior")
    private String notes;

    // Constructors
    public UserStatusChangeRequest() {}

    public UserStatusChangeRequest(String reason, String notes) {
        this.reason = reason;
        this.notes = notes;
    }

    // Getters and Setters
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}