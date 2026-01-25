package com.vectorlabs.mapper;

import com.vectorlabs.dto.machine.AnswerMachineDTO;
import com.vectorlabs.dto.machine.RegisterMachineDTO;
import com.vectorlabs.dto.machine.UpdateMachineDTO;
import com.vectorlabs.model.Machine;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MachineMapper {

    // CREATE: Register DTO -> Entity
    @Mapping(target = "id", ignore = true)
    Machine toEntity(RegisterMachineDTO dto);

    // UPDATE (full/partial): Update DTO -> Entity (in-place)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Machine entity, UpdateMachineDTO dto);

    // Entity -> Answer DTO
    AnswerMachineDTO toDTO(Machine entity);

    // List<Entity> -> List<DTO>
    List<AnswerMachineDTO> toDTO(List<Machine> entities);

    // -------- Helpers de normalização --------
    @AfterMapping
    default void normalize(@MappingTarget Machine entity) {
        if (entity.getType() != null) {
            entity.setType(clean(entity.getType(), 128));
        }
        if (entity.getManufacturer() != null) {
            entity.setManufacturer(clean(entity.getManufacturer(), 255));
        }
        if (entity.getModel() != null) {
            entity.setModel(clean(entity.getModel(), 255));
        }
    }

    // Normaliza espaços, trim e aplica limite de tamanho
    default String clean(String value, int max) {
        if (value == null) return null;
        String v = value.trim().replaceAll("\\s+", " ");
        return v.length() > max ? v.substring(0, max) : v;
    }
}
