package com.vectorlabs.payments.dto;

import java.math.BigDecimal;

public record PricingPreviewDTO(
        int machineCount,
        BigDecimal reportFee,
        BigDecimal pricePerMachine,
        BigDecimal totalAmount
) {}
