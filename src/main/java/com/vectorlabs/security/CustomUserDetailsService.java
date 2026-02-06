package com.vectorlabs.security;

import com.vectorlabs.model.AppUser;
import com.vectorlabs.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        final String email = normalizeEmail(username);

        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + email));

        // Se você quer cortar cedo (além do isEnabled() do CustomUserDetails)
        if (user.getDeleted()) {
            throw new UsernameNotFoundException("User is deleted: " + email);
        }

        // Basic Auth exige senha local. Se for OAuth2-only (password null), falha.
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new UsernameNotFoundException("User has no local password (OAuth2-only): " + email);
        }

        return new CustomUserDetails(user);
    }

    private String normalizeEmail(String value) {
        if (value == null) return "";
        return Objects.toString(value, "").trim().toLowerCase();
    }
}

