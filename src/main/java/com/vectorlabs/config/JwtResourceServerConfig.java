package com.vectorlabs.config;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Configuration
public class JwtResourceServerConfig {

    @Bean
    public JwtDecoder jwtDecoder(@Value("${security.jwt.secret}") String base64Secret) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}

