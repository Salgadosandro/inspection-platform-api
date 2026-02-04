package com.vectorlabs.validator;
import com.vectorlabs.dto.rule.UpdateRuleDTO;
import com.vectorlabs.exception.DoubleRegisterException;
import com.vectorlabs.exception.InvalidFieldException;
import com.vectorlabs.exception.MissingRequiredFieldException;
import com.vectorlabs.model.Rule;
import com.vectorlabs.repository.RuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleValidatorTest {

    @Mock
    private RuleRepository repository;

    @InjectMocks
    private RuleValidator validator;

    // -------------------------
    // helpers
    // -------------------------
    private Rule validRule() {
        Rule r = new Rule();
        r.setCode("NR-12");
        r.setTitle("Norma Regulamentadora 12");
        r.setDescription("Descrição ok");
        return r;
    }

    private String repeat(char c, int times) {
        return String.valueOf(c).repeat(Math.max(0, times));
    }

    // -------------------------
    // CREATE
    // -------------------------
    @Test
    void validateCreation_shouldThrowNpe_whenEntityIsNull_currentBehavior() {
        assertThatThrownBy(() -> validator.validateCreation(null))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(repository);
    }

    @Test
    void validateCreation_shouldThrowMissingRequiredField_whenCodeIsNull() {
        Rule r = validRule();
        r.setCode(null);

        assertThatThrownBy(() -> validator.validateCreation(r))
                .isInstanceOf(MissingRequiredFieldException.class)
                .hasMessage("code is required.");

        verifyNoInteractions(repository);
    }

    @Test
    void validateCreation_shouldThrowMissingRequiredField_whenCodeIsBlank() {
        Rule r = validRule();
        r.setCode("   ");

        assertThatThrownBy(() -> validator.validateCreation(r))
                .isInstanceOf(MissingRequiredFieldException.class)
                .hasMessage("code is required.");

        verifyNoInteractions(repository);
    }

    @Test
    void validateCreation_shouldThrowInvalidField_whenCodeTooLong() {
        Rule r = validRule();
        r.setCode(repeat('A', 51));

        assertThatThrownBy(() -> validator.validateCreation(r))
                .isInstanceOf(InvalidFieldException.class)
                .hasMessage("code max length is 50.");

        verifyNoInteractions(repository);
    }

    @Test
    void validateCreation_shouldThrowMissingRequiredField_whenTitleIsNull() {
        Rule r = validRule();
        r.setTitle(null);

        assertThatThrownBy(() -> validator.validateCreation(r))
                .isInstanceOf(MissingRequiredFieldException.class)
                .hasMessage("title is required.");

        verifyNoInteractions(repository);
    }

    @Test
    void validateCreation_shouldThrowMissingRequiredField_whenTitleIsBlank() {
        Rule r = validRule();
        r.setTitle("   ");

        assertThatThrownBy(() -> validator.validateCreation(r))
                .isInstanceOf(MissingRequiredFieldException.class)
                .hasMessage("title is required.");

        verifyNoInteractions(repository);
    }

    @Test
    void validateCreation_shouldThrowInvalidField_whenTitleTooLong() {
        Rule r = validRule();
        r.setTitle(repeat('B', 201));

        assertThatThrownBy(() -> validator.validateCreation(r))
                .isInstanceOf(InvalidFieldException.class)
                .hasMessage("title max length is 200.");

        verifyNoInteractions(repository);
    }

    @Test
    void validateCreation_shouldThrowInvalidField_whenDescriptionTooLong() {
        Rule r = validRule();
        r.setDescription(repeat('C', 2001));

        assertThatThrownBy(() -> validator.validateCreation(r))
                .isInstanceOf(InvalidFieldException.class)
                .hasMessage("description max length is 2000.");

        verifyNoInteractions(repository);
    }

    @Test
    void validateCreation_shouldThrowDoubleRegister_whenCodeAlreadyExists_ignoreCase() {
        Rule r = validRule();
        r.setCode("nr-12");

        when(repository.existsByCodeIgnoreCase("nr-12")).thenReturn(true);

        assertThatThrownBy(() -> validator.validateCreation(r))
                .isInstanceOf(DoubleRegisterException.class)
                .hasMessage("Duplicate Rule: code already exists.");

        verify(repository, times(1)).existsByCodeIgnoreCase("nr-12");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void validateCreation_shouldPass_whenValidAndUnique() {
        Rule r = validRule();

        when(repository.existsByCodeIgnoreCase("NR-12")).thenReturn(false);

        validator.validateCreation(r);

        verify(repository, times(1)).existsByCodeIgnoreCase("NR-12");
        verifyNoMoreInteractions(repository);
    }

    // -------------------------
    // UPDATE
    // -------------------------
    @Test
    void validateUpdate_shouldThrowInvalidField_whenTitleTooLong() {
        Rule before = validRule();
        UpdateRuleDTO dto = new UpdateRuleDTO(
                "T".repeat(201),
                null,
                null,
                null,
                null
        );


        assertThatThrownBy(() -> validator.validateUpdate(before, dto))
                .isInstanceOf(InvalidFieldException.class)
                .hasMessage("title max length is 200.");
    }

    @Test
    void validateUpdate_shouldThrowInvalidField_whenDescriptionTooLong() {
        Rule before = validRule();
        UpdateRuleDTO dto = new UpdateRuleDTO(
                null,                 // title
                "D".repeat(2001),      // description
                null,                 // updateOrdinance
                null,                 // updateOrdinanceDate
                null                  // active  <-- FALTAVA ESSE
        );

        assertThatThrownBy(() -> validator.validateUpdate(before, dto))
                .isInstanceOf(InvalidFieldException.class)
                .hasMessage("description max length is 2000.");
    }

    @Test
    void validateUpdate_shouldPass_whenFieldsNullOrWithinLimits() {
        Rule before = validRule();
        UpdateRuleDTO dto = new UpdateRuleDTO(
                "Novo título",          // title
                "Nova descrição",       // description
                null,                   // updateOrdinance (String)
                null,                   // updateOrdinanceDate
                true                    // active  <-- FALTAVA ESSE
        );


        validator.validateUpdate(before, dto);
        // Sem interações com repository (code não muda)
        verifyNoInteractions(repository);
    }
}
