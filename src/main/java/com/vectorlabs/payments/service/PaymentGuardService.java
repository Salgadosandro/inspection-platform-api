package com.vectorlabs.payments.service;

import com.vectorlabs.model.enuns.PaymentStatus;
import com.vectorlabs.payments.model.PaymentIntent;
import com.vectorlabs.repository.PaymentIntentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentGuardService {

    private final PaymentIntentRepository paymentIntentRepository;

    public boolean isInspectionPaid(UUID inspectionId) {
        return paymentIntentRepository.findTopByInspectionIdOrderByCreatedAtDesc(inspectionId)
                .map(pi -> pi.getStatus() == PaymentStatus.PAID)
                .orElse(false);
    }

    public PaymentIntent requirePaidInspection(UUID inspectionId) {
        PaymentIntent intent = paymentIntentRepository.findTopByInspectionIdOrderByCreatedAtDesc(inspectionId)
                .orElseThrow(() -> new IllegalStateException("Payment required for this inspection"));

        if (intent.getStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("Payment required for this inspection");
        }

        return intent;
    }
}
