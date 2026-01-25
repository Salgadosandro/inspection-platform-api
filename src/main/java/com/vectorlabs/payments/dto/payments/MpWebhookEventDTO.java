package com.vectorlabs.payments.dto.payments;

public record MpWebhookEventDTO(
        String id,
        String type,
        String action,
        Data data
) {

    public record Data(
            String id
    ) {}
}
