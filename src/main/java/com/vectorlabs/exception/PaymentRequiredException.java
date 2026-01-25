package com.vectorlabs.exception;

public class PaymentRequiredException extends RuntimeException {

    public PaymentRequiredException() {
        super("Payment required for this inspection");
    }

    public PaymentRequiredException(String message) {
        super(message);
    }
}
