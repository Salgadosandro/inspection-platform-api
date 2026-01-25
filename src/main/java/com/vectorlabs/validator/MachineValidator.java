package com.vectorlabs.validator;

import com.vectorlabs.exception.InvalidFieldException;
import com.vectorlabs.exception.MissingRequiredFieldException;
import com.vectorlabs.dto.machine.UpdateMachineDTO;
import com.vectorlabs.model.Machine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MachineValidator {

    // Limites compatíveis com o mapeamento JPA
    private static final int TYPE_MAX = 128;
    private static final int MANUFACTURER_MAX = 255;
    private static final int MODEL_MAX = 255;

    // =================== CREATE ===================
    public void validateCreation(Machine entity) {
        if (entity == null) {
            throw new MissingRequiredFieldException("Machine entity cannot be null.");
        }

        // type (required)
        if (isBlank(entity.getType())) {
            throw new InvalidFieldException("Field 'type' is required.");
        }
        if (entity.getType().trim().length() > TYPE_MAX) {
            throw new InvalidFieldException("Field 'type' exceeds max length of " + TYPE_MAX + " characters.");
        }

        // manufacturer (optional)
        if (entity.getManufacturer() != null && entity.getManufacturer().trim().length() > MANUFACTURER_MAX) {
            throw new InvalidFieldException("Field 'manufacturer' exceeds max length of " + MANUFACTURER_MAX + " characters.");
        }

        // model (required)
        if (isBlank(entity.getModel())) {
            throw new MissingRequiredFieldException("Field 'model' is required.");
        }
        if (entity.getModel().trim().length() > MODEL_MAX) {
            throw new InvalidFieldException("Field 'model' exceeds max length of " + MODEL_MAX + " characters.");
        }
    }

    public void validateUpdate(Machine before, UpdateMachineDTO dto) {
        if (before == null) {
            throw new MissingRequiredFieldException("Machine 'before' cannot be null.");
        }
        if (dto == null) {
            throw new MissingRequiredFieldException("UpdateMachineDTO cannot be null.");
        }

        // type (required no DTO de update)
        if (isBlank(dto.getType())) {
            throw new MissingRequiredFieldException("Field 'type' is required.");
        }
        if (dto.getType().trim().length() > TYPE_MAX) {
            throw new InvalidFieldException("Field 'type' exceeds max length of " + TYPE_MAX + " characters.");
        }

        // manufacturer (optional)
        if (dto.getManufacturer() != null && dto.getManufacturer().trim().length() > MANUFACTURER_MAX) {
            throw new InvalidFieldException("Field 'manufacturer' exceeds max length of " + MANUFACTURER_MAX + " characters.");
        }

        // model (required no DTO de update)
        if (isBlank(dto.getModel())) {
            throw new MissingRequiredFieldException("Field 'model' is required.");
        }
        if (dto.getModel().trim().length() > MODEL_MAX) {
            throw new InvalidFieldException("Field 'model' exceeds max length of " + MODEL_MAX + " characters.");
        }

    }

    // =================== DELETE ===================
    public void validateDelete(Machine entity) {
        if (entity == null) {
            throw new MissingRequiredFieldException("Machine entity cannot be null.");
        }

    }

    // =================== helpers ===================
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean equalsIgnoreCase(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equalsIgnoreCase(b);
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
