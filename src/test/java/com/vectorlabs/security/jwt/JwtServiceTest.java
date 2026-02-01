package com.vectorlabs.security.jwt;

import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.enuns.AuthProvider;
import com.vectorlabs.model.enuns.UserRole;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private static String base64Secret() {
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        return Encoders.BASE64.encode(key.getEncoded());
    }

    private static AppUser user(UUID id, String email, Set<UserRole> roles) {
        AppUser u = new AppUser();
        u.setId(id);
        u.setEmail(email);
        u.setRoles(roles);
        u.setAuthProvider(AuthProvider.LOCAL);
        u.setEnabled(true);
        u.setDeleted(false);
        return u;
    }

    @Test
    void generateAccessToken_contains_expected_claims() {
        String secret = base64Secret();
        JwtService jwtService = new JwtService(secret, "vectorlabs", 3600, 1209600);

        UUID userId = UUID.randomUUID();
        AppUser user = user(
                userId,
                "user@test.com",
                Set.of(UserRole.ADMIN, UserRole.CLIENT)
        );

        String token = jwtService.generateAccessToken(user);

        // valida
        assertThatCode(() -> jwtService.validateTokenOrThrow(token))
                .doesNotThrowAnyException();

        assertThat(jwtService.extractEmail(token))
                .isEqualTo("user@test.com");

        assertThat(jwtService.extractUserId(token))
                .isEqualTo(userId);

        assertThat(jwtService.extractRoles(token))
                .containsExactlyInAnyOrder(
                        "ROLE_ADMIN",
                        "ROLE_CLIENT"
                );

        assertThat(jwtService.isAccessToken(token)).isTrue();
        assertThat(jwtService.isRefreshToken(token)).isFalse();
    }

    @Test
    void generateRefreshToken_sets_typ_refresh() {
        String secret = base64Secret();
        JwtService jwtService = new JwtService(secret, "vectorlabs", 3600, 1209600);

        AppUser user = user(
                UUID.randomUUID(),
                "user@test.com",
                Set.of(UserRole.USER)
        );

        String token = jwtService.generateRefreshToken(user);

        assertThat(jwtService.isRefreshToken(token)).isTrue();
        assertThat(jwtService.isAccessToken(token)).isFalse();
    }

    @Test
    void validateTokenOrThrow_fails_with_invalid_signature() {
        String secretA = base64Secret();
        String secretB = base64Secret();

        JwtService jwtA = new JwtService(secretA, "vectorlabs", 3600, 1209600);
        JwtService jwtB = new JwtService(secretB, "vectorlabs", 3600, 1209600);

        AppUser user = user(
                UUID.randomUUID(),
                "user@test.com",
                Set.of(UserRole.ADMIN)
        );

        String token = jwtA.generateAccessToken(user);

        assertThatThrownBy(() -> jwtB.validateTokenOrThrow(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void expired_token_is_rejected() {
        String secret = base64Secret();

        JwtService expiredJwtService =
                new JwtService(secret, "vectorlabs", -5, 1209600);

        JwtService validator =
                new JwtService(secret, "vectorlabs", 3600, 1209600);

        AppUser user = user(
                UUID.randomUUID(),
                "user@test.com",
                Set.of(UserRole.ADMIN)
        );

        String expiredToken = expiredJwtService.generateAccessToken(user);

        assertThatThrownBy(() -> validator.validateTokenOrThrow(expiredToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void extractUserId_returns_null_if_missing() {
        String secret = base64Secret();
        JwtService jwtService = new JwtService(secret, "vectorlabs", 3600, 1209600);

        AppUser user = user(
                null,
                "user@test.com",
                Set.of(UserRole.USER)
        );

        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractUserId(token)).isNull();
    }
}
