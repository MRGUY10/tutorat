package com.iiil.tutoring.enums;

/**
 * Priority level enumeration
 */
public enum Urgence {
    BASSE("basse"),
    NORMALE("normale"),
    MOYENNE("moyenne"),
    HAUTE("haute"),
    URGENTE("urgente");

    private final String value;

    Urgence(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}