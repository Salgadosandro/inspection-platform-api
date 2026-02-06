package com.vectorlabs.dto.clientcompany;

import com.vectorlabs.dto.address.UpdateAddressDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

public record UpdateClientCompanyDTO(

        @Size(max = 200)
        String corporateName,

        @Size(max = 200)
        String tradeName,

        @Size(max = 18)
        String cnpj,

        @Size(max = 20)
        String phone,

        @Size(max = 150)
        String email,

        @Valid
        UpdateAddressDTO address

        // Eu NÃO deixaria active/deleted aqui por padrão.
        // active/deleted eu separaria em endpoints/DTOs admin específicos.
) {}
