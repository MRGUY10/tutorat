package com.iiil.tutoring.enums;

/**
 * Academic level enumeration
 */
public enum NiveauAcademique {
    DEBUTANT("debutant"),
    INTERMEDIAIRE("intermediaire"),
    AVANCE("avance");

    private final String value;

    NiveauAcademique(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}