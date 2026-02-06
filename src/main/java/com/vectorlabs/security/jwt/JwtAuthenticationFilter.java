package com.vectorlabs.security.jwt;

import com.vectorlabs.security.CustomUserDetails;
import com.vectorlabs.repository.AppUserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // sem bearer -> segue
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // valida assinatura + expiração
            Claims claims = jwtService.parseClaims(token);

            // Se você quiser aceitar apenas access token:
            // if (!jwtService.isAccessToken(token)) throw new JwtException("Not an access token");

            UUID userId = jwtService.extractUserId(token);
            String email = jwtService.extractEmail(token);

            // Se já estiver autenticado, segue
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            // Carrega user do banco (recomendado pra respeitar enabled/deleted/roles atuais)
            var user = appUserRepository.findById(userId)
                    .orElseThrow(() -> new JwtException("User not found"));

            // Se quiser cortar token de usuário deletado/desabilitado
            if (user.getDeleted() || !user.isEnabled()) {
                throw new JwtException("User disabled/deleted");
            }

            // Authorities: pode vir do token ou do banco.
            // Aqui: do banco (mais seguro se roles mudarem)
            var authorities = user.getRoles().stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                    .toList();

            // Principal (você pode usar CustomUserDetails ou só email)
            CustomUserDetails principal = new CustomUserDetails(user);

            var authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    authorities
            );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (JwtException ex) {
            // Token inválido: limpa contexto e responde 401 JSON simples
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                {"status":401,"error":"Unauthorized","message":"Invalid or expired token"}
            """);
        }
    }
}

