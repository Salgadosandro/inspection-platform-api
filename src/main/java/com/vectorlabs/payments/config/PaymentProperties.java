package com.vectorlabs.payments.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payments")
public class PaymentProperties {

    private MercadoPago mercadopago = new MercadoPago();

    public MercadoPago getMercadopago() {
        return mercadopago;
    }

    public void setMercadopago(MercadoPago mercadopago) {
        this.mercadopago = mercadopago;
    }

    public static class MercadoPago {

        private String accessToken;
        private String webhookSecret;
        private String baseUrl;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getWebhookSecret() {
            return webhookSecret;
        }

        public void setWebhookSecret(String webhookSecret) {
            this.webhookSecret = webhookSecret;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}
