package com.vectorlabs.controller;

import com.vectorlabs.controller.common.GenericController;
import com.vectorlabs.dto.rule.AnswerRuleDTO;
import com.vectorlabs.dto.rule.RegisterRuleDTO;
import com.vectorlabs.dto.rule.UpdateRuleDTO;
import com.vectorlabs.mapper.RuleMapper;
import com.vectorlabs.model.Rule;
import com.vectorlabs.service.RuleService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Hidden
@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleController implements GenericController {

    private final RuleService service;
    private final RuleMapper mapper;

    // CREATE
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<AnswerRuleDTO> create(@RequestBody @Valid RegisterRuleDTO dto) {
        Rule entity = mapper.fromRegisterDTO(dto);
        Rule saved = service.save(entity);
        AnswerRuleDTO out = mapper.toDTO(saved);
        return ResponseEntity.created(generateHeaderLocation(saved.getId())).body(out);
    }

    // DETAILS
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<AnswerRuleDTO> getDetails(@PathVariable UUID id) {
        Rule found = service.findById(id);
        AnswerRuleDTO out = mapper.toDTO(found);
        return ResponseEntity.ok(out);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Page<AnswerRuleDTO>> getAll(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "deleted", required = false) Boolean deleted,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize
    ) {
        Page<AnswerRuleDTO> result = service.search(
                code,
                title,
                description,
                active,
                deleted,
                page,
                pageSize
        );
        return ResponseEntity.ok(result);
    }
    // UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<AnswerRuleDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateRuleDTO dto
    ) {
        AnswerRuleDTO result = service.update(id, dto);
        return ResponseEntity.ok(result);
    }

    // DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
