package com.vectorlabs.controller;

import com.vectorlabs.controller.common.GenericController;
import com.vectorlabs.dto.rule.rulemodule.AnswerRuleModuleDTO;
import com.vectorlabs.dto.rule.rulemodule.RegisterRuleModuleDTO;
import com.vectorlabs.dto.rule.rulemodule.UpdateRuleModuleDTO;
import com.vectorlabs.mapper.RuleModuleMapper;
import com.vectorlabs.service.RuleModuleService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/rule-modules")
@RequiredArgsConstructor
public class RuleModuleController implements GenericController {

    private final RuleModuleService service;
    private final RuleModuleMapper mapper;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerRuleModuleDTO> create(@RequestBody @Valid RegisterRuleModuleDTO dto) {
        var entity = mapper.toEntity(dto);
        var saved  = service.save(entity);
        var out    = mapper.toDTO(saved);
        return ResponseEntity.created(generateHeaderLocation(saved.getId())).body(out);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerRuleModuleDTO> getDetails(@PathVariable UUID id) {
        var result = service.findById(id);
        var out    = mapper.toDTO(result);
        return ResponseEntity.ok(out);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AnswerRuleModuleDTO>> getAll(
            @RequestParam(value = "rule", required = false) UUID rule_id,
            @RequestParam(value = "section", required = false) UUID section_id,
            @RequestParam(value = "moduleCode", required = false) String moduleCode,
            @RequestParam(value = "moduleName", required = false) String moduleName,
            @RequestParam(value = "moduleSequence", required = false) Integer moduleSequence,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize
    ) {
        var result = service.search(rule_id, section_id, moduleCode, moduleName, moduleSequence, active, page, pageSize);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnswerRuleModuleDTO> update(@PathVariable UUID id, @RequestBody @Valid UpdateRuleModuleDTO dto) {
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
