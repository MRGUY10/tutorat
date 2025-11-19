package com.iiil.tutoring.enums;

/**
 * Notification type enumeration
 */
public enum NotificationType {
    SESSION("session"),
    MESSAGE("message"),
    PAIEMENT("paiement"),
    SYSTEME("systeme");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}