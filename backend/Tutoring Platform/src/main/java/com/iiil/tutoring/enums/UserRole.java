package com.iiil.tutoring.enums;

/**
 * User role enumeration
 */
public enum UserRole {
    STUDENT("STUDENT"),
    TUTOR("TUTOR"),
    ADMIN("ADMIN");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}