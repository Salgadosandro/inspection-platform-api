package com.vectorlabs.service;

import com.vectorlabs.dto.rule.ruledection.AnswerRuleSectionDTO;
import com.vectorlabs.dto.rule.ruledection.UpdateRuleSectionDTO;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.mapper.RuleSectionMapper;
import com.vectorlabs.model.RuleSection;
import com.vectorlabs.repository.RuleSectionRepository;
import com.vectorlabs.repository.specs.RuleSectionSpecs;
import com.vectorlabs.validator.RuleSectionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RuleSectionService {

    private final RuleSectionRepository repository;
    private final RuleSectionMapper mapper;
    private final RuleSectionValidator validator;

    // CREATE
    @Transactional
    public RuleSection save(RuleSection entity) {
        validator.validateCreation(entity);
        return repository.save(entity);
    }

    // READ - details
    @Transactional(readOnly = true)
    public RuleSection findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("RuleSection not found: " + id));
    }

    // READ - search (paged)
    @Transactional(readOnly = true)
    public Page<AnswerRuleSectionDTO> search(
            UUID ruleId,
            String sectionCode,
            String sectionName,
            Integer sequence,
            Boolean active,
            Integer page,
            Integer pageSize
    ) {

        PageRequest pr = pageable(page, pageSize);
        Specification<RuleSection> spec = Specification.allOf(
                RuleSectionSpecs.byRuleId(ruleId),
                RuleSectionSpecs.codeContains(sectionCode),
                RuleSectionSpecs.nameContains(sectionName),
                RuleSectionSpecs.sequenceEq(sequence),
                RuleSectionSpecs.activeEq(active)
        );

        var result = repository.findAll(spec, pr);
        return result.map(mapper::toDTO);
    }

    // UPDATE (PATCH semantics via UpdateDTO)
    @Transactional
    public AnswerRuleSectionDTO update(UUID id, UpdateRuleSectionDTO dto) {

        var existing = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("RuleSection not found: " + id));

        validator.validateUpdate(existing, dto);
        mapper.updateFromDTO(dto, existing);

        var saved = repository.save(existing);
        return mapper.toDTO(saved);
    }

    // DELETE
    @Transactional
    public void delete(UUID id) {
        var existing = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("RuleSection not found: " + id));
        validator.validateDelete(existing);
        repository.delete(existing);
    }

    // clamp page/pageSize (max 100)
    private PageRequest pageable(Integer page, Integer pageSize) {
        int p = (page == null || page < 0) ? 0 : page;
        int ps = (pageSize == null || pageSize <= 0) ? 10 : Math.min(pageSize, 100);
        return PageRequest.of(p, ps);
    }
}
