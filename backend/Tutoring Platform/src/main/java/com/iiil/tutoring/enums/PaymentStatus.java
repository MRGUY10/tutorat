package com.iiil.tutoring.enums;

/**
 * Payment status enumeration
 */
public enum PaymentStatus {
    EN_ATTENTE("en_attente"),
    COMPLETE("complete"),
    ECHOUE("echoue"),
    REMBOURSE("rembourse");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}