package com.vectorlabs.controller;

import com.vectorlabs.controller.common.GenericController;
import com.vectorlabs.dto.checklisttemplate.AnswerChecklistTemplateDTO;
import com.vectorlabs.dto.checklisttemplate.RegisterChecklistTemplateDTO;
import com.vectorlabs.dto.checklisttemplate.UpdateChecklistTemplateDTO;
import com.vectorlabs.mapper.ChecklistTemplateMapper;
import com.vectorlabs.model.ChecklistTemplate;
import com.vectorlabs.security.SecurityService;
import com.vectorlabs.service.ChecklistTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/checklist-templates")
@RequiredArgsConstructor
public class ChecklistTemplateController implements GenericController {

    private final ChecklistTemplateService service;
    private final ChecklistTemplateMapper mapper;
    private final SecurityService securityService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()")
    public ResponseEntity<AnswerChecklistTemplateDTO> create(
            @RequestBody @Valid RegisterChecklistTemplateDTO dto
    ) {
        UUID userId = securityService.getLoggedUser().getId();

        ChecklistTemplate entity = mapper.fromRegisterDTO(dto);
        ChecklistTemplate saved = service.save(userId, entity);

        AnswerChecklistTemplateDTO response = mapper.toDTO(saved);

        return ResponseEntity
                .created(generateHeaderLocation(saved.getId()))
                .body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()")
    public ResponseEntity<AnswerChecklistTemplateDTO> getDetails(
            @PathVariable UUID id
    ) {
        UUID userId = securityService.getLoggedUser().getId();

        ChecklistTemplate entity = service.findById(userId, id);
        AnswerChecklistTemplateDTO response = mapper.toDTO(entity);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()")
    public ResponseEntity<Page<AnswerChecklistTemplateDTO>> getAll(
            @RequestParam(required = false) UUID user,
            @RequestParam(required = false) UUID rule,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(name = "page_size", defaultValue = "10") Integer pageSize
    ) {
        UUID userId = securityService.getLoggedUser().getId();

        Page<AnswerChecklistTemplateDTO> result = service.search(
                userId,
                user,
                rule,
                title,
                description,
                active,
                page,
                pageSize
        );

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()")
    public ResponseEntity<AnswerChecklistTemplateDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateChecklistTemplateDTO dto
    ) {
        UUID userId = securityService.getLoggedUser().getId();

        AnswerChecklistTemplateDTO updated = service.update(userId, id, dto);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        UUID userId = securityService.getLoggedUser().getId();

        service.delete(userId, id);

        return ResponseEntity.noContent().build();
    }
}
