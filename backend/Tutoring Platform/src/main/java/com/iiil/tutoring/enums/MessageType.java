package com.iiil.tutoring.enums;

/**
 * Message type enumeration
 */
public enum MessageType {
    TEXTE("texte"),
    FICHIER("fichier"),
    IMAGE("image"),
    SYSTEM("system");

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}