package com.vectorlabs.payments.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class MercadoPagoRestClientConfig {

    @Bean
    RestClient mercadoPagoRestClient(MercadoPagoProperties props) {
        return RestClient.builder()
                .baseUrl(props.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.accessToken())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
    }
}

