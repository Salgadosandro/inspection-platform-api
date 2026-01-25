package com.vectorlabs.service;

import com.vectorlabs.dto.rule.rulemodule.AnswerRuleModuleDTO;
import com.vectorlabs.dto.rule.rulemodule.UpdateRuleModuleDTO;
import com.vectorlabs.exception.ObjectNotFound;
import com.vectorlabs.mapper.RuleModuleMapper;
import com.vectorlabs.model.RuleModule;
import com.vectorlabs.repository.RuleModuleRepository;
import com.vectorlabs.repository.specs.RuleModuleSpecs;
import com.vectorlabs.validator.RuleModuleValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RuleModuleService {

    private final RuleModuleRepository repository;
    private final RuleModuleMapper mapper;
    private final RuleModuleValidator validator;

    // CREATE
    @Transactional
    public RuleModule save(RuleModule entity) {
        validator.validateCreation(entity);
        return repository.save(entity);
    }

    // READ - details
    @Transactional(readOnly = true)
    public RuleModule findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("RuleModule not found: " + id));
    }

    // READ - search (paged)
    @Transactional(readOnly = true)
    public Page<AnswerRuleModuleDTO> search(
            UUID ruleId,
            UUID sectionId,
            String moduleCode,
            String moduleName,
            Integer moduleSequence,
            Boolean active,
            Integer page,
            Integer pageSize
    ) {

        PageRequest pr = pageable(page, pageSize);
        Specification<RuleModule> spec = Specification.allOf(
                RuleModuleSpecs.byRuleId(ruleId),
                RuleModuleSpecs.bySectionId(sectionId),
                RuleModuleSpecs.moduleCodeContains(moduleCode),
                RuleModuleSpecs.moduleNameContains(moduleName),
                RuleModuleSpecs.moduleSequenceEq(moduleSequence),
                RuleModuleSpecs.activeEq(active)
        );


        var result = repository.findAll(spec, pr);
        return result.map(mapper::toDTO);
    }

    // UPDATE (PATCH semantics via UpdateDTO)
    @Transactional
    public AnswerRuleModuleDTO update(UUID id, UpdateRuleModuleDTO dto) {

        var existing = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("RuleModule not found: " + id));

        validator.validateUpdate(existing, dto);
        mapper.updateFromDTO(dto, existing);

        var saved = repository.save(existing);
        return mapper.toDTO(saved);
    }

    // DELETE
    @Transactional
    public void delete(UUID id) {

        var existing = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("RuleModule not found: " + id));

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
