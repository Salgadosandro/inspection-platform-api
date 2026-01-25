package com.vectorlabs.service;

import com.vectorlabs.dto.rule.ruleitem.AnswerRuleItemDTO;
import com.vectorlabs.dto.rule.ruleitem.UpdateRuleItemDTO;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.mapper.RuleItemMapper;
import com.vectorlabs.model.RuleItem;
import com.vectorlabs.repository.RuleItemRepository;
import com.vectorlabs.repository.specs.RuleItemSpecs;
import com.vectorlabs.validator.RuleItemValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RuleItemService {

    private final RuleItemRepository repository;
    private final RuleItemMapper mapper;
    private final RuleItemValidator validator;
    // CREATE
    @Transactional
    public RuleItem save(RuleItem entity) {
        validator.validateCreation(entity);
        return repository.save(entity);
    }
    // READ - details
    @Transactional(readOnly = true)
    public RuleItem findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("RuleItem not found: " + id));
    }
    // READ - search (paged)
    @Transactional(readOnly = true)
    public Page<AnswerRuleItemDTO> search(
            UUID moduleId,
            UUID parentId,
            String itemCode,
            String description,
            Boolean active,
            Integer page,
            Integer pageSize
    ) {
        int p  = (page == null || page < 0) ? 0 : page;
        int ps = (pageSize == null || pageSize <= 0) ? 10 : Math.min(pageSize, 100);
        Specification<RuleItem> spec = RuleItemSpecs.any()
                .and(RuleItemSpecs.byModuleId(moduleId))
                .and(RuleItemSpecs.byParentId(parentId))
                .and(RuleItemSpecs.itemCodeContains(itemCode))
                .and(RuleItemSpecs.descriptionContains(description))
                .and(RuleItemSpecs.eqActive(active)
                );

        var result = repository.findAll(spec, PageRequest.of(p, ps));
        return result.map(mapper::toDTO);
    }
    // UPDATE (PATCH semantics via UpdateDTO)
    @Transactional
    public AnswerRuleItemDTO update(UUID id, UpdateRuleItemDTO dto) {
        var existing = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("RuleItem not found: " + id));
        validator.validateUpdate(existing, dto);
        mapper.updateFromDTO(dto, existing);
        var saved = repository.save(existing);
        return mapper.toDTO(saved);
    }
    // DELETE
    @Transactional
    public void delete(UUID id) {
            var existing = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("RuleItem not found: " + id));
        validator.validateDelete(existing);
        repository.delete(existing);
    }
}

