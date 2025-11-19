package com.iiil.tutoring.enums;

/**
 * Request status enumeration
 */
public enum RequestStatus {
    EN_ATTENTE("en_attente"),
    ACCEPTEE("acceptee"),
    REFUSEE("refusee");

    private final String value;

    RequestStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}