package com.vectorlabs.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.security.core.GrantedAuthority;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;


@ExtendWith(MockitoExtension.class)
class JwtGrantedAuthoritiesConverterTest {

    private final JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();

    @Test
    void converts_roles_claim_into_authorities() {
        Jwt jwt = Jwt.withTokenValue("t")
                .header("alg", "none")
                .claim("roles", List.of("ADMIN", "INSPECTOR"))
                .build();

        var authorities = converter.convert(jwt);

        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_ADMIN", "ROLE_INSPECTOR");
    }
}
