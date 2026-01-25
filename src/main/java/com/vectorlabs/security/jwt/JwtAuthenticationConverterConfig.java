package com.vectorlabs.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
@RequiredArgsConstructor
public class JwtAuthenticationConverterConfig {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        // Isso diz ao Resource Server: "minhas authorities vêm do claim roles"
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return converter;
    }
}
