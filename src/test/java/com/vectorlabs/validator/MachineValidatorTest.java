package com.vectorlabs.validator;

import com.vectorlabs.dto.machine.UpdateMachineDTO;
import com.vectorlabs.exception.InvalidFieldException;
import com.vectorlabs.exception.MissingRequiredFieldException;
import com.vectorlabs.model.Machine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MachineValidatorTest {

    private MachineValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MachineValidator();
    }

    // =================== CREATE ===================

    @Test
    void validateCreation_shouldThrow_whenEntityIsNull() {
        assertThatThrownBy(() -> validator.validateCreation(null))
                .isInstanceOf(MissingRequiredFieldException.class);
    }

    @Test
    void validateCreation_shouldThrow_whenTypeIsBlank() {
        Machine m = new Machine();
        m.setType("   ");
        m.setModel("XPT-300");

        assertThatThrownBy(() -> validator.validateCreation(m))
                .isInstanceOf(InvalidFieldException.class);
    }

    @Test
    void validateCreation_shouldThrow_whenTypeExceedsMaxLength() {
        Machine m = new Machine();
        m.setType("a".repeat(129)); // TYPE_MAX + 1
        m.setModel("XPT-300");

        assertThatThrownBy(() -> validator.validateCreation(m))
                .isInstanceOf(InvalidFieldException.class);
    }

    @Test
    void validateCreation_shouldThrow_whenManufacturerExceedsMaxLength() {
        Machine m = new Machine();
        m.setType("Prensa");
        m.setManufacturer("a".repeat(256)); // MANUFACTURER_MAX + 1
        m.setModel("XPT-300");

        assertThatThrownBy(() -> validator.validateCreation(m))
                .isInstanceOf(InvalidFieldException.class);
    }

    @Test
    void validateCreation_shouldThrow_whenModelIsBlank() {
        Machine m = new Machine();
        m.setType("Prensa");
        m.setModel("  ");

        assertThatThrownBy(() -> validator.validateCreation(m))
                .isInstanceOf(MissingRequiredFieldException.class);
    }

    @Test
    void validateCreation_shouldThrow_whenModelExceedsMaxLength() {
        Machine m = new Machine();
        m.setType("Prensa");
        m.setModel("a".repeat(256)); // MODEL_MAX + 1

        assertThatThrownBy(() -> validator.validateCreation(m))
                .isInstanceOf(InvalidFieldException.class);
    }

    @Test
    void validateCreation_shouldPass_whenValid() {
        Machine m = new Machine();
        m.setType("Prensa");
        m.setManufacturer("Siemens");
        m.setModel("XPT-300");

        assertThatCode(() -> validator.validateCreation(m))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCreation_shouldPass_whenManufacturerIsNull() {
        Machine m = new Machine();
        m.setType("Prensa");
        m.setManufacturer(null);
        m.setModel("XPT-300");

        assertThatCode(() -> validator.validateCreation(m))
                .doesNotThrowAnyException();
    }

    // =================== UPDATE ===================

    @Test
    void validateUpdate_shouldThrow_whenBeforeIsNull() {
        UpdateMachineDTO dto = mockUpdate("Prensa", "Siemens", "XPT-300");

        assertThatThrownBy(() -> validator.validateUpdate(null, dto))
                .isInstanceOf(MissingRequiredFieldException.class);
    }

    @Test
    void validateUpdate_shouldThrow_whenDtoIsNull() {
        Machine before = new Machine();

        assertThatThrownBy(() -> validator.validateUpdate(before, null))
                .isInstanceOf(MissingRequiredFieldException.class);
    }

    @Test
    void validateUpdate_shouldThrow_whenTypeIsBlank() {
        Machine before = new Machine();
        UpdateMachineDTO dto = mockUpdate("   ", "Siemens", "XPT-300");

        assertThatThrownBy(() -> validator.validateUpdate(before, dto))
                .isInstanceOf(MissingRequiredFieldException.class);
    }

    @Test
    void validateUpdate_shouldThrow_whenTypeExceedsMaxLength() {
        Machine before = new Machine();
        UpdateMachineDTO dto = mockUpdate("a".repeat(129), "Siemens", "XPT-300");

        assertThatThrownBy(() -> validator.validateUpdate(before, dto))
                .isInstanceOf(InvalidFieldException.class);
    }

    @Test
    void validateUpdate_shouldThrow_whenManufacturerExceedsMaxLength() {
        Machine before = new Machine();
        UpdateMachineDTO dto = mockUpdate("Prensa", "a".repeat(256), "XPT-300");

        assertThatThrownBy(() -> validator.validateUpdate(before, dto))
                .isInstanceOf(InvalidFieldException.class);
    }

    @Test
    void validateUpdate_shouldThrow_whenModelIsBlank() {
        Machine before = new Machine();
        UpdateMachineDTO dto = mockUpdate("Prensa", "Siemens", "   ");

        assertThatThrownBy(() -> validator.validateUpdate(before, dto))
                .isInstanceOf(MissingRequiredFieldException.class);
    }

    @Test
    void validateUpdate_shouldThrow_whenModelExceedsMaxLength() {
        Machine before = new Machine();
        UpdateMachineDTO dto = mockUpdate("Prensa", "Siemens", "a".repeat(256));

        assertThatThrownBy(() -> validator.validateUpdate(before, dto))
                .isInstanceOf(InvalidFieldException.class);
    }

    @Test
    void validateUpdate_shouldPass_whenValid() {
        Machine before = new Machine();
        UpdateMachineDTO dto = mockUpdate("Prensa", "Siemens", "XPT-300");

        assertThatCode(() -> validator.validateUpdate(before, dto))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_shouldPass_whenManufacturerIsNull() {
        Machine before = new Machine();
        UpdateMachineDTO dto = mockUpdate("Prensa", null, "XPT-300");

        assertThatCode(() -> validator.validateUpdate(before, dto))
                .doesNotThrowAnyException();
    }

    // =================== DELETE ===================

    @Test
    void validateDelete_shouldThrow_whenEntityIsNull() {
        assertThatThrownBy(() -> validator.validateDelete(null))
                .isInstanceOf(MissingRequiredFieldException.class);
    }

    @Test
    void validateDelete_shouldPass_whenEntityIsNotNull() {
        Machine m = new Machine();
        assertThatCode(() -> validator.validateDelete(m))
                .doesNotThrowAnyException();
    }

    // =================== helpers ===================

    private UpdateMachineDTO mockUpdate(String type, String manufacturer, String model) {
        // Se UpdateMachineDTO for record, troque por "return new UpdateMachineDTO(...)".
        // Aqui estou assumindo DTO com getters (Lombok @Getter) ou interface.
        UpdateMachineDTO dto = org.mockito.Mockito.mock(UpdateMachineDTO.class);
        org.mockito.Mockito.when(dto.getType()).thenReturn(type);
        org.mockito.Mockito.when(dto.getManufacturer()).thenReturn(manufacturer);
        org.mockito.Mockito.when(dto.getModel()).thenReturn(model);
        return dto;
    }
}
