package com.vectorlabs.security.oauth;

import com.vectorlabs.model.AppUser;
import com.vectorlabs.repository.AppUserRepository;
import com.vectorlabs.security.jwt.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AppUserRepository appUserRepository;
    private final JwtService jwtService;

    @Value("${app.oauth2.redirect-success}")
    private String redirectSuccessUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        var attributes = token.getPrincipal().getAttributes();

        // email é o identificador padrão
        String email = attributes.get("email") == null ? null : attributes.get("email").toString();
        if (email == null || email.isBlank()) {
            // fallback (GitHub pode não retornar email)
            response.sendRedirect(redirectSuccessUrl + "?error=" + encode("email_not_found"));
            return;
        }
        AppUser user = appUserRepository.findByEmail(email.trim().toLowerCase())
                .orElse(null);
        if (user == null) {
            response.sendRedirect(redirectSuccessUrl + "?error=" + encode("user_not_found"));
            return;
        }
        if (user.isDeleted() || !user.isEnabled()) {
            response.sendRedirect(redirectSuccessUrl + "?error=" + encode("user_disabled"));
            return;
        }
        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);

        String targetUrl = UriComponentsBuilder.fromUriString(redirectSuccessUrl)
                .queryParam("accessToken", access)
                .queryParam("refreshToken", refresh)
                .build()
                .toUriString();

        response.sendRedirect(targetUrl);
    }
    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
