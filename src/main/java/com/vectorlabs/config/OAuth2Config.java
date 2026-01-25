package com.vectorlabs.config;

import com.vectorlabs.security.oauth.OAuth2AuthenticationFailureHandler;
import com.vectorlabs.security.oauth.OAuth2AuthenticationSuccessHandler;
import com.vectorlabs.security.oauth.OAuth2UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@RequiredArgsConstructor
public class OAuth2Config {

    private final OAuth2UserServiceImpl oAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler successHandler;
    private final OAuth2AuthenticationFailureHandler failureHandler;

    public void configure(HttpSecurity http) throws Exception {
        http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                .successHandler(successHandler)
                .failureHandler(failureHandler)
        );
    }
}
