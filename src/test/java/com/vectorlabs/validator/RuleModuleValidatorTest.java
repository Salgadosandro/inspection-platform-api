package com.vectorlabs.validator;
import com.vectorlabs.dto.rule.rulemodule.UpdateRuleModuleDTO;
import com.vectorlabs.exception.*;
import com.vectorlabs.model.RuleModule;
import com.vectorlabs.model.RuleSection;
import com.vectorlabs.repository.RuleModuleRepository;
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
class RuleModuleValidatorTest {

    @Mock private RuleModuleRepository repository;
    @Mock private RuleSectionRepository ruleSectionRepository;
    @Mock private AppUserService userService; // não usado hoje, mas está no construtor

    @InjectMocks
    private RuleModuleValidator validator;

    private UUID sectionId;
    private RuleSection section;
    private RuleModule validModule;

    @BeforeEach
    void setup() {
        sectionId = UUID.randomUUID();

        section = new RuleSection();
        section.setId(sectionId);

        validModule = new RuleModule();
        validModule.setId(UUID.randomUUID());
        validModule.setSection(section);
        validModule.setModuleCode("MOD-01");
        validModule.setModuleName("Module name");
        validModule.setModuleSequence(1);
        validModule.setActive(true);
    }

    // ========================= CREATE =========================

    @Test
    void validateCreation_shouldPass_whenValidAndUnique() {
        when(ruleSectionRepository.existsById(sectionId)).thenReturn(true);
        when(repository.existsBySection_IdAndModuleCode(sectionId, "MOD-01")).thenReturn(false);
        when(repository.existsBySection_IdAndModuleSequence(sectionId, 1)).thenReturn(false);

        assertDoesNotThrow(() -> validator.validateCreation(validModule));

        verify(ruleSectionRepository).existsById(sectionId);
        verify(repository).existsBySection_IdAndModuleCode(sectionId, "MOD-01");
        verify(repository).existsBySection_IdAndModuleSequence(sectionId, 1);
        verifyNoMoreInteractions(ruleSectionRepository, repository, userService);
    }

    @Test
    void validateCreation_shouldThrow_whenEntityNull() {
        assertThrows(MissingRequiredFieldException.class, () -> validator.validateCreation(null));
        verifyNoInteractions(repository, ruleSectionRepository, userService);
    }

    @Test
    void validateCreation_shouldThrow_whenSectionIdMissing() {
        validModule.setSection(null);

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateCreation(validModule));

        verifyNoInteractions(repository, ruleSectionRepository, userService);
    }

    @Test
    void validateCreation_shouldThrow_whenSectionNotFound() {
        when(ruleSectionRepository.existsById(sectionId)).thenReturn(false);

        assertThrows(ObjectNotFound.class, () -> validator.validateCreation(validModule));

        verify(ruleSectionRepository).existsById(sectionId);
        verifyNoMoreInteractions(ruleSectionRepository);
        verifyNoInteractions(repository, userService);
    }

    @Test
    void validateCreation_shouldThrow_whenModuleCodeBlank() {
        validModule.setModuleCode("   ");
        when(ruleSectionRepository.existsById(sectionId)).thenReturn(true);

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateCreation(validModule));

        verify(ruleSectionRepository).existsById(sectionId);
        verifyNoMoreInteractions(ruleSectionRepository);
        verifyNoInteractions(repository, userService);
    }

    @Test
    void validateCreation_shouldThrow_whenModuleCodeTooLong() {
        validModule.setModuleCode("A".repeat(51));
        when(ruleSectionRepository.existsById(sectionId)).thenReturn(true);

        assertThrows(InvalidFieldException.class, () -> validator.validateCreation(validModule));

        verify(ruleSectionRepository).existsById(sectionId);
        verifyNoMoreInteractions(ruleSectionRepository);
        verifyNoInteractions(repository, userService);
    }

    @Test
    void validateCreation_shouldThrow_whenModuleNameBlank() {
        validModule.setModuleName("  ");
        when(ruleSectionRepository.existsById(sectionId)).thenReturn(true);

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateCreation(validModule));

        verify(ruleSectionRepository).existsById(sectionId);
        verifyNoMoreInteractions(ruleSectionRepository);
        verifyNoInteractions(repository, userService);
    }

    @Test
    void validateCreation_shouldThrow_whenModuleNameTooLong() {
        validModule.setModuleName("N".repeat(301));
        when(ruleSectionRepository.existsById(sectionId)).thenReturn(true);

        assertThrows(InvalidFieldException.class, () -> validator.validateCreation(validModule));

        verify(ruleSectionRepository).existsById(sectionId);
        verifyNoMoreInteractions(ruleSectionRepository);
        verifyNoInteractions(repository, userService);
    }

    @Test
    void validateCreation_shouldThrow_whenModuleSequenceNull() {
        validModule.setModuleSequence(null);
        when(ruleSectionRepository.existsById(sectionId)).thenReturn(true);

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateCreation(validModule));

        verify(ruleSectionRepository).existsById(sectionId);
        verifyNoMoreInteractions(ruleSectionRepository);
        verifyNoInteractions(repository, userService);
    }

    @Test
    void validateCreation_shouldThrow_whenModuleSequenceNegative() {
        validModule.setModuleSequence(-1);
        when(ruleSectionRepository.existsById(sectionId)).thenReturn(true);

        assertThrows(InvalidFieldException.class, () -> validator.validateCreation(validModule));

        verify(ruleSectionRepository).existsById(sectionId);
        verifyNoMoreInteractions(ruleSectionRepository);
        verifyNoInteractions(repository, userService);
    }

    @Test
    void validateCreation_shouldThrow_whenDuplicateModuleCodeInSection() {
        when(ruleSectionRepository.existsById(sectionId)).thenReturn(true);
        when(repository.existsBySection_IdAndModuleCode(sectionId, "MOD-01")).thenReturn(true);

        assertThrows(DoubleRegisterException.class, () -> validator.validateCreation(validModule));

        verify(ruleSectionRepository).existsById(sectionId);
        verify(repository).existsBySection_IdAndModuleCode(sectionId, "MOD-01");
        verifyNoMoreInteractions(ruleSectionRepository, repository);
        verifyNoInteractions(userService);
    }

    @Test
    void validateCreation_shouldThrow_whenDuplicateSequenceInSection() {
        when(ruleSectionRepository.existsById(sectionId)).thenReturn(true);
        when(repository.existsBySection_IdAndModuleCode(sectionId, "MOD-01")).thenReturn(false);
        when(repository.existsBySection_IdAndModuleSequence(sectionId, 1)).thenReturn(true);

        assertThrows(DoubleRegisterException.class, () -> validator.validateCreation(validModule));

        verify(ruleSectionRepository).existsById(sectionId);
        verify(repository).existsBySection_IdAndModuleCode(sectionId, "MOD-01");
        verify(repository).existsBySection_IdAndModuleSequence(sectionId, 1);
        verifyNoMoreInteractions(ruleSectionRepository, repository);
        verifyNoInteractions(userService);
    }

    // ========================= UPDATE =========================

    @Test
    void validateUpdate_shouldPass_whenNoChangesAndUnique() {
        UpdateRuleModuleDTO dto = mock(UpdateRuleModuleDTO.class);
        when(dto.moduleCode()).thenReturn(null);
        when(dto.moduleName()).thenReturn(null);
        when(dto.moduleSequence()).thenReturn(null);
        when(dto.active()).thenReturn(null);

        when(repository.existsBySection_IdAndModuleCodeAndIdNot(sectionId, "MOD-01", validModule.getId()))
                .thenReturn(false);
        when(repository.existsBySection_IdAndModuleSequenceAndIdNot(sectionId, 1, validModule.getId()))
                .thenReturn(false);

        assertDoesNotThrow(() -> validator.validateUpdate(validModule, dto));

        verify(repository).existsBySection_IdAndModuleCodeAndIdNot(sectionId, "MOD-01", validModule.getId());
        verify(repository).existsBySection_IdAndModuleSequenceAndIdNot(sectionId, 1, validModule.getId());
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(ruleSectionRepository, userService);
    }

    @Test
    void validateUpdate_shouldThrow_whenBeforeNull() {
        UpdateRuleModuleDTO dto = mock(UpdateRuleModuleDTO.class);

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateUpdate(null, dto));

        verifyNoInteractions(repository, ruleSectionRepository, userService);
    }

    @Test
    void validateUpdate_shouldThrow_whenDtoNull() {
        assertThrows(MissingRequiredFieldException.class, () -> validator.validateUpdate(validModule, null));
        verifyNoInteractions(repository, ruleSectionRepository, userService);
    }

    @Test
    void validateUpdate_shouldThrow_whenModuleCodeProvidedButBlank() {
        UpdateRuleModuleDTO dto = mock(UpdateRuleModuleDTO.class);
        when(dto.moduleCode()).thenReturn("   ");

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateUpdate(validModule, dto));

        verifyNoInteractions(repository, ruleSectionRepository, userService);
    }

    @Test
    void validateUpdate_shouldThrow_whenModuleCodeTooLong() {
        UpdateRuleModuleDTO dto = mock(UpdateRuleModuleDTO.class);
        when(dto.moduleCode()).thenReturn("A".repeat(51));

        assertThrows(InvalidFieldException.class, () -> validator.validateUpdate(validModule, dto));

        verifyNoInteractions(repository, ruleSectionRepository, userService);
    }

    @Test
    void validateUpdate_shouldThrow_whenModuleNameProvidedButBlank() {
        UpdateRuleModuleDTO dto = mock(UpdateRuleModuleDTO.class);
        when(dto.moduleCode()).thenReturn(null);
        when(dto.moduleName()).thenReturn("   ");

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateUpdate(validModule, dto));

        verifyNoInteractions(repository, ruleSectionRepository, userService);
    }

    @Test
    void validateUpdate_shouldThrow_whenModuleNameTooLong() {
        UpdateRuleModuleDTO dto = mock(UpdateRuleModuleDTO.class);
        when(dto.moduleCode()).thenReturn(null);
        when(dto.moduleName()).thenReturn("N".repeat(301));

        assertThrows(InvalidFieldException.class, () -> validator.validateUpdate(validModule, dto));

        verifyNoInteractions(repository, ruleSectionRepository, userService);
    }

    @Test
    void validateUpdate_shouldThrow_whenModuleSequenceNegative() {
        UpdateRuleModuleDTO dto = mock(UpdateRuleModuleDTO.class);
        when(dto.moduleCode()).thenReturn(null);
        when(dto.moduleName()).thenReturn(null);
        when(dto.moduleSequence()).thenReturn(-1);

        assertThrows(InvalidFieldException.class, () -> validator.validateUpdate(validModule, dto));

        verifyNoInteractions(repository, ruleSectionRepository, userService);
    }

    @Test
    void validateUpdate_shouldThrow_whenDuplicateModuleCodeDetected() {
        UpdateRuleModuleDTO dto = mock(UpdateRuleModuleDTO.class);
        when(dto.moduleCode()).thenReturn("MOD-02");
        when(dto.moduleName()).thenReturn(null);
        when(dto.moduleSequence()).thenReturn(null);
        when(dto.active()).thenReturn(null);

        when(repository.existsBySection_IdAndModuleCodeAndIdNot(sectionId, "MOD-02", validModule.getId()))
                .thenReturn(true);

        assertThrows(DoubleRegisterException.class, () -> validator.validateUpdate(validModule, dto));

        verify(repository).existsBySection_IdAndModuleCodeAndIdNot(sectionId, "MOD-02", validModule.getId());
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(ruleSectionRepository, userService);
    }

    @Test
    void validateUpdate_shouldThrow_whenDuplicateSequenceDetected() {
        UpdateRuleModuleDTO dto = mock(UpdateRuleModuleDTO.class);
        when(dto.moduleCode()).thenReturn(null);
        when(dto.moduleName()).thenReturn(null);
        when(dto.moduleSequence()).thenReturn(2);
        when(dto.active()).thenReturn(null);

        when(repository.existsBySection_IdAndModuleCodeAndIdNot(sectionId, "MOD-01", validModule.getId()))
                .thenReturn(false);
        when(repository.existsBySection_IdAndModuleSequenceAndIdNot(sectionId, 2, validModule.getId()))
                .thenReturn(true);

        assertThrows(DoubleRegisterException.class, () -> validator.validateUpdate(validModule, dto));

        verify(repository).existsBySection_IdAndModuleCodeAndIdNot(sectionId, "MOD-01", validModule.getId());
        verify(repository).existsBySection_IdAndModuleSequenceAndIdNot(sectionId, 2, validModule.getId());
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(ruleSectionRepository, userService);
    }

    // ========================= DELETE =========================

    @Test
    void validateDelete_shouldPass_whenEntityAndIdPresent() {
        assertDoesNotThrow(() -> validator.validateDelete(validModule));
        verifyNoInteractions(repository, ruleSectionRepository, userService);
    }

    @Test
    void validateDelete_shouldThrow_whenEntityNull() {
        assertThrows(MissingRequiredFieldException.class, () -> validator.validateDelete(null));
        verifyNoInteractions(repository, ruleSectionRepository, userService);
    }

    @Test
    void validateDelete_shouldThrow_whenIdNull() {
        validModule.setId(null);

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateDelete(validModule));

        verifyNoInteractions(repository, ruleSectionRepository, userService);
    }
}
