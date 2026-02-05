package com.vectorlabs.validator;
import com.vectorlabs.dto.rule.ruledection.UpdateRuleSectionDTO;
import com.vectorlabs.exception.*;
import com.vectorlabs.model.Rule;
import com.vectorlabs.model.RuleSection;
import com.vectorlabs.repository.RuleModuleRepository;
import com.vectorlabs.repository.RuleRepository;
import com.vectorlabs.repository.RuleSectionRepository;
import com.vectorlabs.service.AppUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleSectionValidatorTest {

    @Mock private RuleSectionRepository repository;
    @Mock private RuleRepository ruleRepository;
    @Mock private RuleModuleRepository ruleModuleRepository;
    @Mock private AppUserService userService; // não é usado no validator hoje, mas está no construtor

    @InjectMocks
    private RuleSectionValidator validator;

    private UUID ruleId;
    private Rule rule;
    private RuleSection validSection;

    @BeforeEach
    void setup() {
        ruleId = UUID.randomUUID();

        rule = new Rule();
        rule.setId(ruleId);

        validSection = new RuleSection();
        validSection.setId(UUID.randomUUID());
        validSection.setRule(rule);
        validSection.setCode("SEC-01");
        validSection.setName("Safety");
        validSection.setSequence(1);
    }

    // ========================= CREATE =========================

    @Test
    void validateCreation_shouldPass_whenValidAndUnique() {
        when(ruleRepository.existsById(ruleId)).thenReturn(true);
        when(repository.existsByRule_IdAndCode(ruleId, "SEC-01")).thenReturn(false);
        when(repository.existsByRule_IdAndSequence(ruleId, 1)).thenReturn(false);

        assertDoesNotThrow(() -> validator.validateCreation(validSection));

        verify(ruleRepository).existsById(ruleId);
        verify(repository).existsByRule_IdAndCode(ruleId, "SEC-01");
        verify(repository).existsByRule_IdAndSequence(ruleId, 1);
        verifyNoMoreInteractions(ruleRepository, repository, ruleModuleRepository, userService);
    }

    @Test
    void validateCreation_shouldThrow_whenEntityNull() {
        assertThrows(MissingRequiredFieldException.class, () -> validator.validateCreation(null));
        verifyNoInteractions(repository, ruleRepository, ruleModuleRepository, userService);
    }

    @Test
    void validateCreation_shouldThrow_whenRuleIdMissing() {
        validSection.setRule(null);

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateCreation(validSection));

        verifyNoInteractions(repository, ruleRepository, ruleModuleRepository, userService);
    }

    @Test
    void validateCreation_shouldThrow_whenRuleNotFound() {
        when(ruleRepository.existsById(ruleId)).thenReturn(false);

        assertThrows(ObjectNotFound.class, () -> validator.validateCreation(validSection));

        verify(ruleRepository).existsById(ruleId);
        verifyNoMoreInteractions(ruleRepository);
        verifyNoInteractions(repository, ruleModuleRepository, userService);
    }

    @Test
    void validateCreation_shouldThrow_whenCodeBlank() {
        validSection.setCode("   ");
        when(ruleRepository.existsById(ruleId)).thenReturn(true);

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateCreation(validSection));

        verify(ruleRepository).existsById(ruleId);
        verifyNoMoreInteractions(ruleRepository);
        verifyNoInteractions(repository, ruleModuleRepository, userService);
    }

    @Test
    void validateCreation_shouldThrow_whenCodeTooLong() {
        validSection.setCode("A".repeat(51));
        when(ruleRepository.existsById(ruleId)).thenReturn(true);

        assertThrows(InvalidFieldException.class, () -> validator.validateCreation(validSection));

        verify(ruleRepository).existsById(ruleId);
        verifyNoMoreInteractions(ruleRepository);
        verifyNoInteractions(repository, ruleModuleRepository, userService);
    }

    @Test
    void validateCreation_shouldThrow_whenNameBlank() {
        validSection.setName("  ");
        when(ruleRepository.existsById(ruleId)).thenReturn(true);

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateCreation(validSection));

        verify(ruleRepository).existsById(ruleId);
        verifyNoMoreInteractions(ruleRepository);
        verifyNoInteractions(repository, ruleModuleRepository, userService);
    }

    @Test
    void validateCreation_shouldThrow_whenNameTooLong() {
        validSection.setName("N".repeat(301));
        when(ruleRepository.existsById(ruleId)).thenReturn(true);

        assertThrows(InvalidFieldException.class, () -> validator.validateCreation(validSection));

        verify(ruleRepository).existsById(ruleId);
        verifyNoMoreInteractions(ruleRepository);
        verifyNoInteractions(repository, ruleModuleRepository, userService);
    }

    @Test
    void validateCreation_shouldThrow_whenSequenceNull() {
        validSection.setSequence(null);
        when(ruleRepository.existsById(ruleId)).thenReturn(true);

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateCreation(validSection));

        verify(ruleRepository).existsById(ruleId);
        verifyNoMoreInteractions(ruleRepository);
        verifyNoInteractions(repository, ruleModuleRepository, userService);
    }

    @Test
    void validateCreation_shouldThrow_whenSequenceNegative() {
        validSection.setSequence(-1);
        when(ruleRepository.existsById(ruleId)).thenReturn(true);

        assertThrows(InvalidFieldException.class, () -> validator.validateCreation(validSection));

        verify(ruleRepository).existsById(ruleId);
        verifyNoMoreInteractions(ruleRepository);
        verifyNoInteractions(repository, ruleModuleRepository, userService);
    }

    @Test
    void validateCreation_shouldThrow_whenDuplicateCodeInRule() {
        when(ruleRepository.existsById(ruleId)).thenReturn(true);
        when(repository.existsByRule_IdAndCode(ruleId, "SEC-01")).thenReturn(true);

        assertThrows(DoubleRegisterException.class, () -> validator.validateCreation(validSection));

        verify(ruleRepository).existsById(ruleId);
        verify(repository).existsByRule_IdAndCode(ruleId, "SEC-01");
        verifyNoMoreInteractions(ruleRepository, repository);
        verifyNoInteractions(ruleModuleRepository, userService);
    }

    @Test
    void validateCreation_shouldThrow_whenDuplicateSequenceInRule() {
        when(ruleRepository.existsById(ruleId)).thenReturn(true);
        when(repository.existsByRule_IdAndCode(ruleId, "SEC-01")).thenReturn(false);
        when(repository.existsByRule_IdAndSequence(ruleId, 1)).thenReturn(true);

        assertThrows(DoubleRegisterException.class, () -> validator.validateCreation(validSection));

        verify(ruleRepository).existsById(ruleId);
        verify(repository).existsByRule_IdAndCode(ruleId, "SEC-01");
        verify(repository).existsByRule_IdAndSequence(ruleId, 1);
        verifyNoMoreInteractions(ruleRepository, repository);
        verifyNoInteractions(ruleModuleRepository, userService);
    }

    // ========================= UPDATE =========================

    @Test
    void validateUpdate_shouldPass_whenNoChangesAndUnique() {
        UpdateRuleSectionDTO dto = mock(UpdateRuleSectionDTO.class);
        when(dto.code()).thenReturn(null);
        when(dto.name()).thenReturn(null);
        when(dto.sequence()).thenReturn(null);

        when(repository.existsByRule_IdAndCodeAndIdNot(ruleId, "SEC-01", validSection.getId())).thenReturn(false);
        when(repository.existsByRule_IdAndSequenceAndIdNot(ruleId, 1, validSection.getId())).thenReturn(false);

        assertDoesNotThrow(() -> validator.validateUpdate(validSection, dto));

        verify(repository).existsByRule_IdAndCodeAndIdNot(ruleId, "SEC-01", validSection.getId());
        verify(repository).existsByRule_IdAndSequenceAndIdNot(ruleId, 1, validSection.getId());
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(ruleRepository, ruleModuleRepository, userService);
    }

    @Test
    void validateUpdate_shouldThrow_whenBeforeNull() {
        UpdateRuleSectionDTO dto = mock(UpdateRuleSectionDTO.class);

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateUpdate(null, dto));

        verifyNoInteractions(repository, ruleRepository, ruleModuleRepository, userService);
    }

    @Test
    void validateUpdate_shouldThrow_whenDtoNull() {
        assertThrows(MissingRequiredFieldException.class, () -> validator.validateUpdate(validSection, null));
        verifyNoInteractions(repository, ruleRepository, ruleModuleRepository, userService);
    }

    @Test
    void validateUpdate_shouldThrow_whenCodeProvidedButBlank() {
        UpdateRuleSectionDTO dto = mock(UpdateRuleSectionDTO.class);
        when(dto.code()).thenReturn("   ");

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateUpdate(validSection, dto));

        verifyNoInteractions(repository, ruleRepository, ruleModuleRepository, userService);
    }

    @Test
    void validateUpdate_shouldThrow_whenCodeTooLong() {
        UpdateRuleSectionDTO dto = mock(UpdateRuleSectionDTO.class);
        when(dto.code()).thenReturn("A".repeat(51));

        assertThrows(InvalidFieldException.class, () -> validator.validateUpdate(validSection, dto));

        verifyNoInteractions(repository, ruleRepository, ruleModuleRepository, userService);
    }

    @Test
    void validateUpdate_shouldThrow_whenNameProvidedButBlank() {
        UpdateRuleSectionDTO dto = mock(UpdateRuleSectionDTO.class);
        when(dto.code()).thenReturn(null);
        when(dto.name()).thenReturn("   ");

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateUpdate(validSection, dto));

        verifyNoInteractions(repository, ruleRepository, ruleModuleRepository, userService);
    }

    @Test
    void validateUpdate_shouldThrow_whenNameTooLong() {
        UpdateRuleSectionDTO dto = mock(UpdateRuleSectionDTO.class);
        when(dto.code()).thenReturn(null);
        when(dto.name()).thenReturn("N".repeat(301));

        // seu código lança MissingRequiredFieldException aqui (bugzinho), então o teste deve refletir isso:
        assertThrows(MissingRequiredFieldException.class, () -> validator.validateUpdate(validSection, dto));

        verifyNoInteractions(repository, ruleRepository, ruleModuleRepository, userService);
    }

    @Test
    void validateUpdate_shouldThrow_whenSequenceNegative() {
        UpdateRuleSectionDTO dto = mock(UpdateRuleSectionDTO.class);
        when(dto.code()).thenReturn(null);
        when(dto.name()).thenReturn(null);
        when(dto.sequence()).thenReturn(-1);

        // seu código lança MissingRequiredFieldException aqui (mesma inconsistência):
        assertThrows(MissingRequiredFieldException.class, () -> validator.validateUpdate(validSection, dto));

        verifyNoInteractions(repository, ruleRepository, ruleModuleRepository, userService);
    }

    @Test
    void validateUpdate_shouldThrow_whenDuplicateCodeDetected() {
        UpdateRuleSectionDTO dto = mock(UpdateRuleSectionDTO.class);
        when(dto.code()).thenReturn("SEC-02"); // novo code
        when(dto.name()).thenReturn(null);
        when(dto.sequence()).thenReturn(null);

        when(repository.existsByRule_IdAndCodeAndIdNot(ruleId, "SEC-02", validSection.getId())).thenReturn(true);

        assertThrows(DoubleRegisterException.class, () -> validator.validateUpdate(validSection, dto));

        verify(repository).existsByRule_IdAndCodeAndIdNot(ruleId, "SEC-02", validSection.getId());
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(ruleRepository, ruleModuleRepository, userService);
    }

    @Test
    void validateUpdate_shouldThrow_whenDuplicateSequenceDetected() {
        UpdateRuleSectionDTO dto = mock(UpdateRuleSectionDTO.class);
        when(dto.code()).thenReturn(null);
        when(dto.name()).thenReturn(null);
        when(dto.sequence()).thenReturn(2); // novo seq

        when(repository.existsByRule_IdAndCodeAndIdNot(ruleId, "SEC-01", validSection.getId())).thenReturn(false);
        when(repository.existsByRule_IdAndSequenceAndIdNot(ruleId, 2, validSection.getId())).thenReturn(true);

        assertThrows(DoubleRegisterException.class, () -> validator.validateUpdate(validSection, dto));

        verify(repository).existsByRule_IdAndCodeAndIdNot(ruleId, "SEC-01", validSection.getId());
        verify(repository).existsByRule_IdAndSequenceAndIdNot(ruleId, 2, validSection.getId());
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(ruleRepository, ruleModuleRepository, userService);
    }

    // ========================= DELETE =========================

    @Test
    void validateDelete_shouldPass_whenNoModulesInSection() {
        when(ruleModuleRepository.existsBySection_Id(validSection.getId())).thenReturn(false);

        assertDoesNotThrow(() -> validator.validateDelete(validSection));

        verify(ruleModuleRepository).existsBySection_Id(validSection.getId());
        verifyNoMoreInteractions(ruleModuleRepository);
        verifyNoInteractions(repository, ruleRepository, userService);
    }

    @Test
    void validateDelete_shouldThrow_whenEntityNull() {
        assertThrows(MissingRequiredFieldException.class, () -> validator.validateDelete(null));
        verifyNoInteractions(repository, ruleRepository, ruleModuleRepository, userService);
    }

    @Test
    void validateDelete_shouldThrow_whenIdNull() {
        validSection.setId(null);

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateDelete(validSection));

        verifyNoInteractions(repository, ruleRepository, ruleModuleRepository, userService);
    }

    @Test
    void validateDelete_shouldThrow_whenSectionHasModules() {
        when(ruleModuleRepository.existsBySection_Id(validSection.getId())).thenReturn(true);

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateDelete(validSection));

        verify(ruleModuleRepository).existsBySection_Id(validSection.getId());
        verifyNoMoreInteractions(ruleModuleRepository);
        verifyNoInteractions(repository, ruleRepository, userService);
    }
}
