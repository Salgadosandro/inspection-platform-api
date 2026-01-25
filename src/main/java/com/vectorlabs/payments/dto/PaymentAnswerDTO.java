package com.vectorlabs.payments.dto;

import com.vectorlabs.model.enuns.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentAnswerDTO(
        UUID paymentIntentId,
        UUID inspectionId,
        BigDecimal amount,
        int machineCount,
        String checkoutUrl,
        String qrCode,
        PaymentStatus status
) {}
