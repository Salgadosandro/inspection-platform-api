package com.vectorlabs.validator;

import com.vectorlabs.dto.rule.ruleitem.UpdateRuleItemDTO;
import com.vectorlabs.exception.DoubleRegisterException;
import com.vectorlabs.exception.InvalidFieldException;
import com.vectorlabs.exception.MissingRequiredFieldException;
import com.vectorlabs.model.RuleItem;
import com.vectorlabs.model.RuleModule;
import com.vectorlabs.repository.RuleItemRepository;
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
class RuleItemValidatorTest {

    @Mock
    private RuleItemRepository repository;

    @InjectMocks
    private RuleItemValidator validator;

    private RuleModule module;
    private RuleItem validItem;

    @BeforeEach
    void setup() {
        module = new RuleModule();
        module.setId(UUID.randomUUID());

        validItem = new RuleItem();
        validItem.setId(UUID.randomUUID());
        validItem.setModule(module);
        validItem.setItemCode("ITM-01");
        validItem.setDescription("desc");
    }

    // ===================== CREATE =====================

    @Test
    void validateCreation_shouldPass_whenValidAndUnique() {
        when(repository.existsByModule_IdAndItemCode(module.getId(), "ITM-01")).thenReturn(false);

        assertDoesNotThrow(() -> validator.validateCreation(validItem));

        verify(repository).existsByModule_IdAndItemCode(module.getId(), "ITM-01");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void validateCreation_shouldThrow_whenModuleIsNull() {
        validItem.setModule(null);

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateCreation(validItem));

        verifyNoInteractions(repository);
    }

    @Test
    void validateCreation_shouldThrow_whenItemCodeIsNull() {
        validItem.setItemCode(null);

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateCreation(validItem));

        verifyNoInteractions(repository);
    }

    @Test
    void validateCreation_shouldThrow_whenItemCodeIsBlank() {
        validItem.setItemCode("   ");

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateCreation(validItem));

        verifyNoInteractions(repository);
    }

    @Test
    void validateCreation_shouldThrow_whenItemCodeTooLong() {
        validItem.setItemCode("A".repeat(51));

        assertThrows(InvalidFieldException.class, () -> validator.validateCreation(validItem));

        verifyNoInteractions(repository);
    }

    @Test
    void validateCreation_shouldThrow_whenDescriptionTooLong() {
        validItem.setDescription("D".repeat(4001));

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateCreation(validItem));
        // (no seu código, aqui ele lança MissingRequiredFieldException mesmo; mantive o teste fiel)

        verifyNoInteractions(repository);
    }

    @Test
    void validateCreation_shouldThrow_whenDuplicateInModule() {
        when(repository.existsByModule_IdAndItemCode(module.getId(), "ITM-01")).thenReturn(true);

        assertThrows(DoubleRegisterException.class, () -> validator.validateCreation(validItem));

        verify(repository).existsByModule_IdAndItemCode(module.getId(), "ITM-01");
    }

    // ===================== UPDATE =====================

    @Test
    void validateUpdate_shouldPass_whenNoChangesAndUnique() {
        UpdateRuleItemDTO dto = mock(UpdateRuleItemDTO.class);
        when(dto.itemCode()).thenReturn(null);
        when(dto.description()).thenReturn(null);

        when(repository.existsByModule_IdAndItemCodeAndIdNot(
                module.getId(),
                "ITM-01",
                validItem.getId()
        )).thenReturn(false);

        assertDoesNotThrow(() -> validator.validateUpdate(validItem, dto));

        verify(repository).existsByModule_IdAndItemCodeAndIdNot(module.getId(), "ITM-01", validItem.getId());
    }
    @Test
    void validateUpdate_shouldThrow_whenItemCodeProvidedButBlank() {
        UpdateRuleItemDTO dto = mock(UpdateRuleItemDTO.class);
        when(dto.itemCode()).thenReturn("   ");

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateUpdate(validItem, dto));

        verifyNoInteractions(repository);
    }


    @Test
    void validateUpdate_shouldThrow_whenItemCodeTooLong() {
        UpdateRuleItemDTO dto = mock(UpdateRuleItemDTO.class);
        when(dto.itemCode()).thenReturn("A".repeat(51));

        assertThrows(InvalidFieldException.class, () -> validator.validateUpdate(validItem, dto));

        verifyNoInteractions(repository);
    }


    @Test
    void validateUpdate_shouldThrow_whenDescriptionTooLong() {
        UpdateRuleItemDTO dto = mock(UpdateRuleItemDTO.class);
        when(dto.itemCode()).thenReturn(null);
        when(dto.description()).thenReturn("D".repeat(4001));

        assertThrows(InvalidFieldException.class, () -> validator.validateUpdate(validItem, dto));

        verifyNoInteractions(repository);
    }

    @Test
    void validateUpdate_shouldUseEffectiveCodeFromDto_whenItemCodeProvided() {
        UpdateRuleItemDTO dto = mock(UpdateRuleItemDTO.class);
        when(dto.itemCode()).thenReturn("ITM-02");
        when(dto.description()).thenReturn(null);

        when(repository.existsByModule_IdAndItemCodeAndIdNot(
                module.getId(),
                "ITM-02",
                validItem.getId()
        )).thenReturn(false);

        assertDoesNotThrow(() -> validator.validateUpdate(validItem, dto));

        verify(repository).existsByModule_IdAndItemCodeAndIdNot(module.getId(), "ITM-02", validItem.getId());
    }

    @Test
    void validateUpdate_shouldThrow_whenDuplicateDetected() {
        UpdateRuleItemDTO dto = mock(UpdateRuleItemDTO.class);
        when(dto.itemCode()).thenReturn("ITM-02");
        when(dto.description()).thenReturn(null);

        when(repository.existsByModule_IdAndItemCodeAndIdNot(
                module.getId(),
                "ITM-02",
                validItem.getId()
        )).thenReturn(true);

        assertThrows(DoubleRegisterException.class, () -> validator.validateUpdate(validItem, dto));

        verify(repository).existsByModule_IdAndItemCodeAndIdNot(module.getId(), "ITM-02", validItem.getId());
    }

    // ===================== DELETE =====================

    @Test
    void validateDelete_shouldPass_whenNoChildItems() {
        when(repository.existsByParent_Id(validItem.getId())).thenReturn(false);

        assertDoesNotThrow(() -> validator.validateDelete(validItem));

        verify(repository).existsByParent_Id(validItem.getId());
    }

    @Test
    void validateDelete_shouldThrow_whenHasChildItems() {
        when(repository.existsByParent_Id(validItem.getId())).thenReturn(true);

        assertThrows(MissingRequiredFieldException.class, () -> validator.validateDelete(validItem));

        verify(repository).existsByParent_Id(validItem.getId());
    }
}
