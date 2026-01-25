package com.vectorlabs.security.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Value("${app.oauth2.redirect-failure:${app.oauth2.redirect-success}}")
    private String redirectFailureUrl;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        String msg = exception.getMessage() == null ? "oauth2_login_failed" : exception.getMessage();

        String targetUrl = UriComponentsBuilder.fromUriString(redirectFailureUrl)
                .queryParam("error", URLEncoder.encode(msg, StandardCharsets.UTF_8))
                .build()
                .toUriString();

        response.sendRedirect(targetUrl);
    }
}
