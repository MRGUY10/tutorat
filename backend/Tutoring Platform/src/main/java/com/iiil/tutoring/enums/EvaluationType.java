package com.iiil.tutoring.enums;

/**
 * Evaluation type enumeration
 */
public enum EvaluationType {
    ETUDIANT_VERS_TUTEUR("etudiant_vers_tuteur"),
    TUTEUR_VERS_ETUDIANT("tuteur_vers_etudiant");

    private final String value;

    EvaluationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}