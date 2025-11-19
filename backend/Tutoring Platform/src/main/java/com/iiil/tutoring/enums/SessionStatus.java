package com.iiil.tutoring.enums;

/**
 * Session status enumeration
 */
public enum SessionStatus {
    DEMANDEE("demandee"),
    CONFIRMEE("confirmee"),
    EN_COURS("en_cours"),
    TERMINEE("terminee"),
    ANNULEE("annulee");

    private final String value;

    SessionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}