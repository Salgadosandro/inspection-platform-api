package com.vectorlabs.payments.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingService {

    // taxa fixa por “relatório/inspeção”
    private static final BigDecimal REPORT_FEE = new BigDecimal("10.00");

    // preço por máquina
    private static final BigDecimal PRICE_PER_MACHINE = new BigDecimal("20.00");

    public BigDecimal reportFee() {
        return REPORT_FEE;
    }

    public BigDecimal pricePerMachine() {
        return PRICE_PER_MACHINE;
    }

    public BigDecimal calculateTotal(int machineCount) {
        if (machineCount <= 0) {
            throw new IllegalArgumentException("Machine count must be positive");
        }

        BigDecimal machinesPart = PRICE_PER_MACHINE.multiply(BigDecimal.valueOf(machineCount));
        return REPORT_FEE.add(machinesPart);
    }
}
