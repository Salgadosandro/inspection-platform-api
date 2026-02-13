package com.vectorlabs.controller;

import com.vectorlabs.controller.common.GenericController;
import com.vectorlabs.dto.location.AnswerLocationDTO;
import com.vectorlabs.dto.location.RegisterLocationDTO;
import com.vectorlabs.dto.location.UpdateLocationDTO;
import com.vectorlabs.mapper.LocationMapper;
import com.vectorlabs.security.SecurityService;

import com.vectorlabs.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController implements GenericController {

    private final LocationService service;
    private final LocationMapper mapper;
    private final SecurityService securityService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()")
    public ResponseEntity<AnswerLocationDTO> create(@RequestBody @Valid RegisterLocationDTO dto) {
        UUID userId = securityService.getLoggedUser().getId();
        var entity = mapper.fromRegisterDTO(dto);
        var saved  = service.save(userId, entity);
        var out    = mapper.toDTO(saved);
        return ResponseEntity.created(generateHeaderLocation(saved.getId())).body(out);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()")
    public ResponseEntity<AnswerLocationDTO> getDetails(@PathVariable UUID id) {
        UUID userId = securityService.getLoggedUser().getId();
        var result = service.findById(userId, id);
        var out    = mapper.toDTO(result);
        return ResponseEntity.ok(out);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()")
    public ResponseEntity<Page<AnswerLocationDTO>> getAll(
            @RequestParam(value = "clientCompanyId", required = false) UUID clientCompanyId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "street", required = false) String street,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "zipCode", required = false) String zipCode,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize
    ) {
        UUID userId = securityService.getLoggedUser().getId();
        var result = service.search(userId, clientCompanyId, name, code, description, street, city, state, zipCode, page, pageSize);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()")
    public ResponseEntity<AnswerLocationDTO> update(@PathVariable UUID id, @RequestBody @Valid UpdateLocationDTO dto) {
        UUID userId = securityService.getLoggedUser().getId();
        var result = service.update(userId, id, dto);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        UUID userId = securityService.getLoggedUser().getId();
        service.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
