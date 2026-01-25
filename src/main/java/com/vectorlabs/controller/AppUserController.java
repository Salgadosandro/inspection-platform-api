package com.vectorlabs.controller;
import com.vectorlabs.controller.common.GenericController;
import com.vectorlabs.dto.appuser.AnswerAppUserDTO;
import com.vectorlabs.dto.appuser.RegisterAppUserDTO;
import com.vectorlabs.dto.appuser.SearchAppUserDTO;
import com.vectorlabs.dto.appuser.UpdateAppUserDTO;
import com.vectorlabs.mapper.AppUserMapper;
import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.enuns.AuthProvider;
import com.vectorlabs.model.enuns.UserRole;
import com.vectorlabs.security.CustomUserDetails;
import com.vectorlabs.security.SecurityService;
import com.vectorlabs.service.AppUserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/internal")
@RequiredArgsConstructor
public class AppUserController implements GenericController {

    private final AppUserService service;
    private final SecurityService securityService;
    private final AppUserMapper mapper;


    //Working
    @PostMapping
    public ResponseEntity<AnswerAppUserDTO> register(@RequestBody @Valid RegisterAppUserDTO dto) {
        AppUser saved = service.register(dto);
        AnswerAppUserDTO out = mapper.toAnswerDTO(saved);
        URI location = generateHeaderLocation(saved.getId());

        return ResponseEntity.created(location).body(out);
    }
    //Working
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnswerAppUserDTO> getDetails(@PathVariable UUID id) {
        AppUser entity = service.findById(id);
        return ResponseEntity.ok(mapper.toAnswerDTO(entity));
    }
    //Working
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerAppUserDTO> getMe(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        AppUser user = principal.getUser(); // ou principal.getId()
        return ResponseEntity.ok(mapper.toAnswerDTO(user));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerAppUserDTO> updateMe(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody @Valid UpdateAppUserDTO dto
    ) {
        AppUser logged = principal.getUser(); // ou principal.getId()
        AppUser result = service.updateMe(dto, logged);

        return ResponseEntity.ok(mapper.toAnswerDTO(result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnswerAppUserDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateAppUserDTO dto
    ) {
        AppUser result = service.update(id, dto);
        return ResponseEntity.ok(mapper.toAnswerDTO(result));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AnswerAppUserDTO>> search(
            @RequestParam(required = false) UUID id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String cnpj,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Boolean deleted,
            @RequestParam(required = false) AuthProvider authProvider,
            @RequestParam(required = false) Set<UserRole> roles,
            @RequestParam(required = false) Instant lastLoginFrom,
            @RequestParam(required = false) Instant lastLoginTo,
            @PageableDefault(size = 20) Pageable pageable
    ) {

        SearchAppUserDTO filter = new SearchAppUserDTO(
                id,
                name,
                email,
                cpf,
                cnpj,
                city,
                state,
                country,
                enabled,
                deleted,
                authProvider,
                roles,
                lastLoginFrom,
                lastLoginTo
        );

        Page<AppUser> page = service.search(filter, pageable);
        Page<AnswerAppUserDTO> out = page.map(mapper::toAnswerDTO);

        return ResponseEntity.ok(out);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AnswerAppUserDTO> patch(
            @PathVariable UUID id,
            @RequestBody UpdateAppUserDTO dto
    ) {
        AppUser updated = service.patch(id, dto);
        return ResponseEntity.ok(mapper.toAnswerDTO(updated));
    }

    /**
     * Soft delete (marca deleted=true)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable UUID id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{id}/hard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnswerAppUserDTO> delete(@PathVariable UUID id) {
        // Sugestão: isso deveria ser soft delete por padrão
        AppUser result = service.deleteById(id);
        return ResponseEntity.ok(mapper.toAnswerDTO(result));
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerAppUserDTO> deleteMe() {
        AppUser logged = securityService.getLoggedUser();
        AppUser result = service.softDeleteMe(logged);
        return ResponseEntity.ok(mapper.toAnswerDTO(result));
    }

}