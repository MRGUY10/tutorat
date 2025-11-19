package com.iiil.tutoring.enums;

/**
 * Payment method enumeration
 */
public enum PaymentMethod {
    CARTE("carte"),
    PAYPAL("paypal"),
    VIREMENT("virement");

    private final String value;

    PaymentMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}