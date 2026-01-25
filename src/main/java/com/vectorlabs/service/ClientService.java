package com.vectorlabs.service;

import com.vectorlabs.dto.client.AnswerClientDTO;
import com.vectorlabs.dto.client.RegisterClientDTO;
import com.vectorlabs.dto.client.UpdateClientDTO;
import com.vectorlabs.model.Client;
import com.vectorlabs.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AnswerClientDTO register(RegisterClientDTO dto) {

        String clientId = normalize(dto.clientId());
        if (clientRepository.existsByClientId(clientId)) {
            throw new IllegalArgumentException("ClientId already exists");
        }

        Client client = Client.builder()
                .clientId(clientId)
                .clientSecretHash(passwordEncoder.encode(dto.clientSecret()))
                .redirectUri(normalizeNullable(dto.redirectUri()))
                .scopes(normalizeScopes(dto.scope()))
                .enabled(true)
                .deleted(false)
                .build();

        clientRepository.save(client);

        return toAnswer(client);
    }

    @Transactional(readOnly = true)
    public AnswerClientDTO getById(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        return toAnswer(client);
    }

    @Transactional(readOnly = true)
    public AnswerClientDTO getByClientId(String clientId) {
        Client client = clientRepository.findByClientId(normalize(clientId))
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        return toAnswer(client);
    }

    @Transactional
    public AnswerClientDTO update(UUID id, UpdateClientDTO dto) {

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        if (dto.clientSecret() != null && !dto.clientSecret().isBlank()) {
            client.setClientSecretHash(passwordEncoder.encode(dto.clientSecret()));
        }
        if (dto.redirectUri() != null) {
            client.setRedirectUri(normalizeNullable(dto.redirectUri()));
        }
        if (dto.scope() != null) {
            client.setScopes(normalizeScopes(dto.scope()));
        }
        if (dto.enabled() != null) {
            client.setEnabled(dto.enabled());
        }

        clientRepository.save(client);
        return toAnswer(client);
    }

    @Transactional
    public void softDelete(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        client.setDeleted(true);
        client.setEnabled(false);
        clientRepository.save(client);
    }

    /**
     * Para autenticação de integrações:
     * valida clientId/clientSecret (texto) contra hash salvo.
     */
    @Transactional(readOnly = true)
    public boolean validateSecret(String clientId, String rawSecret) {

        Client client = clientRepository.findByClientId(normalize(clientId))
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        if (client.isDeleted() || !client.isEnabled()) return false;

        return passwordEncoder.matches(rawSecret, client.getClientSecretHash());
    }

    @Transactional
    public void markUsed(String clientId) {
        clientRepository.findByClientId(normalize(clientId)).ifPresent(c -> {
            c.setLastUsedAt(Instant.now());
            clientRepository.save(c);
        });
    }

    public List<String> getScopeList(Client client) {
        if (client.getScopes() == null || client.getScopes().isBlank()) return List.of();
        return List.of(client.getScopes().trim().split("\\s+"));
    }

    private AnswerClientDTO toAnswer(Client c) {
        return new AnswerClientDTO(
                c.getId(),
                c.getClientId(),
                c.getScopes(),
                c.getRedirectUri(),
                c.isEnabled(),
                c.isDeleted(),
                c.getLastUsedAt()
        );
    }

    private String normalize(String value) {
        if (value == null) return "";
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeNullable(String value) {
        if (value == null) return null;
        String v = value.trim();
        return v.isBlank() ? null : v;
    }

    private String normalizeScopes(String value) {
        // troca vírgula por espaço e normaliza múltiplos espaços
        if (value == null) return "";
        return value.trim()
                .replace(",", " ")
                .replaceAll("\\s+", " ");
    }
}

