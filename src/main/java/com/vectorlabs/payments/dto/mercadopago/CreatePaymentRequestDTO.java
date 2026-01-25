package com.vectorlabs.payments.dto.mercadopago;

public record CreatePaymentRequestDTO(
        String returnUrl,
        String cancelUrl
) {}

