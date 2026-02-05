package com.vectorlabs.mapper;

import com.vectorlabs.dto.rule.RegisterRuleDTO;
import com.vectorlabs.dto.rule.ruledection.AnswerRuleSectionDTO;
import com.vectorlabs.dto.rule.ruledection.RegisterRuleSectionDTO;
import com.vectorlabs.dto.rule.ruledection.UpdateRuleSectionDTO;
import com.vectorlabs.model.Rule;
import com.vectorlabs.model.RuleSection;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface RuleSectionMapper {

    // Service/Controller usam: mapper.toEntity(dto)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rule", source = "ruleId", qualifiedByName = "toRule")
    @Mapping(target = "active", expression = "java(dto.active() == null ? Boolean.TRUE : dto.active())")
    RuleSection toEntity(RegisterRuleSectionDTO dto);


    // Service/Controller usam: mapper.toDTO(entity)
    @Mapping(target = "ruleId", source = "rule.id")
    AnswerRuleSectionDTO toDTO(RuleSection entity);

    // Service usa: mapper.updateFromDTO(dto, existing)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rule", ignore = true) // regra: não muda ruleId no update
    void updateFromDTO(UpdateRuleSectionDTO dto, @MappingTarget RuleSection entity);

    @Named("toRule")
    default Rule toRule(UUID ruleId) {
        if (ruleId == null) return null;
        Rule r = new Rule();
        r.setId(ruleId);
        return r;
    }
}
