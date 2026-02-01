package com.vectorlabs.security;

import com.vectorlabs.config.SecurityConfiguration;
import com.vectorlabs.security.jwt.JwtAuthenticationFilter;
import com.vectorlabs.security.oauth.OAuth2AuthenticationFailureHandler;
import com.vectorlabs.security.oauth.OAuth2AuthenticationSuccessHandler;
import com.vectorlabs.security.oauth.OAuth2UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BasicAuthEntryPointTest.TestController.class)
@Import({SecurityConfiguration.class, BasicAuthEntryPoint.class})
class BasicAuthEntryPointTest {

    @Autowired
    MockMvc mvc;

    // Esses mocks existem apenas para satisfazer dependências do SecurityConfiguration
    @MockBean JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean OAuth2UserServiceImpl oAuth2UserServiceImpl;
    @MockBean OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    @MockBean OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    /**
     * Controller fake só pra ter um endpoint protegido por /api/users/internal/**
     * (cai na basicChain do seu SecurityConfiguration)
     */
    @RestController
    static class TestController {
        @GetMapping("/api/users/internal/me")
        public String me() {
            return "ok";
        }
    }

    @Test
    void when_missing_basic_auth_on_internal_endpoint_then_returns_401_with_realm_and_json_body() throws Exception {
        mvc.perform(get("/api/users/internal/me").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Basic realm=\"VectorLabs-Internal\""))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication is required to access this resource"))
                .andExpect(jsonPath("$.path").value("/api/users/internal/me"));
    }
}