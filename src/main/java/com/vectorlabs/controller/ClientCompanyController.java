package com.vectorlabs.controller;

import com.vectorlabs.controller.common.GenericController;
import com.vectorlabs.dto.clientcompany.AnswerClientCompanyDTO;
import com.vectorlabs.dto.clientcompany.RegisterClientCompanyAdminDTO;
import com.vectorlabs.dto.clientcompany.RegisterClientCompanyDTO;
import com.vectorlabs.dto.clientcompany.UpdateClientCompanyDTO;
import com.vectorlabs.service.ClientCompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/client-companies")
@RequiredArgsConstructor
public class ClientCompanyController implements GenericController {

    private final ClientCompanyService service;

    // USER cria pra si; ADMIN pode criar pra si também (ou use o endpoint admin abaixo)
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerClientCompanyDTO> create(@RequestBody @Valid RegisterClientCompanyDTO dto) {
        var out = service.create(dto);
        return ResponseEntity.created(generateHeaderLocation(out.id())).body(out);
    }

    // ADMIN cria para outro user
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnswerClientCompanyDTO> adminCreate(@RequestBody @Valid RegisterClientCompanyAdminDTO dto) {
        var out = service.adminCreate(dto);
        return ResponseEntity
                .created(locationOf("/api/client-companies", out.id()))
                .body(out);

    }

    // USER: só dele | ADMIN: qualquer
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerClientCompanyDTO> getDetails(@PathVariable UUID id) {
        var out = service.getDetails(id);
        return ResponseEntity.ok(out);
    }

    /**
     * USER: ignora user param e lista apenas suas empresas
     * ADMIN: pode filtrar por user param (ou null para listar todas)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AnswerClientCompanyDTO>> getAll(
            @RequestParam(value = "user", required = false) UUID userId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "tradeName", required = false) String tradeName,
            @RequestParam(value = "cnpj", required = false) String cnpj,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize
    ) {
        var result = service.search(userId, name, tradeName, cnpj, phone, email, active, page, pageSize);
        return ResponseEntity.ok(result);
    }

    // USER: só dele | ADMIN: qualquer
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerClientCompanyDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateClientCompanyDTO dto
    ) {
        var out = service.update(id, dto);
        return ResponseEntity.ok(out);
    }

    // SOFT DELETE (USER: só dele | ADMIN: qualquer)
    @PatchMapping("/{id}/soft-delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> softDelete(@PathVariable UUID id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    // (Opcional) reativar
    @PatchMapping("/{id}/activate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> activate(@PathVariable UUID id) {
        service.activate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
