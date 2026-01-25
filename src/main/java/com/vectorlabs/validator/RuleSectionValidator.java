package com.vectorlabs.validator;

import com.vectorlabs.dto.rule.ruledection.UpdateRuleSectionDTO;

import com.vectorlabs.exception.*;
import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.RuleSection;
import com.vectorlabs.repository.RuleModuleRepository;
import com.vectorlabs.repository.RuleRepository;
import com.vectorlabs.repository.RuleSectionRepository;

import com.vectorlabs.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RuleSectionValidator {

    private final RuleSectionRepository repository;
    private final RuleRepository ruleRepository;
    private final RuleModuleRepository ruleModuleRepository;
    private final AppUserService userService;

    // REATE
    public void validateCreation(RuleSection entity) {

        if (entity == null) throw new MissingRequiredFieldException("RuleSection is required.");
        if (entity.getRule() == null || entity.getRule().getId() == null) {
            throw new MissingRequiredFieldException("ruleId is required.");
        }
        UUID ruleId = entity.getRule().getId();
        if (!ruleRepository.existsById(ruleId)) {
            throw new ObjectNotFound("Rule not found: " + ruleId);
        }

        if (!notBlank(entity.getCode())) throw new MissingRequiredFieldException("sectionCode is required.");
        if (entity.getCode().length() > 50) throw new InvalidFieldException("sectionCode max length is 50.");

        if (!notBlank(entity.getName())) throw new MissingRequiredFieldException("sectionName is required.");
        if (entity.getName().length() > 300) throw new InvalidFieldException("sectionName max length is 300.");

        if (entity.getSequence() == null) throw new MissingRequiredFieldException("sequence is required.");
        if (entity.getSequence() < 0) throw new InvalidFieldException("sequence must be >= 0.");

        assertUniqueOnCreate(entity);
    }

    // UPDATE (PATCH
    public void validateUpdate(RuleSection before, UpdateRuleSectionDTO dto) {

        if (before == null) throw new MissingRequiredFieldException("RuleSection is required.");
        if (dto == null) throw new MissingRequiredFieldException("UpdateRuleSectionDTO is required.");

        if (dto.code() != null) {
            if (!notBlank(dto.code())) throw new MissingRequiredFieldException("sectionCode cannot be blank.");
            if (dto.code().length() > 50) throw new InvalidFieldException("sectionCode max length is 50.");
        }

        if (dto.name() != null) {
            if (!notBlank(dto.name())) throw new MissingRequiredFieldException("sectionName cannot be blank.");
            if (dto.name().length() > 300) throw new MissingRequiredFieldException("sectionName max length is 300.");
        }

        if (dto.sequence() != null) {
            if (dto.sequence() < 0) throw new MissingRequiredFieldException("sequence must be >= 0.");
        }

        assertUniqueOnUpdate(before, dto);
    }
    // DELETE
    public void validateDelete(RuleSection entity) {

        if (entity == null) throw new MissingRequiredFieldException("RuleSection is required.");
        if (entity.getId() == null) throw new MissingRequiredFieldException("RuleSection id is required.");
        if (ruleModuleRepository.existsBySection_Id(entity.getId())) throw new MissingRequiredFieldException("Cannot delete: section has modules.");
    }
    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }


    private void assertUniqueOnCreate(RuleSection entity) {
        UUID ruleId = entity.getRule().getId();
        String code = entity.getCode().trim();

        boolean exists = repository.existsByRule_IdAndCode(ruleId, code);
        if (exists) {
            throw new DoubleRegisterException(
                    "Duplicate RuleSection: combination (ruleId, sectionCode) already exists."
            );
        }

        boolean existsSeq = repository.existsByRule_IdAndSequence(ruleId, entity.getSequence());
        if (existsSeq) {
            throw new DoubleRegisterException(
                    "Duplicate RuleSection: combination (ruleId, sequence) already exists."
            );
        }
    }

    private void assertUniqueOnUpdate(RuleSection before, UpdateRuleSectionDTO dto) {
        UUID ruleId = before.getRule().getId();
        String code = (dto.code() != null) ? dto.code().trim() : before.getCode();
        Integer seq = (dto.sequence() != null) ? dto.sequence() : before.getSequence();

        boolean exists = repository.existsByRule_IdAndCodeAndIdNot(ruleId, code, before.getId());
        if (exists) {
            throw new DoubleRegisterException(
                    "Duplicate RuleSection: combination (ruleId, sectionCode) already exists."
            );
        }

        boolean existsSeq = repository.existsByRule_IdAndSequenceAndIdNot(ruleId, seq, before.getId());
        if (existsSeq) {
            throw new DoubleRegisterException(
                    "Duplicate RuleSection: combination (ruleId, sequence) already exists."
            );
        }
    }
}

