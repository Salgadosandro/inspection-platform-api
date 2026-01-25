package com.vectorlabs.payments.dto;

public record CreatePaymentRequest(
        String couponCode,
        String paymentMethod
) {}
