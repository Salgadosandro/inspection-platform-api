package com.vectorlabs.mapper;

import com.vectorlabs.dto.clientcompany.AnswerClientCompanyDTO;
import com.vectorlabs.dto.clientcompany.RegisterClientCompanyDTO;
import com.vectorlabs.dto.clientcompany.UpdateClientCompanyDTO;
import com.vectorlabs.model.ClientCompany;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ClientCompanyMapper {

    AnswerClientCompanyDTO toDTO(ClientCompany entity);

    ClientCompany fromRegisterDTO(RegisterClientCompanyDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDTO(UpdateClientCompanyDTO dto, @MappingTarget ClientCompany entity);
}
