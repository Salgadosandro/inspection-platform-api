package com.vectorlabs.payments.webhook;

import com.vectorlabs.model.enuns.PaymentProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AllowAllWebhookSignatureValidator implements WebhookSignatureValidator {

    @Override
    public boolean isValid(PaymentProvider provider, String rawPayload, Map<String, String> headers, HttpServletRequest request) {
        // MVP: aceitar tudo. Depois você troca por validação real.
        return true;
    }
}
