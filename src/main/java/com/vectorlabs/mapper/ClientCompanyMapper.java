package com.vectorlabs.mapper;

import com.vectorlabs.dto.address.AnswerAddressDTO;
import com.vectorlabs.dto.address.RegisterAddressDTO;
import com.vectorlabs.dto.address.UpdateAddressDTO;
import com.vectorlabs.dto.clientcompany.AnswerClientCompanyDTO;
import com.vectorlabs.dto.clientcompany.RegisterClientCompanyAdminDTO;
import com.vectorlabs.dto.clientcompany.RegisterClientCompanyDTO;
import com.vectorlabs.dto.clientcompany.UpdateClientCompanyDTO;
import com.vectorlabs.model.Address;
import com.vectorlabs.model.ClientCompany;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ClientCompanyMapper {

    // =================== ENTITY -> DTO ===================

    @Mapping(target = "userId", source = "user.id")
    AnswerClientCompanyDTO toDTO(ClientCompany entity);

    // =================== DTO -> ENTITY ===================

    // USER create
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "user", ignore = true),       // setado no service
            @Mapping(target = "active", ignore = true),
            @Mapping(target = "deleted", ignore = true),
            @Mapping(target = "deletedAt", ignore = true),

            // auditoria (JPA Auditing / service)
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "updatedBy", ignore = true),
            @Mapping(target = "deletedBy", ignore = true)
    })
    ClientCompany fromRegisterDTO(RegisterClientCompanyDTO dto);

    // ADMIN create (tem userId no DTO, mas user é setado no service)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "user", ignore = true),       // setado no service a partir do userId
            @Mapping(target = "active", ignore = true),
            @Mapping(target = "deleted", ignore = true),
            @Mapping(target = "deletedAt", ignore = true),

            // auditoria (JPA Auditing / service)
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "updatedBy", ignore = true),
            @Mapping(target = "deletedBy", ignore = true)

            // OBS: @Mapping(target="address", source="address") é redundante
            // quando o nome do campo é o mesmo; MapStruct já mapeia sozinho.
    })
    ClientCompany fromRegisterAdminDTO(RegisterClientCompanyAdminDTO dto);

    // =================== PATCH ===================

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "user", ignore = true),
            @Mapping(target = "active", ignore = true),
            @Mapping(target = "deleted", ignore = true),
            @Mapping(target = "deletedAt", ignore = true),

            // auditoria (não mexe via patch)
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "updatedBy", ignore = true),
            @Mapping(target = "deletedBy", ignore = true),

            // endereço é tratado no hook @AfterMapping
            @Mapping(target = "address", ignore = true)
    })
    void updateFromDTO(UpdateClientCompanyDTO dto, @MappingTarget ClientCompany entity);

    // =================== ADDRESS MAPPINGS ===================

    AnswerAddressDTO toDTO(Address address);

    Address fromRegisterDTO(RegisterAddressDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDTO(UpdateAddressDTO dto, @MappingTarget Address address);

    // =================== ADDRESS PATCH HOOK ===================

    @AfterMapping
    default void patchAddress(UpdateClientCompanyDTO dto, @MappingTarget ClientCompany entity) {
        if (dto == null || dto.address() == null) return;

        if (entity.getAddress() == null) {
            entity.setAddress(new Address());
        }
        updateFromDTO(dto.address(), entity.getAddress());
    }
}