package com.vectorlabs.security.jwt;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class JwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Jwt jwt) {

        Object raw = jwt.getClaims().get(JwtService.CLAIM_ROLES);
        if (raw == null) return List.of();

        // Esperado: ["ROLE_ADMIN", "ROLE_CLIENT"]
        List<String> roles = (List<String>) raw;

        return roles.stream()
                .map(r -> (GrantedAuthority) new SimpleGrantedAuthority(r))
                .toList();

    }
}

