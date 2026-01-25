package com.vectorlabs.controller;

import com.vectorlabs.controller.common.GenericController;
import com.vectorlabs.dto.rule.ruleitem.AnswerRuleItemDTO;
import com.vectorlabs.dto.rule.ruleitem.RegisterRuleItemDTO;
import com.vectorlabs.dto.rule.ruleitem.UpdateRuleItemDTO;
import com.vectorlabs.mapper.RuleItemMapper;
import com.vectorlabs.service.RuleItemService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@Hidden
@RestController
@RequestMapping("/api/rule-items")
@RequiredArgsConstructor
public class RuleItemController implements GenericController {

    private final RuleItemService service;
    private final RuleItemMapper mapper;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerRuleItemDTO> create(@RequestBody @Valid RegisterRuleItemDTO dto) {
        var entity = mapper.fromRegisterDTO(dto);
        var saved  = service.save(entity);
        var out    = mapper.toDTO(saved);
        return ResponseEntity.created(generateHeaderLocation(saved.getId())).body(out);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerRuleItemDTO> getDetails(@PathVariable UUID id) {
        var result = service.findById(id);
        var out    = mapper.toDTO(result);
        return ResponseEntity.ok(out);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AnswerRuleItemDTO>> getAll(
            @RequestParam(value = "module_id", required = false) UUID moduleId,
            @RequestParam(value = "parent_id", required = false) UUID parentId,
            @RequestParam(value = "item_code", required = false) String itemCode,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize
    ) {
        var result = service.search(
                moduleId,
                parentId,
                itemCode,
                description,
                active,
                page,
                pageSize
        );
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerRuleItemDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateRuleItemDTO dto
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
