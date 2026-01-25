package com.vectorlabs.controller;

import com.vectorlabs.dto.machine.AnswerMachineDTO;
import com.vectorlabs.dto.machine.RegisterMachineDTO;
import com.vectorlabs.dto.machine.SearchMachineDTO;
import com.vectorlabs.dto.machine.UpdateMachineDTO;
import com.vectorlabs.mapper.MachineMapper;
import com.vectorlabs.model.Machine;
import com.vectorlabs.service.MachineService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Hidden
@RestController
@RequestMapping("/api/machines")
@RequiredArgsConstructor
public class MachineController {

    private final MachineService service;
    private final MachineMapper mapper;

    /** Criar máquina */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerMachineDTO> create(@RequestBody @Valid RegisterMachineDTO dto) {
        Machine entity = mapper.toEntity(dto);
        Machine saved = service.save(entity);
        AnswerMachineDTO out = mapper.toDTO(saved);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(out);
    }

    /** Atualizar máquina (PUT – atualização completa/segura) */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerMachineDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateMachineDTO dto
    ) {
        Machine updated = service.update(id, dto); // service aplica validações e usa mapper.updateEntity(...)
        return ResponseEntity.ok(mapper.toDTO(updated));
    }

    /** Detalhar máquina por ID */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerMachineDTO> details(@PathVariable UUID id) {
        Machine entity = service.findById(id);
        return ResponseEntity.ok(mapper.toDTO(entity));
    }

    /** Remover máquina */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /** Listar máquinas com filtros + paginação */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AnswerMachineDTO>> list(
            @Valid @ModelAttribute SearchMachineDTO filters,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize
    ) {
        Page<Machine> pageResult = service.search(filters, page, pageSize);
        Page<AnswerMachineDTO> dtoPage = pageResult.map(mapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }
}
