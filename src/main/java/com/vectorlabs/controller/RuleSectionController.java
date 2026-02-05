package com.vectorlabs.controller;

import com.vectorlabs.controller.common.GenericController;
import com.vectorlabs.dto.rule.ruledection.AnswerRuleSectionDTO;
import com.vectorlabs.dto.rule.ruledection.RegisterRuleSectionDTO;
import com.vectorlabs.dto.rule.ruledection.UpdateRuleSectionDTO;
import com.vectorlabs.mapper.RuleSectionMapper;
import com.vectorlabs.service.RuleSectionService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/rule-sections")
@RequiredArgsConstructor
public class RuleSectionController implements GenericController {

    private final RuleSectionService service;
    private final RuleSectionMapper mapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<AnswerRuleSectionDTO> create(@RequestBody @Valid RegisterRuleSectionDTO dto) {
        var entity = mapper.toEntity(dto);
        var saved  = service.save(entity);
        var out    = mapper.toDTO(saved);
        return ResponseEntity.created(generateHeaderLocation(saved.getId())).body(out);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<AnswerRuleSectionDTO> getDetails(@PathVariable UUID id) {
        var result = service.findById(id);
        var out    = mapper.toDTO(result);
        return ResponseEntity.ok(out);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Page<AnswerRuleSectionDTO>> getAll(
            @RequestParam(value = "rule", required = false) UUID rule_id,
            @RequestParam(value = "code", required = false) String sectionCode,
            @RequestParam(value = "name", required = false) String sectionName,
            @RequestParam(value = "sequence", required = false) Integer sequence,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize
    ) {
        var result = service.search(rule_id, sectionCode, sectionName, sequence, active, page, pageSize);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<AnswerRuleSectionDTO> update(@PathVariable UUID id, @RequestBody @Valid UpdateRuleSectionDTO dto) {
        var result = service.update(id, dto);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
