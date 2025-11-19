package com.iiil.tutoring.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for user status change response
 */
@Schema(description = "Response for user status change operations")
public class UserStatusChangeResponse {

    @Schema(description = "Success status", example = "true")
    private boolean success;

    @Schema(description = "Response message", example = "User has been blocked successfully")
    private String message;

    @Schema(description = "User ID", example = "123")
    private Long userId;

    @Schema(description = "New status", example = "SUSPENDED")
    private String newStatus;

    @Schema(description = "Reason for change", example = "Violation of terms")
    private String reason;

    // Constructors
    public UserStatusChangeResponse() {}

    public UserStatusChangeResponse(boolean success, String message, Long userId, String newStatus, String reason) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.newStatus = newStatus;
        this.reason = reason;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}