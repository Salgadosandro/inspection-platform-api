package com.vectorlabs.validator;

import com.vectorlabs.dto.rule.rulemodule.UpdateRuleModuleDTO;
import com.vectorlabs.exception.*;
import com.vectorlabs.model.AppUser;
import com.vectorlabs.model.RuleModule;
import com.vectorlabs.repository.RuleModuleRepository;
import com.vectorlabs.repository.RuleSectionRepository;

import com.vectorlabs.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RuleModuleValidator {

    private final RuleModuleRepository repository;
    private final RuleSectionRepository ruleSectionRepository;
    private final AppUserService userService;

    // CREATE
    public void validateCreation(RuleModule entity) {

        if (entity == null) throw new MissingRequiredFieldException("RuleModule is required.");
        if (entity.getSection() == null || entity.getSection().getId() == null) {
            throw new MissingRequiredFieldException("sectionId is required.");
        }

        UUID sectionId = entity.getSection().getId();
        if (!ruleSectionRepository.existsById(sectionId)) {
            throw new ObjectNotFound("RuleSection not found: " + sectionId);
        }

        if (!notBlank(entity.getModuleCode())) throw new MissingRequiredFieldException("moduleCode is required.");
        if (entity.getModuleCode().length() > 50) throw new InvalidFieldException("moduleCode max length is 50.");

        if (!notBlank(entity.getModuleName())) throw new MissingRequiredFieldException("moduleName is required.");
        if (entity.getModuleName().length() > 300) throw new InvalidFieldException("moduleName max length is 300.");

        if (entity.getModuleSequence() == null) throw new MissingRequiredFieldException("moduleSequence is required.");
        if (entity.getModuleSequence() < 0) throw new InvalidFieldException("moduleSequence must be >= 0.");

        assertUniqueOnCreate(entity);
    }

    // UPDATE (PATCH)
    public void validateUpdate(RuleModule before, UpdateRuleModuleDTO dto) {

        if (before == null) throw new MissingRequiredFieldException("RuleModule is required.");
        if (dto == null) throw new MissingRequiredFieldException("UpdateRuleModuleDTO is required.");

        assertImmutableOnUpdate(dto);

        if (dto.moduleCode() != null) {
            if (!notBlank(dto.moduleCode())) throw new MissingRequiredFieldException("moduleCode cannot be blank.");
            if (dto.moduleCode().length() > 50) throw new InvalidFieldException("moduleCode max length is 50.");
        }

        if (dto.moduleName() != null) {
            if (!notBlank(dto.moduleName())) throw new MissingRequiredFieldException("moduleName cannot be blank.");
            if (dto.moduleName().length() > 300) throw new InvalidFieldException("moduleName max length is 300.");
        }

        if (dto.moduleSequence() != null) {
            if (dto.moduleSequence() < 0) throw new InvalidFieldException("moduleSequence must be >= 0.");
        }

        if (dto.active() != null) {
            // ok
        }

        assertUniqueOnUpdate(before, dto);
    }

    // DELETE
    public void validateDelete(RuleModule entity) {

        if (entity == null) throw new MissingRequiredFieldException("RuleModule is required.");
        if (entity.getId() == null) throw new MissingRequiredFieldException("RuleModule id is required.");

        // Se você quiser impedir exclusão quando houver RuleItems vinculados:
        // if (ruleItemRepository.existsByModule_Id(entity.getId())) throw new MissingRequiredFieldException()("Cannot delete: module has items.");
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private void assertImmutableOnUpdate(UpdateRuleModuleDTO dto) {
        // regra: se você não quer mover módulo entre seções
        if (dto.sectionId() != null) {
            throw new MissingRequiredFieldException("Section cannot be changed after creation.");
        }
    }

    private void assertUniqueOnCreate(RuleModule entity) {
        UUID sectionId = entity.getSection().getId();
        String moduleCode = entity.getModuleCode().trim();
        boolean exists = repository.existsBySection_IdAndModuleCode(sectionId, moduleCode);
        if (exists) {
            throw new DoubleRegisterException(
                    "Duplicate RuleModule: combination (sectionId, moduleCode) already exists."
            );
        }

        boolean existsSeq = repository.existsBySection_IdAndModuleSequence(sectionId, entity.getModuleSequence());
        if (existsSeq) {
            throw new DoubleRegisterException(
                    "Duplicate RuleModule: combination (sectionId, moduleSequence) already exists."
            );
        }
    }

    private void assertUniqueOnUpdate(RuleModule before, UpdateRuleModuleDTO dto) {
        UUID sectionId = before.getSection().getId();
        String moduleCode = (dto.moduleCode() != null) ? dto.moduleCode().trim() : before.getModuleCode();
        Integer seq = (dto.moduleSequence() != null) ? dto.moduleSequence() : before.getModuleSequence();

        boolean exists = repository.existsBySection_IdAndModuleCodeAndIdNot(sectionId, moduleCode, before.getId());
        if (exists) {
            throw new DoubleRegisterException(
                    "Duplicate RuleModule: combination (sectionId, moduleCode) already exists."
            );
        }

        boolean existsSeq = repository.existsBySection_IdAndModuleSequenceAndIdNot(sectionId, seq, before.getId());
        if (existsSeq) {
            throw new DoubleRegisterException(
                    "Duplicate RuleModule: combination (sectionId, moduleSequence) already exists."
            );
        }
    }
}
