package com.vectorlabs.payments.gateway.mercadopago;


import com.vectorlabs.model.enuns.PaymentProvider;
import com.vectorlabs.model.enuns.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentGatewayClient {

    CreateChargeResult createCharge(UUID inspectionId, int machineCountSnapshot, BigDecimal totalAmount);

    PaymentStatus getPaymentStatus(PaymentProvider provider, String providerPaymentId);

    record CreateChargeResult(
            PaymentProvider provider,
            String providerPaymentId,
            String checkoutUrl
    ) {}
}
