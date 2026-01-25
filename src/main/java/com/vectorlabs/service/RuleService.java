package com.vectorlabs.service;

import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.dto.rule.AnswerRuleDTO;
import com.vectorlabs.dto.rule.UpdateRuleDTO;
import com.vectorlabs.mapper.RuleMapper;
import com.vectorlabs.model.Rule;
import com.vectorlabs.repository.RuleRepository;
import com.vectorlabs.repository.specs.RuleSpecs;
import com.vectorlabs.validator.RuleValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RuleService {

    private final RuleRepository repository;
    private final RuleMapper mapper;
    private final RuleValidator validator;

    // CREATE
    @Transactional
    public Rule save(Rule entity) {
        validator.validateCreation(entity);

        // defaults de soft delete
        if (entity.getDeleted() == null) {
            entity.setDeleted(false);
        }

        return repository.save(entity);
    }

    // READ - DETAILS
    @Transactional(readOnly = true)
    public Rule findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Rule not found: " + id));
    }

    // SEARCH / LIST
    @Transactional(readOnly = true)
    public Page<AnswerRuleDTO> search(
            String code,
            String title,
            String description,
            Boolean active,
            Boolean deleted,
            Integer page,
            Integer pageSize
    ) {
        Pageable pageable = PageRequest.of(
                page,
                pageSize,
                Sort.by(Sort.Direction.ASC, "code")
        );

        // padrão: não listar deletados (a não ser que o cliente peça explicitamente)
        Boolean deletedFilter = (deleted == null) ? Boolean.FALSE : deleted;

        Specification<Rule> spec = Specification.allOf(
                RuleSpecs.codeLike(code),
                RuleSpecs.titleLike(title),
                RuleSpecs.descriptionLike(description),
                RuleSpecs.activeEquals(active),
                RuleSpecs.deletedEquals(deletedFilter)
        );

        return repository.findAll(spec, pageable)
                .map(mapper::toDTO);
    }

    // UPDATE
    @Transactional
    public AnswerRuleDTO update(UUID id, UpdateRuleDTO dto) {
        var existing = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Rule not found: " + id));

        validator.validateUpdate(existing, dto);

        mapper.updateFromDTO(dto, existing);
        var saved = repository.save(existing);
        return mapper.toDTO(saved);
    }

    // SOFT DELETE
    @Transactional
    public void softDelete(UUID id) {
        var existing = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Rule not found: " + id));

        if (Boolean.TRUE.equals(existing.getDeleted())) {
            return; // idempotente
        }

        existing.setDeleted(true);
        existing.setActive(false);
        repository.save(existing);
    }

    // RESTORE (opcional, mas útil)
    @Transactional
    public void restore(UUID id) {
        var existing = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Rule not found: " + id));

        if (!Boolean.TRUE.equals(existing.getDeleted())) {
            return; // idempotente
        }

        existing.setDeleted(false);
        repository.save(existing);
    }
}





