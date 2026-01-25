package com.vectorlabs.payments.webhook;

import com.vectorlabs.model.enuns.PaymentProvider;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface WebhookSignatureValidator {

    /**
     * Valida se o webhook é legítimo (assinatura/secret/header/IP etc.).
     * No MVP você pode retornar true e depois apertar a segurança.
     */
    boolean isValid(
            PaymentProvider provider,
            String rawPayload,
            Map<String, String> headers,
            HttpServletRequest request
    );
}
