package com.vectorlabs.security;

import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.model.AppUser;
import com.vectorlabs.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final AppUserService appUserService;

    public AppUser getLoggedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new ObjectNotFound("No authentication");
        }

        // JWT
        if (auth instanceof JwtAuthenticationToken jat) {
            Jwt jwt = jat.getToken();
            UUID userId = UUID.fromString(jwt.getSubject());
            return appUserService.findById(userId);
        }

        // Basic Auth
        if (auth.getPrincipal() instanceof CustomUserDetails principal) {
            return appUserService.findById(principal.getId());
        }

        throw new ObjectNotFound(
                "Unsupported authentication principal: " +
                        auth.getPrincipal().getClass().getName()
        );
    }

}


