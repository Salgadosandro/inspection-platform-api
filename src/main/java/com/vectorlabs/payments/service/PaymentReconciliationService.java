package com.vectorlabs.payments.service;

import com.vectorlabs.model.enuns.PaymentStatus;
import com.vectorlabs.payments.model.PaymentIntent;
import com.vectorlabs.payments.gateway.mercadopago.PaymentGatewayClient;
import com.vectorlabs.repository.PaymentIntentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentReconciliationService {

    private final PaymentIntentRepository paymentIntentRepository;
    private final PaymentGatewayClient paymentGatewayClient;

    @Transactional
    public PaymentIntent reconcileByIntentId(UUID paymentIntentId) {
        PaymentIntent intent = paymentIntentRepository.findById(paymentIntentId)
                .orElseThrow(() -> new IllegalArgumentException("PaymentIntent not found"));

        // se já está finalizado, não precisa reconciliar
        if (isFinalStatus(intent.getStatus())) {
            return intent;
        }

        // se não tem providerPaymentId, não tem como consultar o provedor
        if (intent.getProviderPaymentId() == null || intent.getProviderPaymentId().isBlank()) {
            throw new IllegalStateException("Cannot reconcile payment without providerPaymentId");
        }

        PaymentStatus remoteStatus = paymentGatewayClient.getPaymentStatus(
                intent.getProvider(),
                intent.getProviderPaymentId()
        );

        // atualiza somente se mudou (evita write desnecessário)
        if (remoteStatus != intent.getStatus()) {
            intent.setStatus(remoteStatus);
            if (remoteStatus == PaymentStatus.PAID && intent.getPaidAt() == null) {
                intent.setPaidAt(Instant.now());
            }
            paymentIntentRepository.save(intent);
        }

        return intent;
    }

    /**
     * Regra simples: se estiver PENDING há tempo demais, vale reconciliar.
     * Você chamaria isso via scheduler depois, mas por enquanto pode ser manual.
     */
    public boolean shouldReconcile(PaymentIntent intent, Duration minAge) {
        if (isFinalStatus(intent.getStatus())) return false;
        if (intent.getCreatedAt() == null) return true;

        Instant cutoff = Instant.now().minus(minAge);
        return intent.getCreatedAt().isBefore(cutoff);
    }

    private boolean isFinalStatus(PaymentStatus status) {
        return status == PaymentStatus.PAID
                || status == PaymentStatus.CANCELED
                || status == PaymentStatus.FAILED
                || status == PaymentStatus.REFUNDED;
    }
}
