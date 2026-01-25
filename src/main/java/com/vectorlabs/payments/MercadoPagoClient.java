package com.vectorlabs.payments;

import com.vectorlabs.payments.config.MercadoPagoProperties;
import com.vectorlabs.model.enuns.PaymentProvider;
import com.vectorlabs.model.enuns.PaymentStatus;
import com.vectorlabs.payments.gateway.mercadopago.PaymentGatewayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MercadoPagoClient implements PaymentGatewayClient {

    private final RestClient mercadoPagoRestClient;
    private final MercadoPagoProperties props;

    @Override
    public CreateChargeResult createCharge(UUID inspectionId, int machineCountSnapshot, BigDecimal totalAmount) {

        // 1) Monta a “Preference” (Checkout Pro) e usa external_reference pra rastrear no seu sistema
        PreferenceRequest body = new PreferenceRequest(
                List.of(new Item(
                        "Inspeção NR12 (" + machineCountSnapshot + " máquinas)",
                        1,
                        props.currencyId() == null ? "BRL" : props.currencyId(),
                        totalAmount
                )),
                inspectionId.toString(),
                props.notificationUrl()
        );

        // 2) POST /checkout/preferences -> retorna init_point (checkout url) e id da preference
        PreferenceResponse resp = mercadoPagoRestClient
                .post()
                .uri("/checkout/preferences")
                .body(body)
                .retrieve()
                .body(PreferenceResponse.class);

        if (resp == null || resp.init_point() == null || resp.init_point().isBlank()) {
            throw new IllegalStateException("Mercado Pago did not return init_point");
        }

        // OBS: aqui eu devolvo providerPaymentId como o ID da PREFERENCE.
        // Em webhook, você recebe o ID do PAYMENT (v1/payments/{id}) e salva no PaymentIntent depois.
        return new CreateChargeResult(
                PaymentProvider.MERCADO_PAGO,
                resp.id(),
                resp.init_point()
        );
    }

    @Override
    public PaymentStatus getPaymentStatus(PaymentProvider provider, String providerPaymentId) {
        if (provider != PaymentProvider.MERCADO_PAGO) {
            throw new IllegalArgumentException("Unsupported provider: " + provider);
        }

        // Para consultar status real, o ideal é usar o ID do PAYMENT:
        // GET /v1/payments/{payment_id}
        PaymentGetResponse payment = mercadoPagoRestClient
                .get()
                .uri("/v1/payments/{id}", providerPaymentId)
                .retrieve()
                .body(PaymentGetResponse.class);

        if (payment == null || payment.status() == null) {
            throw new IllegalStateException("Mercado Pago did not return payment status");
        }

        return mapStatus(payment.status());
    }

    private PaymentStatus mapStatus(String mpStatus) {
        // Status do MP (exemplos comuns): approved, pending, in_process, rejected, cancelled, refunded, charged_back
        // Fonte: docs/SDK do MP listam esses status. :contentReference[oaicite:3]{index=3}
        return switch (mpStatus) {
            case "approved" -> PaymentStatus.PAID;
            case "cancelled" -> PaymentStatus.CANCELED;
            case "rejected" -> PaymentStatus.FAILED;
            case "refunded", "charged_back" -> PaymentStatus.REFUNDED;
            case "in_process", "pending", "authorized", "in_mediation" -> PaymentStatus.PENDING;
            default -> PaymentStatus.PENDING; // conservador no MVP
        };
    }

    // ===== DTOs internos (requests/responses) =====

    record PreferenceRequest(
            List<Item> items,
            String external_reference,
            String notification_url
    ) {}

    record Item(
            String title,
            int quantity,
            String currency_id,
            BigDecimal unit_price
    ) {}

    record PreferenceResponse(
            String id,
            String init_point
    ) {}

    record PaymentGetResponse(
            String id,
            String status
    ) {}
}
