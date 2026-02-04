package com.vectorlabs.config;

import com.vectorlabs.security.BasicAuthEntryPoint;
import com.vectorlabs.security.jwt.JwtAuthenticationFilter;
import com.vectorlabs.security.oauth.OAuth2AuthenticationFailureHandler;
import com.vectorlabs.security.oauth.OAuth2AuthenticationSuccessHandler;
import com.vectorlabs.security.oauth.OAuth2UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

    private final BasicAuthEntryPoint basicAuthEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2UserServiceImpl oAuth2UserServiceImpl;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Bean
    @Order(0)
    public SecurityFilterChain swaggerChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html" // opcional, caso você redirecione ou use legado
                )
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain basicChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/users/internal/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/users/internal").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/internal/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/internal/refresh").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic.authenticationEntryPoint(basicAuthEntryPoint));

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiJwtChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth -> auth
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain googleOauth2Chain(HttpSecurity http) throws Exception {

        http
            .securityMatcher(
                    "/oauth2/authorization/google",
                    "/login/oauth2/code/google"
            )
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(
                    auth -> auth
                    .requestMatchers(HttpMethod.GET, "/oauth2/authorization/google").permitAll()
                    .requestMatchers(HttpMethod.GET, "/login/oauth2/code/google").permitAll()
                    .anyRequest().denyAll()
            )
            .oauth2Login(
                    oauth -> oauth
                            .userInfoEndpoint(u -> u.userService(oAuth2UserServiceImpl))
                            .successHandler(oAuth2AuthenticationSuccessHandler)
                            .failureHandler(oAuth2AuthenticationFailureHandler)
            )
            .sessionManagement(
                    sm -> sm.sessionCreationPolicy(
                            SessionCreationPolicy.IF_REQUIRED
                    )
            )
            .logout(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    @Order(99)
    public SecurityFilterChain fallbackChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().denyAll());
        return http.build();
    }

}
