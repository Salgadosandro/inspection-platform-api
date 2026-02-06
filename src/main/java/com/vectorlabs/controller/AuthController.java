package com.vectorlabs.controller;

import com.vectorlabs.dto.auth.LoginDTO;
import com.vectorlabs.dto.auth.RefreshTokenDTO;
import com.vectorlabs.dto.auth.TokenResponseDTO;
import com.vectorlabs.model.AppUser;
import com.vectorlabs.repository.AppUserRepository;
import com.vectorlabs.security.jwt.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/users/internal")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final JwtService jwtService;

    //Working
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public TokenResponseDTO login(@RequestBody @Valid LoginDTO dto) {
        var authToken = new UsernamePasswordAuthenticationToken(dto.email(), dto.password());
        try {
            authenticationManager.authenticate(authToken);
        } catch (Exception ex) {
            throw new BadCredentialsException("Invalid credentials");
        }
        AppUser user = appUserRepository.findByEmail(dto.email().trim().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (user.getDeleted()  || !user.isEnabled()) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // Atualiza último login (opcional)
        user.setLastLoginAt(Instant.now());
        appUserRepository.save(user);

        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);

        return new TokenResponseDTO("Bearer", access, refresh);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public TokenResponseDTO refresh(@RequestBody @Valid RefreshTokenDTO dto) {

        String refreshToken = dto.refreshToken();

        // valida assinatura/expiração
        jwtService.validateTokenOrThrow(refreshToken);

        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid token");
        }

        var userId = jwtService.extractUserId(refreshToken);
        if (userId == null) {
            throw new BadCredentialsException("Invalid token");
        }

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("Invalid token"));

        if (user.getDeleted() || !user.isEnabled()) {
            throw new BadCredentialsException("Invalid token");
        }

        String newAccess = jwtService.generateAccessToken(user);
        String newRefresh = jwtService.generateRefreshToken(user);

        return new TokenResponseDTO("Bearer", newAccess, newRefresh);
    }
}

