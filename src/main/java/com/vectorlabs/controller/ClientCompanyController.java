package com.vectorlabs.controller;

import com.vectorlabs.controller.common.GenericController;
import com.vectorlabs.dto.clientcompany.AnswerClientCompanyDTO;
import com.vectorlabs.dto.clientcompany.RegisterClientCompanyDTO;
import com.vectorlabs.dto.clientcompany.UpdateClientCompanyDTO;
import com.vectorlabs.mapper.ClientCompanyMapper;
import com.vectorlabs.service.ClientCompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@RestController
@RequestMapping("/api/client-companies")
@RequiredArgsConstructor
public class ClientCompanyController implements GenericController {

    private final ClientCompanyService service;
    private final ClientCompanyMapper mapper;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerClientCompanyDTO> create(@RequestBody @Valid RegisterClientCompanyDTO dto) {
        var entity = mapper.fromRegisterDTO(dto);
        var saved  = service.save(entity);
        var out    = mapper.toDTO(saved);
        return ResponseEntity.created(generateHeaderLocation(saved.getId())).body(out);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerClientCompanyDTO> getDetails(@PathVariable UUID id) {
        var result = service.findById(id);
        var out    = mapper.toDTO(result);
        return ResponseEntity.ok(out);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AnswerClientCompanyDTO>> getAll(
            @RequestParam(value = "user", required = false) UUID userId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "taxId", required = false) String taxId,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize
    ) {
        var result = service.search(userId, name, taxId, phone, email, page, pageSize);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerClientCompanyDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateClientCompanyDTO dto
    ) {
        var result = service.update(id, dto);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
