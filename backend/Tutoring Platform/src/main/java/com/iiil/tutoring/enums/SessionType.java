package com.iiil.tutoring.enums;

/**
 * Session type enumeration
 */
public enum SessionType {
    EN_LIGNE("en_ligne"),
    PRESENTIEL("presentiel");

    private final String value;

    SessionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}