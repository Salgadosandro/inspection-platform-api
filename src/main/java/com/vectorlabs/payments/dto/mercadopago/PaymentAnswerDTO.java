package com.vectorlabs.payments.dto.mercadopago;

import com.vectorlabs.model.enuns.PaymentProvider;
import com.vectorlabs.model.enuns.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentAnswerDTO(
        UUID paymentIntentId,
        UUID inspectionId,
        int machineCount,
        BigDecimal totalAmount,
        PaymentStatus status,
        PaymentProvider provider,
        String checkoutUrl
) {}

