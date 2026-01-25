package com.vectorlabs.validator;

import com.vectorlabs.exception.DoubleRegisterException;
import com.vectorlabs.exception.InvalidFieldException;
import com.vectorlabs.exception.MissingRequiredFieldException;
import com.vectorlabs.dto.rule.UpdateRuleDTO;
import com.vectorlabs.repository.RuleRepository;
import com.vectorlabs.model.Rule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RuleValidator {

    private final RuleRepository repository;

    // CREATE
    public void validateCreation(Rule entity) {

        if (!notBlank(entity.getCode())) {
            throw new MissingRequiredFieldException("code is required.");
        }
        if (entity.getCode().length() > 50) {
            throw new InvalidFieldException("code max length is 50.");
        }

        if (!notBlank(entity.getTitle())) {
            throw new MissingRequiredFieldException("title is required.");
        }
        if (entity.getTitle().length() > 200) {
            throw new InvalidFieldException("title max length is 200.");
        }

        if (entity.getDescription() != null && entity.getDescription().length() > 2000) {
            throw new InvalidFieldException("description max length is 2000.");
        }

        // Como você decidiu não versionar / não mudar code no update,
        // a unicidade fica apenas por code
        assertUniqueOnCreate(entity);
    }

    // UPDATE (sem code)
    public void validateUpdate(Rule before, UpdateRuleDTO dto) {

        if (dto.title() != null && dto.title().length() > 200) {
            throw new InvalidFieldException("title max length is 200.");
        }

        if (dto.description() != null && dto.description().length() > 2000) {
            throw new InvalidFieldException("description max length is 2000.");
        }

        // Não tem validação de unicidade aqui porque code não muda.
        // Se algum dia você voltar a permitir editar code, aí sim reativa.
    }

    // ---------- helpers ----------
    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private void assertUniqueOnCreate(Rule entity) {
        boolean exists = repository.existsByCodeIgnoreCase(entity.getCode());
        if (exists) {
            throw new DoubleRegisterException("Duplicate Rule: code already exists.");
        }
    }
}
