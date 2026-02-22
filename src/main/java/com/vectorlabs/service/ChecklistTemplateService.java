package com.vectorlabs.service;

import com.vectorlabs.dto.checklisttemplate.AnswerChecklistTemplateDTO;
import com.vectorlabs.dto.checklisttemplate.UpdateChecklistTemplateDTO;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.mapper.ChecklistTemplateMapper;
import com.vectorlabs.model.ChecklistTemplate;
import com.vectorlabs.repository.ChecklistTemplateRepository;
import com.vectorlabs.repository.specs.ChecklistTemplateSpecs;
import com.vectorlabs.security.SecurityService;
import com.vectorlabs.validator.ChecklistTemplateValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
@Service
@RequiredArgsConstructor
public class ChecklistTemplateService {

    private final ChecklistTemplateRepository repository;
    private final ChecklistTemplateMapper mapper;
    private final SecurityService securityService;
    private final ChecklistTemplateValidator validator;

    @Transactional
    public ChecklistTemplate save(UUID loggedUserId, ChecklistTemplate entity) {

        boolean isAdmin = securityService.isAdmin();

        validator.validateCreation(loggedUserId, entity, isAdmin);

        // defaults defensivos (caso entity venha sem setar)
        entity.setDefault(false); // ou só setDefault(dto) no mapper
        entity.setActive(true);

        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public ChecklistTemplate findById(UUID loggedUserId, UUID id) {

        boolean isAdmin = securityService.isAdmin();

        if (isAdmin) {
            return repository.findById(id)
                    .orElseThrow(() -> new ObjectNotFound("ChecklistTemplate not found"));
        }

        Specification<ChecklistTemplate> spec =
                and(ChecklistTemplateSpecs.byId(id),
                        ChecklistTemplateSpecs.byUserId(loggedUserId));

        return repository.findOne(spec)
                .orElseThrow(() -> new ObjectNotFound("ChecklistTemplate not found"));
    }

    @Transactional(readOnly = true)
    public Page<AnswerChecklistTemplateDTO> search(
            UUID loggedUserId,
            UUID userFilter,
            UUID ruleFilter,
            String title,
            String description,
            Boolean active,
            Integer page,
            Integer pageSize
    ) {

        boolean isAdmin = securityService.isAdmin();

        validator.validateSearch(loggedUserId, userFilter, isAdmin, page, pageSize);

        Specification<ChecklistTemplate> spec = null;

        if (!isAdmin) {
            spec = and(spec, ChecklistTemplateSpecs.byUserId(loggedUserId));
        } else {

            spec = and(spec, ChecklistTemplateSpecs.byUserId(userFilter));
        }

        spec = and(spec, ChecklistTemplateSpecs.byRuleId(ruleFilter));
        spec = and(spec, ChecklistTemplateSpecs.titleContains(title));
        spec = and(spec, ChecklistTemplateSpecs.descriptionContains(description));
        spec = and(spec, ChecklistTemplateSpecs.eqActive(active));

        PageRequest pageRequest = PageRequest.of(page, pageSize);

        return repository.findAll(spec, pageRequest)
                .map(mapper::toDTO);
    }

    @Transactional
    public AnswerChecklistTemplateDTO update(
            UUID loggedUserId,
            UUID id,
            UpdateChecklistTemplateDTO dto
    ) {

        boolean isAdmin = securityService.isAdmin();

        validator.validateUpdate(loggedUserId, id, dto, isAdmin);

        ChecklistTemplate entity = findById(loggedUserId, id);

        mapper.updateFromDTO(dto, entity);

        ChecklistTemplate updated = repository.save(entity);

        return mapper.toDTO(updated);
    }

    @Transactional
    public void delete(UUID loggedUserId, UUID id) {

        boolean isAdmin = securityService.isAdmin();

        validator.validateDelete(loggedUserId, id, isAdmin);

        ChecklistTemplate entity = findById(loggedUserId, id);

        entity.setActive(false);

        repository.save(entity);
    }

    private Specification<ChecklistTemplate> and(
            Specification<ChecklistTemplate> base,
            Specification<ChecklistTemplate> add
    ) {
        if (base == null) return add;
        if (add == null) return base;
        return base.and(add);
    }
}