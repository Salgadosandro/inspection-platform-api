package com.vectorlabs.security.jwt;

import com.vectorlabs.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    public static final String CLAIM_UID = "uid";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_PROVIDER = "provider";

    private final SecretKey signingKey;
    private final long accessTokenTtlSeconds;
    private final long refreshTokenTtlSeconds;
    private final String issuer;

    public JwtService(
            @Value("${security.jwt.secret}") String base64Secret,
            @Value("${security.jwt.issuer:vectorlabs}") String issuer,
            @Value("${security.jwt.access-ttl-seconds:3600}") long accessTokenTtlSeconds,
            @Value("${security.jwt.refresh-ttl-seconds:1209600}") long refreshTokenTtlSeconds
    ) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.issuer = issuer;
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
    }

    public String generateAccessToken(AppUser user) {
        return buildToken(user, accessTokenTtlSeconds, "access");
    }

    public String generateRefreshToken(AppUser user) {
        return buildToken(user, refreshTokenTtlSeconds, "refresh");
    }

    private String buildToken(AppUser user, long ttlSeconds, String tokenType) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlSeconds);

        List<String> roles = user.getRoles() == null
                ? List.of()
                : user.getRoles().stream()
                .map(r -> "ROLE_" + r.name())
                .toList();

        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(user.getEmail()) // sub = email
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .claim("typ", tokenType)
                .claim(CLAIM_UID, user.getId() != null ? user.getId().toString() : null)
                .claim(CLAIM_ROLES, roles)
                .claim(CLAIM_PROVIDER, user.getAuthProvider() != null ? user.getAuthProvider().name() : null)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Valida assinatura + expiração. Lança JwtException se inválido. */
    public void validateTokenOrThrow(String token) throws JwtException {
        parseClaims(token);
    }

    public Claims parseClaims(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public UUID extractUserId(String token) {
        Object raw = parseClaims(token).get(CLAIM_UID);
        if (raw == null) return null;
        return UUID.fromString(String.valueOf(raw));
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object raw = parseClaims(token).get(CLAIM_ROLES);
        if (raw == null) return List.of();
        return (List<String>) raw;
    }

    public boolean isAccessToken(String token) {
        Object typ = parseClaims(token).get("typ");
        return "access".equals(String.valueOf(typ));
    }

    public boolean isRefreshToken(String token) {
        Object typ = parseClaims(token).get("typ");
        return "refresh".equals(String.valueOf(typ));
    }
}
