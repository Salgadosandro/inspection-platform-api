package com.vectorlabs.payments.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payments.mercadopago")
public record MercadoPagoProperties(
        String baseUrl,
        String accessToken,
        String notificationUrl,
        String currencyId
) {}
