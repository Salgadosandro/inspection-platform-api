package com.vectorlabs.payments.dto.mercadopago;

import java.math.BigDecimal;

public record PricingPreviewDTO(
        int machineCount,
        BigDecimal reportFee,
        BigDecimal pricePerMachine,
        BigDecimal totalAmount
) {}
