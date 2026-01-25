package com.vectorlabs.payments.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectorlabs.exception.WebhookRejectedException;
import com.vectorlabs.model.enuns.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class MercadoPagoWebhookParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parser defensivo:
     * - tenta pegar eventId de campos comuns ("id")
     * - tenta pegar paymentId de campos comuns ("data.id")
     * - tenta pegar status se vier ("status")
     *
     * Se o seu payload real for diferente, você ajusta aqui e o resto do sistema continua igual.
     */
    public Result parse(String rawPayload) {
        try {
            if (rawPayload == null || rawPayload.isBlank()) {
                throw new WebhookRejectedException("Empty webhook payload");
            }

            JsonNode root = objectMapper.readTree(rawPayload);

            String eventId = text(root, "id");
            String paymentId = text(root.path("data"), "id");

            // status pode não vir no webhook; se não vier, você reconcilia depois
            PaymentStatus status = null;
            String statusText = textOrNull(root, "status");
            if (statusText != null) {
                status = mapStatus(statusText);
            }

            if (eventId == null || paymentId == null) {
                throw new WebhookRejectedException("Webhook missing eventId/paymentId");
            }

            return new Result(eventId, paymentId, status);

        } catch (Exception e) {
            throw new WebhookRejectedException("Could not parse webhook payload: " + e.getMessage());
        }
    }

    private String text(JsonNode node, String field) {
        String v = textOrNull(node, field);
        if (v == null || v.isBlank()) return null;
        return v;
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode n = node.get(field);
        if (n == null || n.isNull()) return null;
        return n.asText();
    }

    private PaymentStatus mapStatus(String status) {
        // mapeamento bem conservador. Ajuste conforme o status real que você for usar.
        return switch (status.toLowerCase()) {
            case "approved", "paid" -> PaymentStatus.PAID;
            case "rejected", "failed" -> PaymentStatus.FAILED;
            case "cancelled", "canceled" -> PaymentStatus.CANCELED;
            case "refunded", "charged_back" -> PaymentStatus.REFUNDED;
            default -> PaymentStatus.PENDING;
        };
    }

    public record Result(String providerEventId, String providerPaymentId, PaymentStatus status) {}
}
