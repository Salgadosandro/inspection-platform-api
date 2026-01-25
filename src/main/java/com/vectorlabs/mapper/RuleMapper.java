package com.vectorlabs.mapper;

import com.vectorlabs.dto.rule.AnswerRuleDTO;
import com.vectorlabs.dto.rule.RegisterRuleDTO;
import com.vectorlabs.dto.rule.UpdateRuleDTO;
import com.vectorlabs.model.Rule;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public abstract class RuleMapper {

    // ---------- TO DTO ----------
    // deleted será mapeado automaticamente (mesmo nome)
    public abstract AnswerRuleDTO toDTO(Rule entity);

    // ---------- FROM REGISTER ----------
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true) // sempre controla no backend
    public abstract Rule fromRegisterDTO(RegisterRuleDTO dto);

    // ---------- UPDATE (PATCH) ----------
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)     // boa prática: não alterar code
    @Mapping(target = "deleted", ignore = true)  // nunca deixar update comum "deletar"
    public abstract void updateFromDTO(UpdateRuleDTO dto, @MappingTarget Rule entity);

    // ---------- DEFAULTS ----------
    @AfterMapping
    protected void applyDefaultsOnCreate(@MappingTarget Rule entity) {
        if (entity.getActive() == null) {
            entity.setActive(true);
        }
        if (entity.getDeleted() == null) {
            entity.setDeleted(false);
        }
    }
}
