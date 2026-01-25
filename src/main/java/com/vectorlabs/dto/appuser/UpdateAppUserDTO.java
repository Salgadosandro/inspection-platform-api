package com.vectorlabs.dto.appuser;

import com.vectorlabs.dto.address.UpdateAddressDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

public record UpdateAppUserDTO(
        @Size(max = 150)
        String name,

        @Size(max = 500)
        String pictureUrl,

        @Size(max = 14)
        String cpf,

        @Size(max = 18)
        String cnpj,

        @Valid
        UpdateAddressDTO address
) {}
