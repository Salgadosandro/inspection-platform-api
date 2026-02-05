package com.vectorlabs.mapper;

import com.vectorlabs.dto.rule.rulemodule.AnswerRuleModuleDTO;
import com.vectorlabs.dto.rule.rulemodule.RegisterRuleModuleDTO;
import com.vectorlabs.dto.rule.rulemodule.UpdateRuleModuleDTO;
import com.vectorlabs.model.RuleModule;
import com.vectorlabs.model.RuleSection;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface RuleModuleMapper {

    // Service/Controller usam: mapper.toEntity(dto)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "section", source = "sectionId", qualifiedByName = "toRuleSection")
    @Mapping(target = "active", expression = "java(dto.active() == null ? Boolean.TRUE : dto.active())")
    RuleModule toEntity(RegisterRuleModuleDTO dto);

    // Service/Controller usam: mapper.toDTO(entity)
    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "ruleId", source = "section.rule.id")
    AnswerRuleModuleDTO toDTO(RuleModule entity);

    // Service usa: mapper.updateFromDTO(dto, existing)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateFromDTO(UpdateRuleModuleDTO dto, @MappingTarget RuleModule entity);

    @Named("toRuleSection")
    default RuleSection toRuleSection(UUID sectionId) {
        if (sectionId == null) return null;
        RuleSection s = new RuleSection();
        s.setId(sectionId);
        return s;
    }
}
