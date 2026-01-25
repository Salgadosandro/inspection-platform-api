package com.vectorlabs.payments.dto.payments;

public record MpCreatePreferenceResponseDTO(
        String id,
        String init_point,
        String sandbox_init_point
) {}
