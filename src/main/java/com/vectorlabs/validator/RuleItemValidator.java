package com.vectorlabs.validator;

import com.vectorlabs.dto.rule.ruleitem.UpdateRuleItemDTO;
import com.vectorlabs.exception.DoubleRegisterException;
import com.vectorlabs.exception.InvalidFieldException;
import com.vectorlabs.exception.MissingRequiredFieldException;
import com.vectorlabs.model.RuleItem;
import com.vectorlabs.repository.RuleItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RuleItemValidator {

    private final RuleItemRepository repository;

    // CREATE
    public void validateCreation(RuleItem entity) {

        if (entity.getModule() == null) {
            throw new MissingRequiredFieldException("RuleModule is required.");
        }
        if (!notBlank(entity.getItemCode())) {
            throw new MissingRequiredFieldException("itemCode is required.");
        }
        if (entity.getItemCode().length() > 50) {
            throw new InvalidFieldException("itemCode max length is 50.");
        }
        if (entity.getDescription() != null && entity.getDescription().length() > 4000) {
            throw new MissingRequiredFieldException("description max length is 4000.");
        }
        assertUniqueOnCreate(entity);
    }
    // UPDATE (PATCH)
    public void validateUpdate(RuleItem before, UpdateRuleItemDTO dto) {


        if (dto.itemCode() != null) {
            if (!notBlank(dto.itemCode())) {
                throw new MissingRequiredFieldException("itemCode cannot be blank.");
            }
            if (dto.itemCode().length() > 50) {
                throw new InvalidFieldException("itemCode max length is 50.");
            }
        }
        if (dto.description() != null && dto.description().length() > 4000) {
            throw new InvalidFieldException("description max length is 4000.");
        }
        assertUniqueOnUpdate(before, dto);
    }
    // DELETE
    public void validateDelete(RuleItem entity) {

        if (repository.existsByParent_Id(entity.getId())) {
            throw new MissingRequiredFieldException("Cannot delete RuleItem that has child items.");
        }
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private void assertUniqueOnCreate(RuleItem entity) {
        boolean exists = repository.existsByModule_IdAndItemCode(
                entity.getModule().getId(),
                entity.getItemCode()
        );
        if (exists) {
            throw new DoubleRegisterException(
                    "Duplicate RuleItem: itemCode already exists in this module."
            );
        }
    }
    private void assertUniqueOnUpdate(RuleItem before, UpdateRuleItemDTO dto) {
        String effectiveCode = dto.itemCode() != null ? dto.itemCode() : before.getItemCode();
        boolean exists = repository.existsByModule_IdAndItemCodeAndIdNot(
                before.getModule().getId(),
                effectiveCode,
                before.getId()
        );
        if (exists) {
            throw new DoubleRegisterException(
                    "Duplicate RuleItem: itemCode already exists in this module."
            );
        }
    }
}

