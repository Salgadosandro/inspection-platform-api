package com.vectorlabs.security.oauth;

import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.enuns.AuthProvider;
import com.vectorlabs.model.enuns.UserRole;
import com.vectorlabs.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    private final AppUserRepository appUserRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = mapProvider(registrationId);
        OAuth2UserInfo info = OAuth2UserInfo.from(provider, oAuth2User.getAttributes());
        // Regras mínimas
        if (info.email() == null || info.email().isBlank()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }
        String email = info.email().trim().toLowerCase(Locale.ROOT);
        // 1) Tenta achar pelo provider+providerUserId (mais correto)
        Optional<AppUser> byProvider = appUserRepository
                .findByAuthProviderAndProviderUserId(provider, info.providerUserId());
        AppUser user = byProvider.orElseGet(() ->
                // 2) fallback: tenta achar por email (para vincular contas)
                appUserRepository.findByEmail(email).orElse(null)
        );
        if (user == null) {
            user = AppUser.builder()
                    .email(email)
                    .authProvider(provider)
                    .providerUserId(info.providerUserId())
                    .name(info.name())
                    .pictureUrl(info.pictureUrl())
                    .emailVerified(info.emailVerified())
                    .enabled(true)
                    .deleted(false)
                    .build();
            // role default (ajuste se seu enum for diferente)
            user.getRoles().add(UserRole.CLIENT);
        } else {
            // Atualiza dados “mutáveis”
            user.setName(info.name());
            user.setPictureUrl(info.pictureUrl());
            user.setEmailVerified(info.emailVerified());
            // Vincula provider se ainda não estiver preenchido
            if (user.getAuthProvider() == null || user.getAuthProvider() == AuthProvider.LOCAL) {
                user.setAuthProvider(provider);
            }
            if (user.getProviderUserId() == null || user.getProviderUserId().isBlank()) {
                user.setProviderUserId(info.providerUserId());
            }
            // Se quiser impedir login de usuário deletado:
            if (user.getDeleted() || !user.isEnabled()) {
                throw new OAuth2AuthenticationException("User disabled/deleted");
            }
        }
        user.setLastLoginAt(Instant.now());
        appUserRepository.save(user);
        return oAuth2User;
    }
    private AuthProvider mapProvider(String registrationId) {
        if (registrationId == null) return AuthProvider.LOCAL;
        return switch (registrationId.toLowerCase(Locale.ROOT)) {
            case "google" -> AuthProvider.GOOGLE;
            case "github" -> AuthProvider.GITHUB;
            default -> AuthProvider.LOCAL;
        };
    }
    record OAuth2UserInfo(
            String providerUserId,
            String email,
            String name,
            String pictureUrl,
            boolean emailVerified
    ) {
        static OAuth2UserInfo from(AuthProvider provider, java.util.Map<String, Object> attributes) {

            // Google normalmente:
            // sub, email, name, picture, email_verified
            if (provider == AuthProvider.GOOGLE) {
                String sub = asString(attributes.get("sub"));
                String email = asString(attributes.get("email"));
                String name = asString(attributes.get("name"));
                String picture = asString(attributes.get("picture"));
                boolean verified = asBoolean(attributes.get("email_verified"));
                return new OAuth2UserInfo(sub, email, name, picture, verified);
            }

            // GitHub normalmente:
            // id, email (às vezes null), name, login, avatar_url
            if (provider == AuthProvider.GITHUB) {
                String id = asString(attributes.get("id"));
                String email = asString(attributes.get("email")); // pode vir null
                String name = asString(attributes.get("name"));
                if (name == null || name.isBlank()) {
                    name = asString(attributes.get("login"));
                }
                String avatar = asString(attributes.get("avatar_url"));

                // GitHub pode não retornar email se estiver privado; você pode exigir scope "user:email"
                // ou aceitar null e buscar em endpoint extra (mais complexo). Aqui exigimos email no fluxo.
                return new OAuth2UserInfo(id, email, name, avatar, true);
            }

            // default genérico
            String id = asString(attributes.get("id"));
            String email = asString(attributes.get("email"));
            String name = asString(attributes.get("name"));
            return new OAuth2UserInfo(id, email, name, null, false);
        }

        static String asString(Object v) {
            return v == null ? null : String.valueOf(v);
        }

        static boolean asBoolean(Object v) {
            if (v == null) return false;
            if (v instanceof Boolean b) return b;
            return "true".equalsIgnoreCase(String.valueOf(v));
        }
    }
}

