package com.vectorlabs.payments.webhook;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments/webhooks")
public class PaymentWebhookController {

    private final PaymentWebhookHandler webhookHandler;

    /**
     * Endpoint genérico de webhook.
     * Você pode apontar o Mercado Pago para:
     *   POST /api/payments/webhooks/mercadopago
     */
    @PostMapping("/mercadopago")
    public ResponseEntity<Void> mercadoPagoWebhook(
            @RequestBody(required = false) String rawPayload,
            @RequestHeader Map<String, String> headers,
            HttpServletRequest request
    ) {
        webhookHandler.handleMercadoPago(rawPayload, headers, request);
        return ResponseEntity.ok().build();
    }
}

