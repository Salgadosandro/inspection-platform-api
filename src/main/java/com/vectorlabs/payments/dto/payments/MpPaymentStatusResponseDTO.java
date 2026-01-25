package com.vectorlabs.payments.dto.payments;

public record MpPaymentStatusResponseDTO(
        String id,
        String status,
        String status_detail
) {}
