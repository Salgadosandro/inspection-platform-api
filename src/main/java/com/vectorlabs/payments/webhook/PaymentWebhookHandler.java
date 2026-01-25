package com.vectorlabs.payments.webhook;

import com.vectorlabs.exception.WebhookRejectedException;
import com.vectorlabs.model.enuns.PaymentProvider;
import com.vectorlabs.model.enuns.PaymentStatus;
import com.vectorlabs.payments.model.PaymentEvent;
import com.vectorlabs.repository.PaymentEventRepository;
import com.vectorlabs.repository.PaymentIntentRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentWebhookHandler {

    private final PaymentEventRepository paymentEventRepository;
    private final PaymentIntentRepository paymentIntentRepository;
    private final WebhookSignatureValidator signatureValidator;
    private final MercadoPagoWebhookParser mercadoPagoWebhookParser; // parser simples do payload

    @Transactional
    public void handleMercadoPago(String rawPayload, Map<String, String> headers, HttpServletRequest request) {

        // 1) valida assinatura (se você habilitar assinatura no provedor)
        // Se você ainda não tiver assinatura configurada, pode deixar esse validator aceitar "true" no MVP.
        boolean ok = signatureValidator.isValid(PaymentProvider.MERCADO_PAGO, rawPayload, headers, request);
        if (!ok) {
            throw new WebhookRejectedException("Invalid webhook signature");
        }

        // 2) extrai IDs importantes do payload (eventId e paymentId)
        MercadoPagoWebhookParser.Result parsed = mercadoPagoWebhookParser.parse(rawPayload);

        String providerEventId = parsed.providerEventId();
        String providerPaymentId = parsed.providerPaymentId();
        PaymentStatus newStatus = parsed.status(); // pode vir null se o payload não trouxer status

        // 3) idempotência: se já recebemos esse evento, ignoramos
        if (paymentEventRepository.existsByProviderAndProviderEventId(PaymentProvider.MERCADO_PAGO, providerEventId)) {
            return;
        }

        // 4) grava o evento (log imutável)
        PaymentEvent event = new PaymentEvent();
        event.setProvider(PaymentProvider.MERCADO_PAGO);
        event.setProviderEventId(providerEventId);
        event.setProviderPaymentId(providerPaymentId);
        event.setStatus(newStatus != null ? newStatus : PaymentStatus.PENDING);
        event.setRawPayload(rawPayload != null ? rawPayload : "");
        paymentEventRepository.save(event);

        // 5) encontra o intent pelo providerPaymentId
        var intentOpt = paymentIntentRepository.findByProviderAndProviderPaymentId(
                PaymentProvider.MERCADO_PAGO,
                providerPaymentId
        );

        // Se não achar, ainda assim não falha o webhook (pra não ficar em loop de reenvio).
        if (intentOpt.isEmpty()) {
            return;
        }

        var intent = intentOpt.get();

        // 6) atualiza o status do intent (com regra simples e segura)
        // No MVP, você pode atualizar diretamente com base no status do payload.
        // Em produção, é comum consultar o provedor para confirmar (reconciliation).
        if (newStatus != null && newStatus != intent.getStatus()) {
            intent.setStatus(newStatus);
            if (newStatus == PaymentStatus.PAID && intent.getPaidAt() == null) {
                intent.setPaidAt(Instant.now());
            }
            paymentIntentRepository.save(intent);
        }
    }
}
